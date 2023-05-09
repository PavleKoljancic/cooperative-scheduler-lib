package scheduler.Resources;

public class ResourceHandlerException extends Exception{

    public ResourceHandlerException() 
    {
        super("Task cannot request resources while already holding resources!");
    }
}
