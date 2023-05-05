package scheduler.Task;

import java.util.concurrent.Semaphore;

import scheduler.Task.State.TaskState;

public abstract class TaskWork {

    private Semaphore finishSemaphore;
    private TaskToken cancelToken;
    private Semaphore waitSemaphore;
    private Thread t = null;

    public TaskWork() throws InterruptedException {
        this.cancelToken = new TaskToken(false);
        this.finishSemaphore = new Semaphore(1);
        this.waitSemaphore = new Semaphore(1);
        this.finishSemaphore.acquire();
    }

    synchronized void cancel() {

        synchronized(this) 
        {
            if(!this.cancelToken.isTriggered())
            {
                this.cancelToken.setTriggered(true);
                
            }
        }
    }

    void block() throws InterruptedException {
        this.waitSemaphore.acquire();
    }
    //Calling this method on its on can cause a race condition
    //nontheless the method is only called when the scheduler
    //trys to schedule the task of this TaskWork an this
    // method is only called if the task is already created
    // and paused i.e. when its in the EXECUTION PAUSED state  
    void resume() {
        if (waitSemaphore.availablePermits() == 0)
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

    void Begin(Task myTask) {

        if (t == null) {
            t = new Thread(() -> {

                boolean result = false;
                while (!result && !this.cancelToken.isTriggered()) {

                    if (this.waitSemaphore.tryAcquire()) {
                        long time = System.currentTimeMillis();
                        result = this.Work();
                        time = System.currentTimeMillis() - time;
                        this.waitSemaphore.release();
                        myTask.addExecutionTime(time);
                    } else if (!this.cancelToken.isTriggered()) {

                        try {
                            this.waitSemaphore.acquire();
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        finally
                        {
                            this.waitSemaphore.release();
                        }

                    }

                }
                if (result == true)
                    myTask.StateChange(TaskState.FINISHED);
                if (this.cancelToken.isTriggered())
                {   
                    myTask.StateChange(TaskState.CANCELLED);
                }
                this.finishSemaphore.release();

            });
            t.start();
        }

    }

    public abstract boolean Work();

    public abstract Object Result();

}
