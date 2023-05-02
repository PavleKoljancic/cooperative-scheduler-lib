package scheduler;

import java.util.ArrayList;

import scheduler.Task.Task;
import scheduler.Task.TaskState;

public class FIFO implements SchedulingAlgorithm {

    @Override
    public Task getNextTask(ArrayList<Task> tasks) {

        for(Task t : tasks ) 
        {
            if(t.getState()==TaskState.READY)
                return t;
        }

        return null;
    
    }
    
}
