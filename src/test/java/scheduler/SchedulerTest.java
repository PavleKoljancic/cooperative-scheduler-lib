package scheduler;


import java.util.concurrent.Semaphore;

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
        task.cancelTask();
        task.join();
        Assert.assertNotEquals(Integer.valueOf(10), task.getResult());
    }

    @Test
    public void joinCancelledTest2() throws Exception {
        Scheduler scheduler = new Scheduler(new FIFO(4));
        TaskWork t1 = new TestTaskWorkCounter(10, 1000);

        Task task = new Task(0, true, t1);

        scheduler.addTask(task);
        task.cancelTask();
        task.join();
    }

    @Test
    public void noStartTest() throws Exception {
        Scheduler scheduler = new Scheduler(new FIFO(4));
        TaskWork t1 = new TestTaskWorkCounter(10, 1000);

        Task task = new Task(0, true, t1);

        scheduler.addTask(task);
        Thread.sleep(1000);
        Assert.assertEquals(task.getState(), TaskState.NOTREADY);
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
        Assert.assertEquals(task1.getState(), TaskState.NOTREADY);
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
        task.cancelTask();
        task.join();

    }

    // Znaci ovdje hocu da dodaam vise taskova nego sto scheduler dozvoljava
    // pa onda da ih sve pauziram i dodoam novi u ready stanju
    // te da vidim hoce li ga ovaj rasporediti
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


}
