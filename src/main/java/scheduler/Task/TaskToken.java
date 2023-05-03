package scheduler.Task;


class TaskToken {
    private boolean token;

    public boolean isTriggered() {
        return token;
    }

    public void setTriggered(boolean token) {
        this.token = token;
    }

    public TaskToken(boolean token) {
        this.token = token;
    }
    
}
