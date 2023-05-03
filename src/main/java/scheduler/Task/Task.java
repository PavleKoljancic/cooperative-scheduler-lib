package scheduler.Task;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

import scheduler.Task.State.StateSubscriber;
import scheduler.Task.State.TaskState;

public class Task {
    private int priority;
    private TaskState State;

    private TaskWork work;
    private HashSet<StateSubscriber> stateChangeSubscribers = new HashSet<>();
    private long maxExecutionTime;
    private long executionTime;

    public Task(int priority, boolean wait, TaskWork work, long maxExecutionTime) throws Exception {

        this.maxExecutionTime = maxExecutionTime;
        this.executionTime = 0;

        if (priority < 0)
            throw new Exception("Priority must be greater or equal to zero.");
        this.priority = priority;
        if (wait)
            this.State = TaskState.PAUSED;
        else
            this.State = TaskState.READY;
        this.work = work;
    }

    public Task(int priority, boolean wait, TaskWork work) throws Exception {
        this(priority, wait, work, 0);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public TaskState getState() {
        synchronized (this.State) {
            return State;
        }
    }

    public void cancelTask() {

        if (this.isStateChangePossible()) {
            if (this.getState() != TaskState.EXECUTING)
                this.StateChange(TaskState.CANCELLED);
            this.work.cancelSignal();
        }
    }

    public synchronized void pauseTask() throws InterruptedException {
        if (this.isStateChangePossible()) {
            if (this.getState() == TaskState.READY) {
                this.StateChange(TaskState.PAUSED);
            }
            if (this.getState() == TaskState.EXECUTING) {
                this.work.pause();
                this.StateChange(TaskState.PAUSED);
            }
        }
    }

    public void unpauseTask() {
        if (this.isStateChangePossible()) {
            if (this.getState() == TaskState.PAUSED) {
                this.StateChange(TaskState.READY);
            }
        }
    }

    private boolean isStateChangePossible() {
        synchronized (this.State) {
            return (this.State != TaskState.FINISHED && this.State != TaskState.CANCELLED);
        }
    }

    public boolean addStateSubscriber(StateSubscriber s) {
        synchronized (this.stateChangeSubscribers) {
            return this.stateChangeSubscribers.add(s);
        }
    }

    public boolean removeStateSubscriber(StateSubscriber s) {
        synchronized (this.stateChangeSubscribers) {
            return this.stateChangeSubscribers.remove(s);
        }
    }

    private synchronized void onStateChange(final TaskState former, final TaskState current) {
        for (StateSubscriber s : this.stateChangeSubscribers) {
            s.Inform(this, former, current);
        }
    }

    synchronized void StateChange(TaskState nextState) {

        TaskState formerState;
        synchronized (this.State) {
            formerState = this.State;
            this.State = nextState;
                
        }
        onStateChange(formerState, nextState);
    }

    public void join() {
        this.work.join();
    }

    public void Execute(Semaphore schedulerSemaphore) throws InterruptedException {
        if (schedulerSemaphore.tryAcquire())
            synchronized (this) {
                if (this.getState() == TaskState.READY) {
                    this.StateChange(TaskState.EXECUTING);
                    if (this.work.isCreated())
                        this.work.resume();
                    else
                        this.work.Begin(this);

                }

                else
                    schedulerSemaphore.release();
            }
    }

    public void addExecutionTime(long time) {
        executionTime+=time;
        if(maxExecutionTime>0&&executionTime>=maxExecutionTime)
            this.cancelTask();
    }

}
