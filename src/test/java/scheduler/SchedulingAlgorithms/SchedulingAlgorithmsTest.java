package scheduler.SchedulingAlgorithms;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import scheduler.SchedulingAlgorithms.FIFO.FIFO;
import scheduler.SchedulingAlgorithms.Priority.Priority;
import scheduler.Task.Task;
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
}
