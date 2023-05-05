package scheduler.Task.State;

public enum TaskState {
    READY,EXECUTING,WAITING,NOTREADY,EXECUTIONPAUSED,FINISHED,CANCELLED;

    public boolean canBeScheduled()
    {
        if(this==TaskState.READY||this == TaskState.WAITING)
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
    public boolean isInPausedState()
    {
        if(this==TaskState.NOTREADY||this == TaskState.EXECUTIONPAUSED)
            return true;
        return false;
    }
}
