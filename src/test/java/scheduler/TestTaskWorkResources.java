package scheduler;

import java.util.HashSet;

import scheduler.Resources.ResourceHandlerException;
import scheduler.Task.TaskWork;
import scheduler.Task.WorkInstance;

public class TestTaskWorkResources extends TaskWork {
    HashSet<String> ResourcesToRequest;
    public TestTaskWorkResources(HashSet<String> ResourcesToRequest) throws InterruptedException {
        super();
        this.ResourcesToRequest = ResourcesToRequest;

    }

    @Override
    protected void Work(WorkInstance instance) {
        try {
            this.myResourceHandler.requestResources(ResourcesToRequest, myTask);
        } catch (ResourceHandlerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        instance.Check();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        instance.Check();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(this.myResourceHandler.hasLockedResources(myTask)) 
        {
            this.myResourceHandler.releaseResources(myTask);
        }
    }

    @Override
    public Object Result() {
        return null;

    }
    
}
