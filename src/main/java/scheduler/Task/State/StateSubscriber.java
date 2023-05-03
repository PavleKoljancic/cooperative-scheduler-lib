package scheduler.Task.State;

import scheduler.Task.Task;

public interface StateSubscriber {
    
    public void Inform(Task task, TaskState former, TaskState current);
}
