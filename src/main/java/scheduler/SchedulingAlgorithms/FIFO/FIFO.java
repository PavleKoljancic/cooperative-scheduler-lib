package scheduler.SchedulingAlgorithms.FIFO;


import java.util.concurrent.ConcurrentLinkedQueue;

import scheduler.SchedulingAlgorithms.SchedulingAlgorithm;
import scheduler.Task.Task;



public class FIFO implements SchedulingAlgorithm {

    //Ovdje sam koristio conurrent queue jer je fifo ko stvoren za njega;
    protected ConcurrentLinkedQueue<Task> concurrentLinkedQueue;
    protected int capacity;

    public FIFO(int capacity) 
    {   this.capacity = capacity;
        concurrentLinkedQueue = new ConcurrentLinkedQueue<Task>();
    }

    @Override
    public Task getNextTask() {
        for(Task t: this.concurrentLinkedQueue)
            if(t.getState().canBeScheduled())
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

    @Override
    public int getCapacity() {
        return    this.capacity;
    }
    
}
