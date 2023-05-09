package scheduler.SchedulingAlgorithms;

import scheduler.Task.Task;

public interface SchedulingAlgorithm {
    
    /*
     * returns a task and remove 
     * it from the queue if any are ready
     * otherwise return null
     */
    public Task getNextTask();
    /*
     * trys to add a task to the queue
     * if successful return true
     * otherwise false
     */
    public boolean add(Task t);
    /*
     * trys to remove a task to the queue
     * if successful return true
     * otherwise false
     */
    public boolean remove(Task t);

    /*
     * returns the maximum number of tasks 
     * that can be executed concurrently 
     *      
    */
    public int getCapacity();
}
