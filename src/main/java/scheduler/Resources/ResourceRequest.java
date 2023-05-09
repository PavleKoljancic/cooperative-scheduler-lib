package scheduler.Resources;

import java.util.HashSet;

import scheduler.Task.Task;

public class ResourceRequest {
   private Task requester; //Task requesting or Holding the resources
   private HashSet<String> resources; // The resources being requested or held
   private int originalPriority; // The tasks Original priority;
   private boolean hasResources; //Flag indicating if the task has the RequestedResources or is waiting for them.

public ResourceRequest(Task requester, HashSet<String> resources) {
        this.requester = requester;
        this.resources = new HashSet<String>(resources);
        this.originalPriority = requester.getPriority();
        this.hasResources = false;
    }
    public Task getRequester() {
        return requester;
    }
    public HashSet<String> getResources() {
        return resources;
    }
    public int getOriginalPriority() {
        return originalPriority;
    }
    public boolean hasResources() {
        return hasResources;
    }
    public void gainedResources() 
    {
        this.hasResources = true;
    }
    public void expandRequest(HashSet<String> otherResources) 
    {
        this.resources.addAll(otherResources);
    }
}
