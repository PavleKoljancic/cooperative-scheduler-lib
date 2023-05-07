package scheduler.Task;

import java.util.concurrent.Semaphore;

import scheduler.Task.State.TaskState;

public abstract class TaskWork {

    private Semaphore finishSemaphore;
    private TaskToken cancelToken;
    private Semaphore waitSemaphore;
    private Task myTask;
    private Thread t = null;
    private long time1;

    public TaskWork() throws InterruptedException {
        this.cancelToken = new TaskToken(false);
        this.finishSemaphore = new Semaphore(1);
        this.waitSemaphore = new Semaphore(1);
        this.finishSemaphore.acquire();
    }

    // Should only be called for tasks that have started execution
    // i.e. they are in one of these states EXECUTIONPAUSED,WAITING,EXECUTING
    void cancel() {

        this.cancelToken.setTriggered(true);
        this.resume();

    }

    void block() throws InterruptedException {
        this.waitSemaphore.acquire();
    }

    // Calling this method on its on can cause a race condition
    // nontheless the method is only called when the scheduler
    // trys to schedule the task of this TaskWork an this
    // method is only called if the task is already created
    // and paused i.e. when its in the EXECUTION PAUSED state
    void resume() {
        if (waitSemaphore.availablePermits() == 0)
            this.waitSemaphore.release();
    }

    void finish() {
        this.finishSemaphore.release();
    }

    void join() {

        try {
            this.finishSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.finishSemaphore.release();
        }
    }
    private void updateExecutionTime() {
        long time2 = System.currentTimeMillis();
        long timePassed = time2 - this.time1;
        this.myTask.addExecutionTime(timePassed);
        this.time1  = time2;
    }
    public boolean Check() {
        if (t == null)
            throw new IllegalStateException("Check cannot be performed before the tread running has been initialized");
        this.updateExecutionTime();
        if (cancelToken.isTriggered())
            return false;
        try {
            this.waitSemaphore.acquire();
            this.waitSemaphore.release();
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        return !cancelToken.isTriggered();
    }

    void Begin(Task myTask) {
        this.myTask = myTask;
        if (t == null) {
            t = new Thread(() -> {
                this.time1=System.currentTimeMillis();
                if (this.Work())
                    this.myTask.StateChange(TaskState.FINISHED);
                else
                    this.myTask.StateChange(TaskState.CANCELLED);
                this.finish();

            });
            t.start();
        } else
            throw new IllegalStateException("Thread already exists!");

    }

    //Dovoljno je da je samo protected jer se samo poziva unutar ove klase.
    protected abstract boolean Work();
    //Mora biti javna da bi Task klasa van ovog paketa imala dostupnost
    public abstract Object Result();

}
