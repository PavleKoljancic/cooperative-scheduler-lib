package scheduler.Task;

import java.util.concurrent.Semaphore;

import scheduler.Task.State.TaskState;

public abstract class TaskWork {

    private Semaphore finishSemaphore;
    private TaskToken cancelToken;
    private Semaphore waitSemaphore;
    private boolean created;
    Thread t = null;

    TaskWork() throws InterruptedException {
        this.cancelToken = new TaskToken(false);
        this.finishSemaphore = new Semaphore(1);
        this.waitSemaphore = new Semaphore(1);
        this.finishSemaphore.acquire();
        this.created =false;
    }

    void cancelSignal() {

        synchronized (this.cancelToken) {
            this.cancelToken.setTriggered(true);

        }
    }

    void pause() throws InterruptedException {
            this.waitSemaphore.acquire();
    }

    void resume() {
        if(waitSemaphore.availablePermits()==0)
            this.waitSemaphore.release();
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

    boolean isCreated() 
    {
        return created;
    }

    void Begin(Task myTask) {

        if (t == null) {
            t = new Thread(() -> {

                boolean result = false;
                while((!result)&&this.cancelToken.isTriggered()!=true)
                {  
                    try {
                       this.waitSemaphore.acquire(); 
                       long time = System.nanoTime();
                       result = this.Work();
                       time = System.nanoTime()-time;
                       this.waitSemaphore.release();
                       
                       myTask.addExecutionTime(time);
                    } catch (InterruptedException e) {
                        
                        e.printStackTrace();
                    }
                   
                }
                if(result==true)
                myTask.StateChange(TaskState.FINISHED);
                if (this.cancelToken.isTriggered())
                myTask.StateChange(TaskState.CANCELLED);
                this.finishSemaphore.release();
                
            });
           t.start();
           this.created=true;
        }

    }
    public abstract boolean Work();

}
