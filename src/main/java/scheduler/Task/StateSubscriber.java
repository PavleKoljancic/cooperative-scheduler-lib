package scheduler.Task;

public interface StateSubscriber {
    
    public void Inform(Task task, TaskState former, TaskState current);
}
