package scheduler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import scheduler.SchedulingAlgorithms.FIFO.FIFO;
import scheduler.SchedulingAlgorithms.Priority.Priority;
import scheduler.Task.Task;
import scheduler.Task.TaskWork;
import scheduler.Task.State.TaskState;

public class SchedulerTest {

    @Test
    public void joinTest1() throws Exception {
        Scheduler scheduler = new Scheduler(new FIFO(4));
        TaskWork t1 = new TestTaskWorkCounter(10, 1000);

        Task task = new Task(0, false, t1);

        scheduler.addTask(task);
        task.join();
        Assert.assertEquals(Integer.valueOf(10), task.getResult());
    }

    @Test
    public void joinTest2() throws Exception {
        Scheduler scheduler = new Scheduler(new FIFO(4));
        TaskWork t1 = new TestTaskWorkCounter(10, 1000);

        Task task = new Task(0, false, t1);

        scheduler.addTask(task);
        Assert.assertNotEquals(Integer.valueOf(10), task.getResult());
        task.cancelTask("");
        task.join();
        Assert.assertNotEquals(Integer.valueOf(10), task.getResult());
    }

    @Test
    public void joinCancelledTest2() throws Exception {
        Scheduler scheduler = new Scheduler(new FIFO(4));
        TaskWork t1 = new TestTaskWorkCounter(10, 1000);

        Task task = new Task(0, true, t1);

        scheduler.addTask(task);
        task.cancelTask("");
        task.join();
    }

    @Test
    public void noStartTest() throws Exception {
        Scheduler scheduler = new Scheduler(new FIFO(4));
        TaskWork t1 = new TestTaskWorkCounter(10, 1000);

        Task task = new Task(0, true, t1);

        scheduler.addTask(task);
        Thread.sleep(1000);
        Assert.assertEquals(task.getState(), TaskState.READY_PAUSED);
        Assert.assertNotEquals(Integer.valueOf(10), task.getResult());
    }

    @Test
    public void unPauseTest() throws Exception {
        Scheduler scheduler = new Scheduler(new FIFO(4));
        TaskWork t1 = new TestTaskWorkCounter(10, 1000);

        Task task1 = new Task(0, true, t1);
        TaskWork t2 = new TestTaskWorkCounter(10, 1000);
        scheduler.addTask(task1);

        Task task2 = new Task(0, false, t2);

        scheduler.addTask(task2);
        task2.join();
        Assert.assertEquals(task1.getState(), TaskState.READY_PAUSED);
        Assert.assertEquals(Integer.valueOf(10), task2.getResult());
        task1.unpauseTask();
        task1.join();
        Assert.assertEquals(Integer.valueOf(10), task1.getResult());
    }

    @Test
    public void maxExecutionTimeTest() throws Exception {
        Scheduler scheduler = new Scheduler(new FIFO(4));
        TaskWork t1 = new TestTaskWorkCounter(10, 1000);

        Task task = new Task(0, false, t1, 100);

        scheduler.addTask(task);
        task.unpauseTask();
        task.join();
        Assert.assertNotEquals(Integer.valueOf(10), task.getResult());

    }

    @Test
    public void manyTasks() throws Exception {
        Scheduler scheduler = new Scheduler(new Priority(4));
        Task[] tasks = new Task[100];
        for (int i = 0; i < tasks.length; i++) {
            TaskWork t1 = new TestTaskWorkCounter(10, 50);

            tasks[i] = new Task(tasks.length - 1, false, t1);
            scheduler.addTask(tasks[i]);
        }

        for (int i = 0; i < 100; i++) {
            tasks[i].join();
            Assert.assertEquals(Integer.valueOf(10), tasks[i].getResult());
        }

    }

    @Test
    public void pauseCancelTest() throws Exception {
        Scheduler scheduler = new Scheduler(new FIFO(4));
        TaskWork t1 = new TestTaskWorkCounter(10, 1000);
        Task task = new Task(0, false, t1);

        scheduler.addTask(task);
        Thread.sleep(500);
        task.pauseTask();
        task.cancelTask("");
        task.join();

    }

    @Test
    public void pauseTaskSemaphoreTest() throws Exception {
        Scheduler scheduler = new Scheduler( new FIFO(2));
        Task[] tasks = new Task[5];
        for (int i = 0; i < tasks.length; i++) {
            TaskWork t1 = new TestTaskWorkCounter(10, 500);

            tasks[i] = new Task(tasks.length - 1, false, t1);
            scheduler.addTask(tasks[i]);
        }

        for (int i = 0; i < tasks.length - 1; i++) {
            tasks[i].pauseTask();

        }
        tasks[tasks.length - 1].join();

        for (int i = 0; i < tasks.length; i++)
            tasks[i].unpauseTask();
        for (int i = 0; i < tasks.length; i++)
            tasks[i].join();

    }

    @Test
    public void startDateTimeTest() throws IllegalArgumentException, InterruptedException
    {

        Scheduler scheduler = new Scheduler( new FIFO(2));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Date now = new Date();

        Date starDate =  new Date (now.getTime()+5*1000); // 5 seconds from now;
        Task t1 = new Task(0, true, new TestTaskWorkCounter(10, 1000, ""), 0, starDate, null,"");
        scheduler.addTask(t1);
        Thread.sleep(1000*10);
        Assert.assertNotEquals(TaskState.READY_PAUSED, t1.getState());
        t1.join();
    }

    @Test
    public void endDateTimeTest() throws IllegalArgumentException, InterruptedException
    {

        Scheduler scheduler = new Scheduler( new FIFO(2));

        
        Date now = new Date();

        Date endDate =  new Date (now.getTime()+5*1000); // 5 seconds from now;
        Task t1 = new Task(0, false, new TestTaskWorkCounter(20, 1000, ""), 0, null, endDate,"");
        scheduler.addTask(t1);
        Thread.sleep(1000*10);
        Assert.assertEquals(TaskState.CANCELLED, t1.getState());
        t1.join();
    }

    @Test
    public void executionTimeTest() throws IllegalArgumentException, InterruptedException
    {

        Scheduler scheduler = new Scheduler( new FIFO(2));
        Task t1 = new Task(0, true, new TestTaskWorkCounter(20, 1000, ""), 0, null, null,"");
        scheduler.addTask(t1);
        Thread.sleep(1000);
        Assert.assertEquals(0, t1.getExecutionTime());
        t1.unpauseTask();
        Thread.sleep(7000);
        t1.pauseTask();
        long currentExecutionTime = t1.getExecutionTime();
        Thread.sleep(7000);
        t1.unpauseTask();
        Thread.sleep(1000);
        t1.pauseTask();
        Assert.assertTrue(currentExecutionTime+7000>t1.getExecutionTime());
        
    }

}
