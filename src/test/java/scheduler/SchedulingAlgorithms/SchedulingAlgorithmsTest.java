package scheduler.SchedulingAlgorithms;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import scheduler.Scheduler;
import scheduler.Task.Task;
import scheduler.Task.State.TaskState;
import scheduler.TestTaskWorkCounter;
import scheduler.SchedulingAlgorithms.FIFO.FIFO;
import scheduler.SchedulingAlgorithms.FIFO.RoundRobin;
import scheduler.SchedulingAlgorithms.FIFO.RoundRobinVarible;
import scheduler.SchedulingAlgorithms.Priority.PreemptivePriority;
import scheduler.SchedulingAlgorithms.Priority.Priority;
public class SchedulingAlgorithmsTest {
    
    @Test
    public void FifoTest()
    {

        Task [] tasks = new Task[20];
        FIFO fifo = new FIFO(5);
        for(int i=0;i<tasks.length;i++)
            {tasks[i] = new Task(i, false, null);
             fifo.add(tasks[i]);
            }
        for(int i=0;i<tasks.length;i++)
            assertEquals(fifo.getNextTask(),tasks[i]);

    }
    @Test
    public void PriorityTest()
    {

        Task [] tasks = new Task[20];
        Priority priority = new Priority(5);
        for(int i=0;i<tasks.length;i++)
            {tasks[i] = new Task(tasks.length-i, false, null);
             priority.add(tasks[i]);
            }
        for(int i=0;i<tasks.length;i++)
            assertEquals(priority.getNextTask(),tasks[tasks.length-i-1]);

    }
    @Test
    public void RoundRobinTest()
    {

        Task [] tasks = new Task[20];
        FIFO fifo = new RoundRobin(5, 6000);
        for(int i=0;i<tasks.length;i++)
            {tasks[i] = new Task(i, false, null);
             fifo.add(tasks[i]);
            }
        for(int i=0;i<tasks.length;i++)
        {   
            Task current = fifo.getNextTask();
            assertEquals(current,tasks[i]);
            assertEquals(6000,current.getTimeSlice());
        }
    }
    @Test
    public void RoundRobinVairableTest()
    {

        Task [] tasks = new Task[20];
        FIFO fifo = new RoundRobinVarible(5, 6000);
        for(int i=0;i<tasks.length;i++)
            {tasks[i] = new Task(i, false, null);
             fifo.add(tasks[i]);
            }
        for(int i=0;i<tasks.length;i++)
        {   
            Task current = fifo.getNextTask();
            assertEquals(current,tasks[i]);
            assertEquals(6000/(current.getPriority()+1),current.getTimeSlice());
        }
    }
    @Test
    public void PreemptivePriorityTest() throws IllegalArgumentException, InterruptedException
    {
        Scheduler scheduler = new Scheduler(new PreemptivePriority(1) );
        Task pajo = new Task(2, false, new TestTaskWorkCounter(20, 1000,"Pajo"), 0);

        Task aco = new Task(1, false, new TestTaskWorkCounter(20, 1000,"Aco"), 0);

        Task pero = new Task(0, false, new TestTaskWorkCounter(10, 1000,"Pero"), 0);
        scheduler.addTask(pajo);
        Thread.sleep(1000);
        scheduler.addTask(aco);
        Thread.sleep(2000);
        scheduler.addTask(pero);
        pero.join();
        Assert.assertTrue(pajo.getState().canBeScheduled()&&aco.getState()!=TaskState.FINISHED);
    }
}
