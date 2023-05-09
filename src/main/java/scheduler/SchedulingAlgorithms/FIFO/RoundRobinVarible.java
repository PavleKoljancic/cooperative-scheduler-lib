package scheduler.SchedulingAlgorithms.FIFO;

import scheduler.Task.Task;


public class RoundRobinVarible extends FIFO {
    
    private long maxTimeSlice;
    public RoundRobinVarible(int capacity,long maxTimeSlice) 
    {   super(capacity);
        this.maxTimeSlice = maxTimeSlice;
    }
    @Override
    public Task getNextTask() {
        for(Task t: this.concurrentLinkedQueue)
            if(t.getState().canBeScheduled())
            {   this.concurrentLinkedQueue.remove(t);
                t.setTimeSlice(this.getTimeSlice(t));
                return t;
            }
        return null;
    }

    private long getTimeSlice(Task t)
    {
        return maxTimeSlice/(t.getPriority()+1);
    }
}
