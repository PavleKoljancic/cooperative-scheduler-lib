package scheduler;

import java.util.ArrayList;

import scheduler.Task.Task;
import scheduler.Task.TaskState;

public class Priority implements SchedulingAlgorithm {

    @Override
    public Task getNextTask(ArrayList<Task> tasks) {
        Task result = null;

        for (Task t : tasks) {

            if (t.getState() == TaskState.READY && result == null)
                result = t;
            else if (result != null && result.getPriority() > t.getPriority()) {
                result = t;
                if (result.getPriority() == 0)
                    return result;
            }
        }

        return result;
    }

}
