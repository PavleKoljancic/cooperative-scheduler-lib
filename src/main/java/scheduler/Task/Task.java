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
    private long maxExecutionTime; // If 0 no limit
    private long executionTime;
    private long timeSlice; // If 0 no Limit
    private long timeSliceUsed;

    public void setTimeSlice(long timeSlice) {
        this.timeSlice = timeSlice;
    }

    public Task(int priority, boolean wait, TaskWork work, long maxExecutionTime) throws IllegalArgumentException {

        this.maxExecutionTime = maxExecutionTime;
        this.executionTime = 0;

        if (priority < 0)
            throw new IllegalArgumentException("Priority must be greater or equal to zero.");
        this.priority = priority;
        if (wait)
            this.State = TaskState.PAUSED;
        else
            this.State = TaskState.READY;
        this.work = work;
        this.timeSlice = 0;
    }

    public Task(int priority, boolean wait, TaskWork work) throws IllegalArgumentException {
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
            this.work.cancelSignal();
            this.unpauseTask();
        }
    }

    public synchronized void pauseTask() throws InterruptedException {
        if (this.isStateChangePossible() && !this.work.cancelSignalSent()) {
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

    public synchronized boolean Execute(Semaphore schedulerSemaphore) throws InterruptedException {
        if (schedulerSemaphore.tryAcquire())
            synchronized (this) {
                if (this.getState() == TaskState.READY) {
                    this.StateChange(TaskState.EXECUTING);
                    this.timeSliceUsed = 0;
                    if (this.work.isCreated())
                        this.work.resume();
                    else
                        this.work.Begin(this);
                    return true;
                }

                schedulerSemaphore.release();
                return false;
            }
        return false;
    }

     void addExecutionTime(long time) {
        executionTime += time;
        timeSliceUsed += time;
        if (maxExecutionTime > 0 && executionTime >= maxExecutionTime)
            this.cancelTask();
        else if (timeSlice > 0 && timeSliceUsed > timeSlice)
            try {
                this.work.pause();
            } catch (InterruptedException e) {
            }
    }

    public Object getResult() {
        return this.work.Result();
    }
}
