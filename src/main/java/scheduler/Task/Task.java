package scheduler.Task;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

import scheduler.Resources.ResourceHandler;
import scheduler.Task.State.StateSubscriber;
import scheduler.Task.State.TaskState;

public class Task {
    private int priority;
    private TaskState State;
    private String TaskName;
    private TaskWork work;
    private HashSet<StateSubscriber> stateChangeSubscribers = new HashSet<StateSubscriber>();
    private long maxExecutionTime; // If 0 no limit
    private long executionTime;
    private ResourceHandler myResourceHandler = null;
    private String cancelComment ="";
    private long timeSlice; // If 0 no Limit
    private long timeSliceUsed;
    private Date startDateTime;
    private Date endDateTime;
    private HashSet<DataChangeSubscriber> dataChangeSubscribers = new HashSet<DataChangeSubscriber>();
    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    private double progress;
   public synchronized void addProgress(double add) 
    {
        if(this.progress+add>1.0)
            this.progress = 1.0;
        else this.progress+=add;
        this.onDataChange(false, true, false);
    }
    public double getProgress() {
        return progress;
    }

    public String getTaskName() {
        return TaskName;
    }

    public String getCancelComment() {
        return cancelComment;
    }
    ResourceHandler getMyResourceHandler() {
        return myResourceHandler;
    }

    public void setMyResourceHandler(ResourceHandler myResourceHandler) {
        if (this.myResourceHandler != null)
            throw new IllegalStateException("The resource handler of a task cannot be changed ");
        this.myResourceHandler = myResourceHandler;
    }

    public long getExecutionTime() {
        return executionTime;
    }


    // Sets the time slice alloted to this task
    public void setTimeSlice(long timeSlice) {
        this.timeSlice = timeSlice;
    }

    public long getTimeSlice() {
        return this.timeSlice;
    }

    public Task(int priority, boolean wait, TaskWork work, long maxExecutionTime, Date starDateTime, Date endDateTime, String TaskName)
            throws IllegalArgumentException {
            this.progress = 0;
        this.TaskName = TaskName;
        this.startDateTime = starDateTime;
        this.endDateTime = endDateTime;
        this.maxExecutionTime = maxExecutionTime;
        this.executionTime = 0;

        if (priority < 0)
            throw new IllegalArgumentException("Priority must be greater or equal to zero.");
        this.priority = priority;
        if (wait)
            this.State = TaskState.READY_PAUSED;
        else
            this.State = TaskState.READY;
        this.work = work;
        this.timeSlice = 0;
    }

    public Task(int priority, boolean wait, TaskWork work) throws IllegalArgumentException {
        this(priority, wait, work, 0, null, null, "Unnamed task");
    }

    public Task(int priority, boolean wait, TaskWork work, long maxExecutionTime) throws IllegalArgumentException {
        this(priority, wait, work, maxExecutionTime, null, null,"Unnamed task");
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.onDataChange(true, false, false);
    }

    public int getPriority() {
        return priority;
    }

    public TaskState getState() {

        return State;
    }

    public void cancelTask(String Comment) {
       
        // If the task isn't already canceled
        // or finished then it's possible to
        // cancel the task.

        synchronized (this) {
            if (this.State.isStateChangePossible()) {
                this.cancelComment= Comment;
                // If the task hasn't started execution
                // It's enough to just change the state to CANCELLED
                // The scheduler will remove the canceled task
                // from the queue and release the finish semaphore
                if (this.State == TaskState.READY || this.State == TaskState.READY_PAUSED) {
                    this.StateChange(TaskState.CANCELLED);
                    this.work.finish();
                } else {

                    // If the task has started execution 
                    // then the task has to be cancelled by calling the
                    // task works execution method so that it can set the
                    // cancel trigger and unblock any waiting threads.
                    // Due to the implementation there is no grantee
                    // such as in the previous case that cancellation
                    // as this is up to the proper implementation of
                    // the cooperative mechanisms
                    this.work.cancel();

                }
            }
        }
    }

    // If state change is not possible meaning that
    // the state of the task is already FINISHED or CANCELLED
    // then nothing happens.
    // No need to synchronize the method cause the entire object
    // is synchronized thus no race condition can occur.

    // If the task is in the EXECUTING state it performs a block
    // and then the necessary state change.
    // Otherwise it just performs the necessary state change as
    // the task is either already blocked or it never started execution.

    public void pauseTask() throws InterruptedException {
        synchronized (this) {
            if (this.State.withResources())
                throw new IllegalStateException("When task is with resources it cannot be paused!");
            if (this.State.isStateChangePossible()) {
                if (this.getState() == TaskState.READY) {
                    this.StateChange(TaskState.READY_PAUSED);
                }
                if (this.getState() == TaskState.EXECUTING) {
                    this.work.block();
                    this.StateChange(TaskState.EXECUTION_PAUSED);
                }
                if (this.getState() == TaskState.WAITING) {
                    this.StateChange(TaskState.EXECUTION_PAUSED);
                }
            }
        }
    }
    // If state change is not possible meaning that
    // the state of the task is already FINISHED or CANCELLED
    // then nothing happens.
    // If state change is possible and the task is in one of the
    // 2 paused states then it switches it to an Schedulable state
    // It should be noted that no unblocking of the task if its
    // blocked occurs here this state change is transparent to
    // the task work and is only of use to the scheduler.

    public void unpauseTask() {
        synchronized (this) {
            if (this.State.isStateChangePossible()) {
                if (this.getState() == TaskState.READY_PAUSED) {
                    this.StateChange(TaskState.READY);
                }
                if (this.getState() == TaskState.EXECUTION_PAUSED) {
                    this.StateChange(TaskState.WAITING);
                }
            }
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
        synchronized (this.stateChangeSubscribers) {
        for (StateSubscriber s : this.stateChangeSubscribers) {
            s.Inform(this, former, current);
        }}
    }

        public boolean addDataChangeSubscriber(DataChangeSubscriber s) {
        synchronized (this.dataChangeSubscribers) {
            return this.dataChangeSubscribers.add(s);
        }
    }

    public boolean removeDataChangeSubscriber(DataChangeSubscriber s) {
        synchronized (this.dataChangeSubscribers) {
            return this.dataChangeSubscribers.remove(s);
        }
    }

    private synchronized void onDataChange(boolean priorityChanged, boolean progressChanged, boolean executionTimeChanged) {
        synchronized (this.dataChangeSubscribers) {
        for (DataChangeSubscriber s : this.dataChangeSubscribers) {
            s.Inform(priorityChanged,progressChanged,executionTimeChanged);
        }}
    }

    // Method that performs a change of state and calls the onStateChange method
    // so that all state subscribers can be informed about the change.
    synchronized void StateChange(TaskState nextState) {

        TaskState formerState;
        synchronized (this.State) {
            formerState = this.State;
            this.State = nextState;

        }
        onStateChange(formerState, nextState);
    }

    // Blocking method that waits the the underlying semaphore
    // of the work task is not released.
    public void join() {
        this.work.join();
    }

    // Tries acquire the schedulers semaphore
    // if it fails it returns false.
    // If it successfully acquires the schedulers semaphore
    // It checks if the task is in a schedulable state
    // meaning that the task has to be in the READY or WAITING state
    // If its not in schedulable state it releases the schedulers
    // semaphore
    // If the task is in a schedulable state it changes the tasks state to EXECUTING
    // and then
    // performs either
    // resume or begin based on the current state of the task.
    // The schedulers semaphore gets released when the task changes state
    public synchronized boolean Execute(Semaphore schedulerSemaphore) throws InterruptedException {
        if (schedulerSemaphore.tryAcquire())
            synchronized (this) {
                if (this.getState().canBeScheduled()) {
                    // Ovdje se timeSlice used resetuje svaki put kada zadatak ponovo bude
                    // rasporedjen.
                    this.timeSliceUsed = 0;
                    if (this.getState() == TaskState.READY) {
                        this.work.Begin(this);
                        this.StateChange(TaskState.EXECUTING);
                    } else if (this.getState() == TaskState.WAITING) {
                        this.work.resume();
                        this.StateChange(TaskState.EXECUTING);
                    } else if (this.getState() == TaskState.WAITING_WITH_RESOURCES) {
                        this.work.resume();
                        this.StateChange(TaskState.EXECUTING_WITH_RESOURCES);
                    }
                    
                    return true;
                }

                schedulerSemaphore.release();
                return false;
            }
        return false;
    }

    // Performs a block and proper state change if
    // the task is in the EXECUTING state.
    public synchronized void preemptiveStop() {
        synchronized (this) {
            if (this.State == TaskState.EXECUTING)
                try {
                    this.work.block();
                    this.StateChange(TaskState.WAITING);
                } catch (InterruptedException e) {
                }
                if (this.State == TaskState.EXECUTING_WITH_RESOURCES)
                try {
                    this.work.block();
                    this.StateChange(TaskState.WAITING_WITH_RESOURCES);
                } catch (InterruptedException e) {
                }


        }
    }

    // Updates the time used by the task.
    // And performs cancellation or
    // a preemptive stop if needed
    void addExecutionTime(long time) {
        executionTime += time;
        timeSliceUsed += time;

        if (maxExecutionTime > 0 && executionTime >= maxExecutionTime)
            this.cancelTask("Canceled by scheduler execution time is all used up.");
        else if (timeSlice > 0 && timeSliceUsed > timeSlice)
            this.preemptiveStop();
        this.onDataChange(false, false, true);
    }

    public Object getResult() {
        return this.work.Result();
    }

    public void resourcesGranted(ResourceHandler resourceHandler) {
        if (resourceHandler == this.myResourceHandler) {
            synchronized (this) {
                if (this.State.isStateChangePossible()) {
                    if (this.State == TaskState.EXECUTING)
                        this.StateChange(TaskState.EXECUTING_WITH_RESOURCES);
                    else
                        this.StateChange(TaskState.WAITING_WITH_RESOURCES);
                }
            }
        }
    }

    public void waitingForResources(ResourceHandler resourceHandler) throws InterruptedException {
        if (resourceHandler == this.myResourceHandler) {
            synchronized (this) {
                if (this.State.isStateChangePossible()) {
                    if (this.State == TaskState.EXECUTING)
                        this.work.block();
                    this.StateChange(TaskState.WAITING_FOR_RESOURCES);
                }
            }
        }
    }

    public void resourcesReleased(ResourceHandler resourceHandler) {
        if (resourceHandler == this.myResourceHandler) {
            synchronized (this) {
                if (this.State.isStateChangePossible()) {
                    if(this.State==TaskState.EXECUTING_WITH_RESOURCES)
                        this.StateChange(TaskState.EXECUTING);
                    else throw new IllegalStateException("State when releasing resources must be EXECUTING.");
                }
            }
        }
    }

}
