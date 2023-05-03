package scheduler.SchedulingAlgorithms;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import scheduler.Task.Task;
import scheduler.Task.State.TaskState;

class Priority implements SchedulingAlgorithm {
    //Priority queue nije thread safe
    PriorityQueue<Task> priorityQueue;

    public Priority() 
    {
        this.priorityQueue = new PriorityQueue<Task>(new Comparator<Task>() {

            //Jer je 0 kod mene naj visi prioritet
            @Override
            public int compare(Task arg0, Task arg1) {
                if(arg0.getPriority()<arg1.getPriority())
                    return 1;
                if(arg0.getPriority()>arg1.getPriority())
                    return -1;
                return 0;
            }
            
        });
    }
    @Override
    public Task getNextTask() {
        synchronized(this.priorityQueue){
        HashSet<Task> set = new HashSet<Task>();
        Task result = null;
        while(result==null&&!this.priorityQueue.isEmpty())
        {   
            Task temp = this.priorityQueue.poll();
            if(temp.getState()==TaskState.READY)
                result=temp;
            else
                set.add(temp);
        }
        this.priorityQueue.addAll(set);
        return result;}
    }

    @Override
    public boolean add(Task t) {
        synchronized(this.priorityQueue){
        return this.priorityQueue.add(t);}
    }

    @Override
    public boolean remove(Task t) {
        synchronized(this) {
        return this.priorityQueue.add(t);
        }
    }

    
 

}
