package scheduler.Task;

public interface DataChangeSubscriber {
    public void Inform(boolean priorityChanged, boolean progressChanged, boolean executionTimeChanged);
}
