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
            this.State = TaskState.NOTREADY;
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

        return State;
    }

    public void cancelTask() {

        // If the task isn't already canceled
        // or finished then it's possible to
        // cancel the task

        synchronized (this) {
            if (this.isStateChangePossible()) {
                // If the task hasn't started execution
                // It's enough to just change the state to cancel
                // The scheduler will remove the canceled task
                // from the queue.
                if (this.State == TaskState.READY || this.State == TaskState.NOTREADY)
                    this.StateChange(TaskState.CANCELLED); // problem sa joinom
                else {
                    this.work.cancel();
                }
            }
        }
    }

    public void pauseTask() throws InterruptedException {
        synchronized (this) {
            if (this.isStateChangePossible()) {
                if (this.getState() == TaskState.READY) {
                    this.StateChange(TaskState.NOTREADY);
                }
                if (this.getState() == TaskState.EXECUTING) {
                    this.work.block();
                    this.StateChange(TaskState.EXECUTIONPAUSED);
                }
            }
        }
    }

    public void unpauseTask() {
        synchronized (this) {
            if (this.isStateChangePossible()) {
                if (this.getState() == TaskState.NOTREADY) {
                    this.StateChange(TaskState.READY);
                }
                if (this.getState() == TaskState.EXECUTIONPAUSED) {
                    this.StateChange(TaskState.WAITING);
                }
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
                if (this.getState() == TaskState.READY || this.getState() == TaskState.WAITING) {

                    this.timeSliceUsed = 0;
                    if (this.getState() == TaskState.READY)
                        this.work.Begin(this);
                    else if (this.getState() == TaskState.WAITING)
                        this.work.resume();
                    this.StateChange(TaskState.EXECUTING);
                    return true;
                }

                schedulerSemaphore.release();
                return false;
            }
        return false;
    }

    public synchronized void premtiveStop()
    {
        synchronized(this) 
        {   
            if(this.State==TaskState.EXECUTING)
            try {
                this.work.block();
                this.StateChange(TaskState.WAITING);
            } catch (InterruptedException e) {
            }

        }
    }

    void addExecutionTime(long time) {
        executionTime += time;
        timeSliceUsed += time;
        if (maxExecutionTime > 0 && executionTime >= maxExecutionTime)
            this.cancelTask();
        else if (timeSlice > 0 && timeSliceUsed > timeSlice)
           this.premtiveStop();
    }

    public Object getResult() {
        return this.work.Result();
    }
}
