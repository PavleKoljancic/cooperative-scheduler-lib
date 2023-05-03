package scheduler.SchedulingAlgorithms.Priority;

import java.util.HashSet;

import scheduler.Task.Task;

public class PreemptivePriority extends Priority {
    protected HashSet<Task> executingTasksSet;
    public PreemptivePriority(int capacity)
    {   super(capacity);
        this.executingTasksSet = new HashSet<Task>();
        
    }
    @Override
    public boolean add(Task t) {
        synchronized(this.priorityQueue){
        return this.priorityQueue.add(t);}
    }
}
