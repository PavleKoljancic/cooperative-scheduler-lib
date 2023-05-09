package scheduler;

import scheduler.SchedulingAlgorithms.FIFO.FIFO;
import scheduler.Task.Task;
import scheduler.Task.TaskWork;
import scheduler.Task.WorkInstance;

public class ParallelismTestTaskWork extends TaskWork{
    private int Index;
    private int [] arr;
    private int sleep;
    private int count;
    public ParallelismTestTaskWork(int degreeOfParallelism, int max, int sleep) throws InterruptedException {
        super(degreeOfParallelism);
        arr = new int [degreeOfParallelism];
        for(int i = 0;i<arr.length-1;i++)
            arr[i] = max / degreeOfParallelism;
        arr[arr.length-1] += max % degreeOfParallelism;
        Index =0;
        this.sleep = sleep;
    }
    private synchronized int getIndex()
    {
        return Index++;
    }
    private synchronized void increment()
    {
        this.count++;
    }
    @Override
    protected void Work(WorkInstance instance) {
        int index = this.getIndex();
        for(int i =0;i<this.arr[index];i++){
            
            try { this.increment();
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    @Override
    public Integer Result() {
        return Integer.valueOf(this.count);
    }
    
    public static void main(String [] args) throws IllegalArgumentException, InterruptedException 
    {
        Scheduler scheduler = new Scheduler(new FIFO(1));
        scheduler.addTask(new Task(0, false, new ParallelismTestTaskWork(12, 50, 1000), 0));
    }
    @Override
    public String[] getArgs() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getArgs'");
    }
}
