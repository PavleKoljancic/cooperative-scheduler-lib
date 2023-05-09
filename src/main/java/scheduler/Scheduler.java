package scheduler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import scheduler.Resources.ResourceHandler;
import scheduler.SchedulingAlgorithms.SchedulingAlgorithm;
import scheduler.Task.Task;
import scheduler.Task.State.StateSubscriber;
import scheduler.Task.State.TaskState;

public class Scheduler implements StateSubscriber {

    private Semaphore semaphore;
    private SchedulingAlgorithm schedulingAlgorithm;
    private Timer timer;
    private ResourceHandler resourceHandler;

    public Scheduler(SchedulingAlgorithm schedulingAlgorithm) {
        this.schedulingAlgorithm = schedulingAlgorithm;
        semaphore = new Semaphore(schedulingAlgorithm.getCapacity());
        this.timer = new Timer(true); // Putting it as Daemon thread so it doesn't stop exit if all other threads are
                                      // done
        this.resourceHandler = new ResourceHandler();
    }

    public boolean addTask(Task t) {
        t.setMyResourceHandler(resourceHandler);
        boolean result = schedulingAlgorithm.add(t);
        if (result) {
            t.addStateSubscriber(this);
            if (t.getStartDateTime() != null) {
                this.timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        if (t.getState().isStateChangePossible())
                            t.unpauseTask();
                    }

                }, t.getStartDateTime());
            }
            if (t.getEndDateTime() != null) {
                this.timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        if (t.getState().isStateChangePossible())
                            t.cancelTask();
                    }

                }, t.getEndDateTime());
            }
            if (t.getState().canBeScheduled())
                this.tryExecutingNextTask();
        }
        return result;
    }

    @Override
    public synchronized void Inform(Task task, TaskState former, TaskState current) {
        // If the former state isn't EXECUTING
        // then there is no need to release a semaphore
        if (former == TaskState.EXECUTING) {

            // If state change is still possible ie
            // it is not CANCELED or FINISHED
            // then it should be readd to the "queue"
            if (current.isStateChangePossible())
                this.schedulingAlgorithm.add(task);
            this.semaphore.release();
            this.tryExecutingNextTask();
        }
        // If the former state isn't executing there is no need
        // to release a semaphore or readd the task to the "queue"
        // but if it can be scheduled then the scheduler should
        // try to execute it.
        if (current.canBeScheduled())
            this.tryExecutingNextTask();
        // If the current state of the task is CANCELLED
        // and the former state isn't EXECUTING then the task
        // still in the queue thus it should be removed cause it can never be scheduled
        if (current == TaskState.CANCELLED && former != TaskState.EXECUTING)
            this.schedulingAlgorithm.remove(task);
    }

    // Tries executing next task if there are any available permit's
    // If there are available permits then it tries to get the
    // next task from the scheduling algorithm
    // If any are available then it tries to execute it
    private synchronized void tryExecutingNextTask() {
        if (semaphore.availablePermits() > 0)
            try {
                Task task = schedulingAlgorithm.getNextTask();
                if (task != null)
                    if (!task.Execute(semaphore))
                        this.addTask(task);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
    }

}
