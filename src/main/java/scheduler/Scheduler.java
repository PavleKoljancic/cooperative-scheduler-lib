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
        this.timer = new Timer();
    }

    public boolean addTask(Task t) {

        boolean result = schedulingAlgorithm.add(t);
        t.addStateSubscriber(this);
        if (t.getState() == TaskState.READY)
            this.executeNextTask();

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
                    if (t.getState() != TaskState.FINISHED || t.getState() != TaskState.CANCELLED)
                        t.cancelTask();
                }

            }, EndDate);
        return result;

    }

    @Override
    public synchronized void Inform(Task task, TaskState former, TaskState current) {

        if (current == TaskState.PAUSED) {
            this.schedulingAlgorithm.add(task);
            this.semaphore.release();
        }
        if (current == TaskState.FINISHED || current == TaskState.CANCELLED) {
            this.semaphore.release();

        }
        this.executeNextTask();
    }

    private synchronized void executeNextTask() {
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
