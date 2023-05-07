package scheduler.Task;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

import scheduler.Task.State.TaskState;

public abstract class TaskWork {

    private Semaphore finishSemaphore;
    TaskToken cancelToken;
    private Task myTask;
    private HashSet<WorkInstance> workInstances;
    

    public TaskWork() throws InterruptedException {
        this(1);
    }

    public TaskWork(int degreeOfParallelism) throws InterruptedException {

        this.cancelToken = new TaskToken(false);
        this.finishSemaphore = new Semaphore(1);
        this.workInstances = new HashSet<WorkInstance>(degreeOfParallelism);
        for (int i = 0; i < degreeOfParallelism; i++)
            this.workInstances.add(new WorkInstance(this));
        this.finishSemaphore.acquire();

    }

    // Should only be called for tasks that have started execution
    // i.e. they are in one of these states EXECUTIONPAUSED,WAITING,EXECUTING
    void cancel() {

        this.cancelToken.setTriggered(true);
        this.resume();

    }

    void block() throws InterruptedException {
        for (WorkInstance instance : workInstances)
            instance.block();
    }

    // Calling this method on its on can cause a race condition
    // nontheless the method is only called when the scheduler
    // trys to schedule the task of this TaskWork an this
    // method is only called if the task is already created
    // and paused i.e. when its in the EXECUTION PAUSED state
    void resume() {
        for (WorkInstance instance : workInstances)
            instance.resume();
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

    synchronized void addExecutionTime(long timePassed) {
        synchronized (this) {
            this.myTask.addExecutionTime(timePassed);
        }
    }

    synchronized void updateInstance(WorkInstance workInstance) {
        workInstances.remove(workInstance);
        if (this.workInstances.isEmpty()) {
            if (cancelToken.isTriggered())
                this.myTask.StateChange(TaskState.CANCELLED);
            else
                this.myTask.StateChange(TaskState.FINISHED);
            this.finish();

        }
    }

    // Dovoljno je da je samo protected jer se samo poziva unutar ove klase.
    protected abstract void Work(WorkInstance instance);

    // Mora biti javna da bi Task klasa van ovog paketa imala dostupnost
    public abstract Object Result();

    public void Begin(Task task) {
        this.myTask = task;
        for (WorkInstance instance : workInstances)
            instance.Begin();

    }

}
