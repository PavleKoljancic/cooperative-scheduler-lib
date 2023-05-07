package scheduler.Task;

import java.util.concurrent.Semaphore;

public class WorkInstance {
    private Thread t = null;
    private long time1;
    private Semaphore waitSemaphore;
    private TaskWork myTaskWork;

    public WorkInstance( TaskWork myTaskWork) {
        this.waitSemaphore = new Semaphore(1);
        this.myTaskWork = myTaskWork;
    }

    void block() throws InterruptedException {
        this.waitSemaphore.acquire();
    }

    void resume() {
        if (waitSemaphore.availablePermits() == 0)
            this.waitSemaphore.release();
    }
    private void updateExecutionTime() {
        long time2 = System.currentTimeMillis();
        long timePassed = time2 - this.time1;
        this.myTaskWork.addExecutionTime(timePassed);
        this.time1  = time2;
    }
    public boolean Check() {
        if (t == null)
            throw new IllegalStateException("Check cannot be performed before the tread running has been initialized");
        this.updateExecutionTime();
        if (this.myTaskWork.cancelToken.isTriggered())
            return false;
        try {
            this.waitSemaphore.acquire();
            this.waitSemaphore.release();
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        return !this.myTaskWork.cancelToken.isTriggered();
    }

    void Begin() {
        if (t == null) {
            t = new Thread(() -> {
                this.time1=System.currentTimeMillis();
                myTaskWork.Work(this);
                myTaskWork.updateInstance(this);

            });
            t.start();
        } else
            throw new IllegalStateException("Thread already exists!");

    }
}
