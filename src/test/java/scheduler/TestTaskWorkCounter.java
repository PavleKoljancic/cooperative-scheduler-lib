package scheduler;

import scheduler.Task.TaskWork;

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
    public boolean Work() {
        for(;i<max&&this.Check();i++)
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
        return i==max;
    }

    @Override
    public Integer Result() {
        return Integer.valueOf(i);

    }
    
}
