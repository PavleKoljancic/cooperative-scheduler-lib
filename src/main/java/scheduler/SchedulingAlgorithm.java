package scheduler;
import java.lang.reflect.Array;
import java.util.ArrayList;

import scheduler.Task.Task;

public interface SchedulingAlgorithm {
    
    public Task getNextTask(ArrayList<Task> tasks);
}
