package scheduler.Task;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

public class Task {
    private int priority;
    TaskState State;
    TaskToken cancelToken;
    TaskToken pauseToken;
    TaskExecution taskExecution;
    HashSet<StateSubscriber> stateChangeSubscribers = new HashSet<>();
    int iValue = 0;

    public Task(int priority, boolean wait, TaskExecution taskExecution) {
        this.cancelToken = new TaskToken(false);
        this.pauseToken = new TaskToken(false);
        this.priority = priority;
        if (wait)
            this.State = TaskState.PAUSED;
        else
            this.State = TaskState.READY;
        this.taskExecution = taskExecution;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public synchronized TaskState getState() {
        return State;
    }


    public void cancelTask() {
        synchronized (this.cancelToken) {
            if(this.State!=TaskState.EXECUTING)
            this.StateChange(TaskState.CANCELLED);
            this.cancelToken.setTriggered(true);
            
        }
    }

    public void pauseTask() {
        synchronized (this.pauseToken) {
            if (!pauseToken.isTriggered())
                this.pauseToken.setTriggered(true);
        }
    }

    public void unpauseTask() 
    {   synchronized(this.pauseToken){
        if(pauseToken.isTriggered())
        this.pauseToken.setTriggered(false);
        if(this.State==TaskState.PAUSED){
            this.StateChange(TaskState.READY);
        }
        }
    }

    public synchronized boolean addStateSubscriber(StateSubscriber s) {
        return this.stateChangeSubscribers.add(s);
    }

    public synchronized boolean removeStateSubscriber(StateSubscriber s) {
        return this.stateChangeSubscribers.remove(s);
    }

    public synchronized void onStateChange( TaskState former, TaskState current) {
        for (StateSubscriber s : this.stateChangeSubscribers) {
            s.Inform(this,former, current);
        }
    }

    private void StateChange(TaskState nextState) {
        TaskState formmerState = this.State;
        this.State = nextState;
        onStateChange(formmerState, nextState);
    }

    public boolean Execute(Semaphore schudlerSemaphore) throws InterruptedException {
        if (this.getState() == TaskState.READY) {
            schudlerSemaphore.acquire();
            final Task temp = this;
            Thread t = new Thread(

                    () -> {
                        boolean finised = false;
                        while(!(this.cancelToken.isTriggered()||this.pauseToken.isTriggered())&&!finised)
                            
                                finised=this.taskExecution.doInteration();

                            
                            
                            
                        
                        if (finised)
                            this.StateChange(TaskState.FINISHED);
                        else if (this.cancelToken.isTriggered())
                            this.StateChange(TaskState.CANCELLED);
                        else 
                            this.StateChange(TaskState.PAUSED);
                            
                        
                        schudlerSemaphore.release();
                    });
            t.start();

            return true;
        }
        return false;
    }

    

}
