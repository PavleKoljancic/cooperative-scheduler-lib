package scheduler.SchedulingAlgorithms.FIFO;

import scheduler.Task.Task;
import scheduler.Task.State.TaskState;

public class RoundRobin extends FIFO{
    private long timeSlice;
    public RoundRobin(int capacity,long timeSlice) 
    {   super(capacity);
        this.timeSlice = timeSlice;
    }
    @Override
    public Task getNextTask() {
        for(Task t: this.concurrentLinkedQueue)
            if(t.getState()==TaskState.READY)
            {   this.concurrentLinkedQueue.remove(t);
                t.setTimeSlice(this.timeSlice);
                return t;
            }
        return null;
    }
}
