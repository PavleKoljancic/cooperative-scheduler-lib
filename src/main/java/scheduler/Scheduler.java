package scheduler;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import scheduler.SchedulingAlgorithms.SchedulingAlgorithm;
import scheduler.Task.Task;
import scheduler.Task.State.StateSubscriber;
import scheduler.Task.State.TaskState;

public class Scheduler implements StateSubscriber {

    private Semaphore semaphore;
    private SchedulingAlgorithm schedulingAlgorithm;
    private Timer timer;

    Scheduler(int maxNumberOfTasks, SchedulingAlgorithm schedulingAlgorithm) {
        this.schedulingAlgorithm = schedulingAlgorithm;
        semaphore = new Semaphore(maxNumberOfTasks);
        this.timer = new Timer();
    }

    public boolean addTask(Task t) {

        boolean result = schedulingAlgorithm.add(t);
        t.addStateSubscriber(this);
        executeNextTask();

        return result;
    }

    public boolean addTask(Task t, Date executionDate) {
        Date now = new Date();
        long difference = executionDate.getTime() - now.getTime();
        if (difference > 0) {
            try {
                t.pauseTask();
                boolean result = schedulingAlgorithm.add(t);
                t.addStateSubscriber(this);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        t.unpauseTask();
                    }

                }, difference);
                return result;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        return addTask(t);
    }

    @Override
    public synchronized void Inform(Task task, TaskState former, TaskState current) {
        if (current == TaskState.CANCELLED || current == TaskState.FINISHED) {
            schedulingAlgorithm.remove(task);
            task.removeStateSubscriber(this);
        }
        if (current == TaskState.READY)
            this.executeNextTask();
        if (current == TaskState.PAUSED)
            this.schedulingAlgorithm.add(task);
        if(current==TaskState.FINISHED||current==TaskState.CANCELLED)
        {
            this.semaphore.release();
            this.executeNextTask();
        }
    }

    private synchronized void executeNextTask() {
        if (semaphore.availablePermits() > 0)
            try {
                Task task = schedulingAlgorithm.getNextTask();
                if (task != null)
                    task.Execute(semaphore);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
    }

}
