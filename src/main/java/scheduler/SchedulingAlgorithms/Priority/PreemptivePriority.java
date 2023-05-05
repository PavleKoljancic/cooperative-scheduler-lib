package scheduler.SchedulingAlgorithms.Priority;

import java.util.HashSet;

import scheduler.Task.Task;
import scheduler.Task.State.StateSubscriber;
import scheduler.Task.State.TaskState;

public class PreemptivePriority extends Priority implements StateSubscriber {
    protected HashSet<Task> executingTasksSet;

    public PreemptivePriority(int capacity) {
        super(capacity);
        this.executingTasksSet = new HashSet<Task>();

    }

    @Override
    public boolean add(Task t) {
        synchronized (this) {
            boolean result = this.priorityQueue.add(t);
            if (result && t.getState().canBeScheduled() && this.executingTasksSet.size() == this.capacity) {
                Task max = null;
                for (Task temp : this.executingTasksSet)
                    if (max == null || max.getPriority() < temp.getPriority())
                        max = temp;
                if (max != null && t.getPriority() < max.getPriority())
                    max.premtiveStop();
            }
            return result;
        }
    }

    @Override
    public void Inform(Task task, TaskState former, TaskState current) {
        if (former == TaskState.EXECUTING)
            this.executingTasksSet.remove(task);
        if (current == TaskState.EXECUTING)
            this.executingTasksSet.add(task);
    }
}
