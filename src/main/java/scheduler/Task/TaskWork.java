package scheduler.Task;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

import scheduler.Resources.ResourceHandler;
import scheduler.Task.State.TaskState;

public abstract class TaskWork {

    private Semaphore finishSemaphore;
    TaskToken cancelToken;
    protected Task myTask;
    private HashSet<WorkInstance> workInstances;
    private int degreeOfParallelism;
    protected ResourceHandler myResourceHandler;
    

    public int getDegreeOfParallelism() {
        return degreeOfParallelism;
    }

    public TaskWork() throws InterruptedException {
        this(1);
    }

    public TaskWork(int degreeOfParallelism) throws InterruptedException {
        this.degreeOfParallelism = degreeOfParallelism;
        this.cancelToken = new TaskToken(false);
        this.finishSemaphore = new Semaphore(1);
        this.workInstances = new HashSet<WorkInstance>(degreeOfParallelism);
        for (int i = 0; i < degreeOfParallelism; i++)
            this.workInstances.add(new WorkInstance(this));
        this.finishSemaphore.acquire();

    }

    // Should only be called for tasks that have started execution
    // i.e. they are in one of these states EXECUTIONPAUSED,WAITING,EXECUTING.
    // It sets the cancelToken to true and resumes the  task.
    void cancel() {

        this.cancelToken.setTriggered(true);
        this.resume();

    }


    //Performs a block on all work instancies of the task
    void block() throws InterruptedException {
        for (WorkInstance instance : workInstances)
            instance.block();
    }

    // Calling this method on its on can cause a race condition
    // nontheless the method is only called when the scheduler
    // tries to schedule the task of this TaskWork an this
    // method is only called if the task is already created
    // and paused i.e. when its in the EXECUTION PAUSED state
    void resume() {
        for (WorkInstance instance : workInstances)
            instance.resume();
    }
    //Releases the finish semaphore in the 
    //that was acquired in the constructor of this object.
    void finish() {
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

    //If a work instance has ended either by finishing or being
    //canceled this method is automatically called by the work instance.
    //It removes the workInstance from the set of work instances.
    //If the set of work instances is empty it checks if the task had any resources
    // and releases them the proper state change
    // based on if the cancelTrigger was set.

    synchronized void updateInstance(WorkInstance workInstance) {
        synchronized(this.workInstances){
        workInstances.remove(workInstance);
        if (this.workInstances.isEmpty()) {
            if(this.myResourceHandler.hasLockedResources(myTask))
                this.myResourceHandler.releaseResources(myTask);
            if (cancelToken.isTriggered())
                this.myTask.StateChange(TaskState.CANCELLED);
            else
                this.myTask.StateChange(TaskState.FINISHED);
        
            this.finish();

        }}
    }


    protected abstract void Work(WorkInstance instance);



    public abstract Object Result();

    public void Begin(Task task) {
        synchronized(this.workInstances) 
        {
        this.myTask = task;
        this.myResourceHandler = task.getMyResourceHandler();
        for (WorkInstance instance : workInstances)
            instance.Begin();
        }
    }



}
