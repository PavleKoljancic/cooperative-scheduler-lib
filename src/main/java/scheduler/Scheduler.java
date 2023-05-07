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

    Scheduler(SchedulingAlgorithm schedulingAlgorithm) {
        this.schedulingAlgorithm = schedulingAlgorithm;
        semaphore = new Semaphore(schedulingAlgorithm.getCapacity());
        this.timer = new Timer(true); // Putting it as Daemon thread so it doesn't stop exit if all other threads are
                                      // done
    }

    public boolean addTask(Task t) {

        boolean result = schedulingAlgorithm.add(t);
        if (result) {
            t.addStateSubscriber(this);
            if (t.getState().canBeScheduled())
                this.tryExecutingNextTask();
        }
        return result;
    }

    public boolean addTask(Task t, Date startDate) {
        if (startDate != null)
            try {
                t.pauseTask();
                boolean result = schedulingAlgorithm.add(t);
                t.addStateSubscriber(this);
                if (result)
                    timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            t.unpauseTask();
                        }

                    }, startDate.getTime());
                return result;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        return false;
    }

    public boolean addTask(Task t, Date startDate, Date EndDate) {

        Boolean result = addTask(t, startDate);
        if (result)
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if (t.getState().isStateChangePossible())
                        t.cancelTask();
                }

            }, EndDate);
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
        //If the  current state of  the task is CANCELLED
        // and the former state isn't EXECUTING then the task
        // still in the queue thus it should be removed cause it can never be scheduled 
        

        if (current == TaskState.CANCELLED&& former!=TaskState.EXECUTING)
            this.schedulingAlgorithm.remove(task);
    }

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
