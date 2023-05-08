package scheduler.Task;

import java.util.Date;
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
    private Date startDateTime; 
    private Date endDateTime;


    //Sets the time slice alloted to this task 
    public void setTimeSlice(long timeSlice) {
        this.timeSlice = timeSlice;
    }

    public Task(int priority, boolean wait, TaskWork work, long maxExecutionTime,Date starDateTime, Date endDateTime) throws IllegalArgumentException {
        this.startDateTime = starDateTime;
        this.endDateTime = endDateTime;
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
        this(priority, wait, work, 0,null,null);
    }
    public Task(int priority, boolean wait, TaskWork work,long maxExecutionTime) throws IllegalArgumentException {
        this(priority, wait, work, maxExecutionTime,null,null);
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
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
        // cancel the task.

        synchronized (this) {
            if (this.State.isStateChangePossible()) {
                // If the task hasn't started execution
                // It's enough to just change the state to CANCELLED
                // The scheduler will remove the canceled task
                // from the queue and release the finish semaphore
                if (this.State == TaskState.READY || this.State == TaskState.NOTREADY) {
                    this.StateChange(TaskState.CANCELLED);
                    this.work.finish();
                } else {
                    
                    //If the task has started execution i.e. it is
                    // in the EXECUTING EXECUTIONPAUSED or WAITING state
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


     //If state change is not possible meaning that 
    // the state of the task is already FINISHED or CANCELLED
    // then nothing happens.
    //No need to synchronize the method cause the entire object
    //is synchronized thus no race condition can occur.
   
    //If the task is in the EXECUTING state it performs a block
    //and then the necessary state change.
    //Otherwise it just performs the necessary state change as 
    //the task is either already blocked or it never started execution. 
    
    public void pauseTask() throws InterruptedException {
        synchronized (this) {
            if (this.State.isStateChangePossible()) {
                if (this.getState() == TaskState.READY) {
                    this.StateChange(TaskState.NOTREADY);
                }
                if (this.getState() == TaskState.EXECUTING) {
                    this.work.block();
                    this.StateChange(TaskState.EXECUTIONPAUSED);
                }
                if (this.getState() == TaskState.WAITING) {
                    this.StateChange(TaskState.EXECUTIONPAUSED);
                }
            }
        }
    }
    //If state change is not possible meaning that 
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
                if (this.getState() == TaskState.NOTREADY) {
                    this.StateChange(TaskState.READY);
                }
                if (this.getState() == TaskState.EXECUTIONPAUSED) {
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
        for (StateSubscriber s : this.stateChangeSubscribers) {
            s.Inform(this, former, current);
        }
    }


    //Method that performs a change of state and calls the onStateChange method
    // so that all state subscribers can be informed about the change.
    synchronized void StateChange(TaskState nextState) {

        TaskState formerState;
        synchronized (this.State) {
            formerState = this.State;
            this.State = nextState;

        }
        onStateChange(formerState, nextState);
    }
    //Blocking method that waits the the underlying semaphore 
    // of the work task is not released.
    public void join() {
        this.work.join();
    }


    //Tries acquire the schedulers semaphore
    //if it fails it returns false.
    //If it successfully acquires the schedulers semaphore
    //It checks if the task is in a schedulable state
    //meaning that the task has to be in the READY or WAITING state
    //If its not in schedulable state it releases the schedulers
    //semaphore 
    //If the task is in a schedulable state it changes the tasks state to EXECUTING and then
    // performs either
    //resume or begin based on the  current state of the task.
    //The schedulers semaphore gets released when the task changes state
    public synchronized boolean Execute(Semaphore schedulerSemaphore) throws InterruptedException {
        if (schedulerSemaphore.tryAcquire())
            synchronized (this) {
                if (this.getState() == TaskState.READY || this.getState() == TaskState.WAITING) {
                    //Ovdje se timeSlice used resetuje svaki put kada zadatak ponovo bude rasporedjen.
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


    //Performs a block and proper state change if 
    //the task is in the EXECUTING state.
    public synchronized void preemptiveStop() {
        synchronized (this) {
            if (this.State == TaskState.EXECUTING)
                try {
                    this.work.block();
                    this.StateChange(TaskState.WAITING);
                } catch (InterruptedException e) {
                }

        }
    }

    //Updates the time used by the task.
    //And performs cancellation or 
    // a preemptive stop if needed
    void addExecutionTime(long time) {
        executionTime += time;
        timeSliceUsed += time;
        if (maxExecutionTime > 0 && executionTime >= maxExecutionTime)
            this.cancelTask();
        else if (timeSlice > 0 && timeSliceUsed > timeSlice)
            this.preemptiveStop();
    }

    public Object getResult() {
        return this.work.Result();
    }


}
