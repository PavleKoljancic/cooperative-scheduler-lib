package scheduler.SchedulingAlgorithms.FIFO;

import scheduler.Task.Task;


public class RoundRobin extends FIFO{
    private long timeSlice;
    public RoundRobin(int capacity,long timeSlice) 
    {   super(capacity);
        this.timeSlice = timeSlice;
    }
    @Override
    public Task getNextTask() {
        for(Task t: this.concurrentLinkedQueue)
            if(t.getState().canBeScheduled())
            {   this.concurrentLinkedQueue.remove(t);
                t.setTimeSlice(this.timeSlice);
                return t;
            }
        return null;
    }
}
