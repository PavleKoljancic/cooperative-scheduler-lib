package scheduler;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import scheduler.Resources.ResourceHandlerException;
import scheduler.SchedulingAlgorithms.Priority.PreemptivePriority;
import scheduler.Task.Task;
import scheduler.Task.TaskWork;
import scheduler.Task.WorkInstance;
import scheduler.Task.State.TaskState;

public class SchdulerWithResourcesTest {
    @Test
    public void Test1() throws IllegalArgumentException, InterruptedException {
        Scheduler scheduler = new Scheduler(new PreemptivePriority(2));
        HashSet<String> t1Resources = new HashSet<>();
        t1Resources.add("R1");
        Task t1 = new Task(5, false, new TestTaskWorkResources(t1Resources), 0);
        scheduler.addTask(t1);
        HashSet<String> t2Resources = new HashSet<>();
        t2Resources.add("R1");
        Task t2 = new Task(0, false, new TestTaskWorkResources(t2Resources), 0);
        Thread.sleep(1000);
        HashSet<String> t3Resources = new HashSet<>();
        t3Resources.add("R1");
        t3Resources.add("R2");
        Task t3 = new Task(1, false, new TestTaskWorkResources(t3Resources), 0);

        scheduler.addTask(t2);
        scheduler.addTask(t3);
        Thread.sleep(1000);

        Assert.assertEquals(TaskState.EXECUTING_WITH_RESOURCES, t1.getState());
        Assert.assertEquals(t2.getPriority(), t1.getPriority());
        Assert.assertEquals(TaskState.WAITING_FOR_RESOURCES, t2.getState());
        Assert.assertEquals(TaskState.WAITING_FOR_RESOURCES, t3.getState());
        t1.join();
        Thread.sleep(1000);
        Assert.assertEquals(TaskState.EXECUTING_WITH_RESOURCES, t2.getState());
        Assert.assertEquals(TaskState.WAITING_FOR_RESOURCES, t3.getState());
        t2.join();
        Assert.assertEquals(TaskState.EXECUTING_WITH_RESOURCES, t3.getState());
    }

    @Test
    public void Test2() throws IllegalArgumentException, InterruptedException {
        Scheduler scheduler = new Scheduler(new PreemptivePriority(2));
        HashSet<String> t1Resources = new HashSet<>();
        t1Resources.add("R1");
        Task t1 = new Task(5, false, new TestTaskWorkResources(t1Resources), 0);
        scheduler.addTask(t1);
        HashSet<String> t2Resources = new HashSet<>();
        t2Resources.add("R2");
        Task t2 = new Task(0, false, new TestTaskWorkResources(t2Resources), 0);

        scheduler.addTask(t2);

        Thread.sleep(1000);

        Assert.assertEquals(TaskState.EXECUTING_WITH_RESOURCES, t1.getState());
        Assert.assertEquals(TaskState.EXECUTING_WITH_RESOURCES, t2.getState());

    }

    @Test
    public void Test3() throws IllegalArgumentException, InterruptedException {
        Scheduler scheduler = new Scheduler(new PreemptivePriority(2));
        HashSet<String> t1Resources = new HashSet<>();
        t1Resources.add("R1");
        Task t1 = new Task(5, false, new TestTaskWorkResources(t1Resources), 0);
        scheduler.addTask(t1);
        HashSet<String> t2Resources = new HashSet<>();
        t2Resources.add("R2");
        Task t2 = new Task(2, false, new TestTaskWorkResources(t2Resources), 0);

        scheduler.addTask(t2);

        Task t3 = new Task(0, false, new TestTaskWorkCounter(10, 1000), 0);
        Task t4 = new Task(0, false, new TestTaskWorkCounter(10, 1000), 0);
        scheduler.addTask(t3);
        scheduler.addTask(t4);
        Thread.sleep(1000);
        Assert.assertEquals(TaskState.WAITING_WITH_RESOURCES, t1.getState());
        Assert.assertEquals(TaskState.WAITING_WITH_RESOURCES, t2.getState());

    }

    @Test
    public void Test4() throws IllegalArgumentException, InterruptedException {
        Scheduler scheduler = new Scheduler(new PreemptivePriority(2));

        Task t1 = new Task(5, false, new TaskWork() {
            Exception temp = null;

            @Override
            protected void Work(WorkInstance instance) {
                HashSet<String> r = new HashSet<>();
                r.add("R1");

                try {
                    myResourceHandler.requestResources(r, myTask);
                } catch (ResourceHandlerException e) {

                    e.printStackTrace();
                }

                r.add("R2");
                try {
                    myResourceHandler.requestResources(r, myTask);
                } catch (ResourceHandlerException e) {
                    temp = e;
                    e.printStackTrace();
                }
            }

            @Override
            public Object Result() {
                return temp;
            }

            @Override
            public String[] getArgs() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getArgs'");
            }

        }, 0);
        scheduler.addTask(t1);
        t1.join();
        Assert.assertTrue(t1.getResult() instanceof ResourceHandlerException);

    }

    @Test
    public void Test5() throws IllegalArgumentException, InterruptedException {
        Scheduler scheduler = new Scheduler(new PreemptivePriority(2));

        Task t1 = new Task(5, false, new TaskWork() {
            Exception temp = null;

            @Override
            protected void Work(WorkInstance instance) {
                HashSet<String> r = new HashSet<>();
                r.add("R1");

                try {
                    myResourceHandler.requestResources(r, myTask);
                } catch (ResourceHandlerException e) {

                    e.printStackTrace();
                }

                myResourceHandler.releaseResources(myTask);
                r.add("R2");
                try {
                    myResourceHandler.requestResources(r, myTask);
                } catch (ResourceHandlerException e) {
                    temp = e;
                    e.printStackTrace();
                }
            }

            @Override
            public Object Result() {
                return temp;
            }

            @Override
            public String[] getArgs() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getArgs'");
            }

        }, 0);
        scheduler.addTask(t1);
        t1.join();
        Assert.assertNull(t1.getResult());

    }

    @Test
    public void Test6() throws IllegalArgumentException, InterruptedException {
        Scheduler scheduler = new Scheduler(new PreemptivePriority(2));

        Task t1 = new Task(5, false, new TaskWork() {
            HashSet<String> lockedResources = null;

            @Override
            protected void Work(WorkInstance instance) {
                HashSet<String> r = new HashSet<>();
                r.add("R1");
                r.add("R2");
                try {
                    myResourceHandler.requestResources(r, myTask);
                } catch (ResourceHandlerException e) {

                    e.printStackTrace();
                }
                r.add("R3");
                lockedResources = myResourceHandler.getLockedResources(myTask);

            }

            @Override
            public Object Result() {
                return lockedResources;
            }

            @Override
            public String[] getArgs() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getArgs'");
            }

        }, 0);
        scheduler.addTask(t1);
        t1.join();

        HashSet<String> result = (HashSet<String>) t1.getResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("R1"));
        Assert.assertTrue(result.contains("R2"));
        Assert.assertFalse(result.contains("R3"));
    }

    @Test
    public void pauseAttempt() throws IllegalArgumentException, InterruptedException {
        Scheduler scheduler = new Scheduler(new PreemptivePriority(2));
        HashSet<String> t1Resources = new HashSet<>();
        t1Resources.add("R1");
        Task t1 = new Task(5, false, new TestTaskWorkResources(t1Resources), 0);
        scheduler.addTask(t1);
        Thread.sleep(1000);
        Exception temp = null;
        try {
            t1.pauseTask();
        } catch (Exception e) {
            temp = e;
        }
        Assert.assertTrue(temp != null);
        Assert.assertTrue(temp instanceof IllegalStateException);
        Assert.assertEquals(TaskState.EXECUTING_WITH_RESOURCES, t1.getState());

    }

    @Test
    public void cancelTest() throws IllegalArgumentException, InterruptedException {
        Scheduler scheduler = new Scheduler(new PreemptivePriority(2));
        HashSet<String> t1Resources = new HashSet<>();
        t1Resources.add("R1");
        Task t1 = new Task(5, false, new TestTaskWorkResources(t1Resources), 0);
        scheduler.addTask(t1);
        Thread.sleep(500);
        HashSet<String> t2Resources = new HashSet<>();
        t2Resources.add("R1");
        Task t2 = new Task(5, false, new TestTaskWorkResources(t2Resources), 0);
        scheduler.addTask(t2);
        Assert.assertEquals(TaskState.EXECUTING_WITH_RESOURCES, t1.getState());
        t1.cancelTask("By user");
        Thread.sleep(10000);
        Assert.assertEquals(TaskState.CANCELLED, t1.getState());
        Assert.assertEquals(TaskState.EXECUTING_WITH_RESOURCES, t2.getState());
    }
}
