package scheduler;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import scheduler.SchedulingAlgorithms.FIFO;
import scheduler.SchedulingAlgorithms.Priority;
import scheduler.Task.Task;
import scheduler.Task.TaskWork;

public class SchedulerTest {

    @Test
    public void joinTest1() throws Exception {
        Scheduler scheduler = new Scheduler(4, new FIFO());
        TaskWork t1 = new TaskWork() {

            int i = 0;

            @Override
            public boolean Work() {
                i += 1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return i == 10;
            }

            @Override
            public Integer Result() {
                return Integer.valueOf(i);

            }
        };

        Task task = new Task(0, false, t1);

        scheduler.addTask(task);
        Assert.assertNotEquals(Integer.valueOf(10), task.getResult());
        task.join();
        Assert.assertEquals(Integer.valueOf(10), task.getResult());
    }

    @Test
    public void joinTest2() throws Exception {
        Scheduler scheduler = new Scheduler(4, new FIFO());
        TaskWork t1 = new TaskWork() {

            int i = 0;

            @Override
            public boolean Work() {
                i += 1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return i == 10;
            }

            @Override
            public Integer Result() {
                return Integer.valueOf(i);

            }
        };

        Task task = new Task(0, false, t1);

        scheduler.addTask(task);
        Assert.assertNotEquals(Integer.valueOf(10), task.getResult());
        task.cancelTask();
        task.join();
        Assert.assertNotEquals(Integer.valueOf(10), task.getResult());
    }

    @Test
    public void noStartTest() throws Exception {
        Scheduler scheduler = new Scheduler(4, new FIFO());
        TaskWork t1 = new TaskWork() {

            int i = 0;

            @Override
            public boolean Work() {
                i += 1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return i == 10;
            }

            @Override
            public Integer Result() {
                return Integer.valueOf(i);

            }
        };

        Task task = new Task(0, true, t1);

        scheduler.addTask(task);
        Assert.assertNotEquals(Integer.valueOf(10), task.getResult());
        Assert.assertNotEquals(Integer.valueOf(10), task.getResult());
    }

    @Test
    public void unPauseTest() throws Exception {
        Scheduler scheduler = new Scheduler(4, new FIFO());
        TaskWork t1 = new TaskWork() {

            int i = 0;

            @Override
            public boolean Work() {
                i += 1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return i == 10;
            }

            @Override
            public Integer Result() {
                return Integer.valueOf(i);

            }
        };

        Task task1 = new Task(0, true, t1);
        TaskWork t2 = new TaskWork() {

            int i = 0;

            @Override
            public boolean Work() {
                i += 1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return i == 10;
            }

            @Override
            public Integer Result() {
                return Integer.valueOf(i);

            }
        };
        scheduler.addTask(task1);

        Task task2 = new Task(0, false, t2);

        scheduler.addTask(task2);
        task2.join();
        Assert.assertEquals(Integer.valueOf(10), task2.getResult());
        task1.unpauseTask();
        Thread.sleep(100);
        task1.pauseTask();
        Thread.sleep(1000);
        task1.unpauseTask();
        task1.join();
        Assert.assertEquals(Integer.valueOf(10), task1.getResult());
    }

    @Test
    public void maxExecutionTimeTest() throws Exception {
        Scheduler scheduler = new Scheduler(4, new FIFO());
        TaskWork t1 = new TaskWork() {

            int i = 0;

            @Override
            public boolean Work() {
                i += 1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return i == 10;
            }

            @Override
            public Integer Result() {
                return Integer.valueOf(i);

            }
        };

        Task task = new Task(0, false, t1, 100);

        scheduler.addTask(task);
        task.pauseTask();
        task.unpauseTask();

        task.join();
        Assert.assertNotEquals(Integer.valueOf(10), task.getResult());

    }

    @Test
    public void manyTasks() throws Exception {
        Scheduler scheduler = new Scheduler(4, new Priority());
        Task[] tasks = new Task[100];
        for (int i = 0; i < tasks.length; i++) {
            TaskWork t1 = new TaskWork() {

                int i = 0;

                @Override
                public boolean Work() {
                    i += 1;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return i == 10;
                }

                @Override
                public Integer Result() {
                    return Integer.valueOf(i);

                }
            };

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
        Scheduler scheduler = new Scheduler(4, new FIFO());
        TaskWork t1 = new TaskWork() {

            int i = 0;

            @Override
            public boolean Work() {
                i += 1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return i == 10;
            }

            @Override
            public Integer Result() {
                return Integer.valueOf(i);

            }
        };

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
        Scheduler scheduler = new Scheduler(2, new FIFO());
        Task[] tasks = new Task[4];
        for (int i = 0; i < tasks.length; i++) {
            TaskWork t1 = new TaskWork() {

                int i = 0;

                @Override
                public boolean Work() {
                    i += 1;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return i == 10;
                }

                @Override
                public Integer Result() {
                    return Integer.valueOf(i);

                }
            };

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
