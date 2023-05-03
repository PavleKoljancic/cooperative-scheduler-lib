package scheduler.SchedulingAlgorithms;


import java.util.concurrent.ConcurrentLinkedQueue;

import scheduler.Task.Task;
import scheduler.Task.State.TaskState;


public class FIFO implements SchedulingAlgorithm {

    //Ovdje sam koristio conurrent queue jer je fifo ko stvoren za njega;
    private ConcurrentLinkedQueue<Task> concurrentLinkedQueue;

    public FIFO() 
    {
        concurrentLinkedQueue = new ConcurrentLinkedQueue<Task>();
    }

    @Override
    public Task getNextTask() {
        for(Task t: this.concurrentLinkedQueue)
            if(t.getState()==TaskState.READY)
            {   this.concurrentLinkedQueue.remove(t);
                return t;
            }
        return null;
    }

    @Override
    public boolean add(Task t) {
       return this.concurrentLinkedQueue.add(t);
    }

    @Override
    public boolean remove(Task t) {
        return this.concurrentLinkedQueue.remove(t);
    }
    
}
