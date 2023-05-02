package scheduler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.Flow.Subscriber;

import scheduler.Task.StateSubscriber;
import scheduler.Task.Task;
import scheduler.Task.TaskState;

public class Scheduler implements StateSubscriber {

    ArrayList<Task> tasks;
    Semaphore semaphore;
    SchedulingAlgorithm schedulingAlgorithm;

    Scheduler(int maxNumberOfTasks, SchedulingAlgorithm schedulingAlgorithm) {
        this.schedulingAlgorithm = schedulingAlgorithm;
        this.tasks = new ArrayList<Task>();
        semaphore = new Semaphore(maxNumberOfTasks);
    }

    public synchronized boolean addTask(Task t) {

        boolean result = tasks.add(t);
        t.addStateSubscriber(this);
        executeNextTask();

        return result;
    }

    @Override
    public void Inform(Task task, TaskState former, TaskState current) {
        if (current == TaskState.CANCELLED || current == TaskState.FINISHED) 
        {
            tasks.remove(task);
            task.removeStateSubscriber(this);
        }
        this.executeNextTask();

    }

    private synchronized void executeNextTask() {
        if (semaphore.availablePermits() > 0)
            try {
                Task task = this.getNext();
                if (task != null)
                    task.Execute(semaphore);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
    }

    private synchronized Task getNext() {
        return schedulingAlgorithm.getNextTask(tasks);
    }

}
