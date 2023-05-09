package scheduler.Resources;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import scheduler.Task.Task;

public class ResourceHandler {

    private HashSet<ResourceRequest> gratedRequests;
    private PriorityQueue<ResourceRequest> waitingRequests;
    private HashMap<Task, ResourceRequest> TaskToRequestMap;


    public ResourceHandler() {

        this.gratedRequests = new HashSet<ResourceRequest>();
        this.waitingRequests = new PriorityQueue<ResourceRequest>(new Comparator<ResourceRequest>() {

            @Override
            public int compare(ResourceRequest arg0, ResourceRequest arg1) {
                return arg0.getOriginalPriority() - arg1.getOriginalPriority();
            }

        });
        this.TaskToRequestMap = new HashMap<Task, ResourceRequest>();
    }

    public synchronized void requestResources(HashSet<String> resources, Task requester)
            throws ResourceHandlerException {
        // Checks if any resources were requested
        if (resources.size() > 0) {
            synchronized (this) {
                // If the requester Task is in the Map it means it has either a waiting or
                // granted request
                if (this.TaskToRequestMap.containsKey(requester)) {
                    // if it has a granted request an exception is thrown
                    if (this.gratedRequests.contains(TaskToRequestMap.get(requester)))
                        throw new ResourceHandlerException();
                    // if it is a not granted request the request is expanded by the new resources
                    // Then a new priorityInheritance with the overlapping requests is created.
                    else {
                        ResourceRequest existingRequest = this.TaskToRequestMap.get(requester);
                        HashSet<ResourceRequest> overlappingRequests = this
                                .getOverlappingRequests(existingRequest.getResources());
                        this.priorityInheritance(existingRequest, overlappingRequests);
                    }

                } else {
                    //If the task isn't in the map then it means there are no waiting or granted
                    //requests made by the task.
                    //A new resource request is created the task and resource request are added to the map
                    
                    ResourceRequest newRequest = new ResourceRequest(requester, resources);
                    this.TaskToRequestMap.put(requester, newRequest);
                    //An overlapping set is created with the currently granted Requests 
                    HashSet<ResourceRequest> overlappingRequests = this
                            .getOverlappingRequests(newRequest.getResources());

                    if (overlappingRequests.size() == 0) // No overlap in locked and requested resources

                        this.grantRequest(newRequest);
                        

                    else {
                        this.waitingRequests.add(newRequest);
                        this.priorityInheritance(newRequest, overlappingRequests);

                        try {
                            newRequest.getRequester().waitingForResources(this);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }

                    }

                }
            }
        }
    }

    private synchronized HashSet<ResourceRequest> getOverlappingRequests(HashSet<String> resources) {
        synchronized (this) {
            HashSet<ResourceRequest> overlappingRequests = new HashSet<>();

            for (ResourceRequest grantedRequest : gratedRequests) {
                for (String resource : resources)
                    if (grantedRequest.getResources().contains(resource))
                        overlappingRequests.add(grantedRequest);
            }
            return overlappingRequests;
        }
    }

    private void grantRequest(ResourceRequest requestToGrant) {
        requestToGrant.gainedResources();
        this.gratedRequests.add(requestToGrant);
        requestToGrant.getRequester().resourcesGranted(this);
    }

    public synchronized boolean hasLockedResources(Task task) {
        synchronized (this) {
            return this.TaskToRequestMap.containsKey(task)
                    && this.gratedRequests.contains(this.TaskToRequestMap.get(task));
        }
    }

    public synchronized HashSet<String> getLockedResources(Task holder) {
        if (hasLockedResources(holder))
            synchronized (this) {
                return this.TaskToRequestMap.get(holder).getResources();
            }

        return null;

    }

    public synchronized void releaseResources(Task holder) {
        if (this.hasLockedResources(holder)) {
            synchronized(this){
            ResourceRequest request = this.TaskToRequestMap.get(holder);
            this.TaskToRequestMap.remove(holder, request);
            this.gratedRequests.remove(request);
            request.getRequester().setPriority(request.getOriginalPriority());
            request.getRequester().resourcesReleased(this);
            HashSet<ResourceRequest> readd = new HashSet<ResourceRequest>();
            while(!this.waitingRequests.isEmpty()) 
            {
                ResourceRequest temp = this.waitingRequests.poll();
                HashSet<ResourceRequest> overlapping =  this.getOverlappingRequests(temp.getResources());
                if(overlapping.size()==0)
                    this.grantRequest(temp);
                else readd.add(temp);
            }
            this.waitingRequests.addAll(readd);
        }
        }
    }

    private void priorityInheritance(ResourceRequest toCheck, HashSet<ResourceRequest> overlappingRequests) {

        for (ResourceRequest grantedRequest : overlappingRequests) // Priority Inheritance
            if (grantedRequest.getRequester().getPriority() > toCheck.getOriginalPriority())
                grantedRequest.getRequester().setPriority(toCheck.getOriginalPriority());
    }
}
