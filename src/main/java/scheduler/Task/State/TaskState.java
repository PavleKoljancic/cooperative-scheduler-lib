package scheduler.Task.State;

public enum TaskState {
    READY,EXECUTING,WAITING,READY_PAUSED,EXECUTION_PAUSED,FINISHED,CANCELLED,EXECUTING_WITH_RESOURCES,WAITING_WITH_RESOURCES,WAITING_FOR_RESOURCES;

    public boolean canBeScheduled()
    {
        if(this==TaskState.READY||this == TaskState.WAITING||this==WAITING_WITH_RESOURCES)
            return true;
        return false;
    }
    //Effectively checks if it is in one of the 2 final states
    public boolean isStateChangePossible()
    {
        if(this==TaskState.FINISHED||this == TaskState.CANCELLED)
            return false;
        return true;
    }

    public boolean isExecutingState()
    {
        return this==TaskState.EXECUTING||this==TaskState.EXECUTING_WITH_RESOURCES;
    }

    public boolean withResources() 
    {
        return this==TaskState.EXECUTING_WITH_RESOURCES||this==TaskState.WAITING_FOR_RESOURCES||this==TaskState.WAITING_WITH_RESOURCES;
    }

}
