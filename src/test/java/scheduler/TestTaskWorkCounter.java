package scheduler;

import scheduler.Task.TaskWork;
import scheduler.Task.WorkInstance;

class TestTaskWorkCounter extends TaskWork {

    public TestTaskWorkCounter(int max, int sleep,String name) throws InterruptedException {
        super();
        this.max = max;
        this.sleep = sleep;
        this.name= name;
    }
    public TestTaskWorkCounter(int max, int sleep) throws InterruptedException {
        super();
        this.max = max;
        this.sleep = sleep;
        this.name= "";
    }
    private String name;
    private int i = 0;
    private int max;
    public int getMax() {
        return max;
    }

    private int sleep;


    @Override
    public Integer Result() {
        return Integer.valueOf(i);

    }
    @Override
    protected void Work(WorkInstance instance) {
        for(;i<max&&instance.Check();i++)
        {   
            if(!this.name.isEmpty())
                System.out.println(name + " Iteration:" +(i+1)+".");
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
}
