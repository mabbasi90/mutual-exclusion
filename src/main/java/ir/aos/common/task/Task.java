package ir.aos.common.task;

import java.io.Serializable;
import java.util.List;

public class Task implements Serializable {

    public static final String TEXT_SPLITTER = " ";
    private int taskId;
    private int processorId;
    private int taskTime;
    private List<Integer> resourcesId;

    public Task(int taskId, int processorId, int taskTime, List<Integer> resourcesId) {
        this.taskId = taskId;
        this.processorId = processorId;
        this.taskTime = taskTime;
        this.resourcesId = resourcesId;
    }

    public int getId() {
        return taskId;
    }

    public int getProcessorId() {
        return processorId;
    }

    public int getTaskTime() {
        return taskTime;
    }

    public List<Integer> getResourcesId() {
        return resourcesId;
    }

    @Override
    public String toString() {
        return taskId + TEXT_SPLITTER + processorId + TEXT_SPLITTER + taskTime + TEXT_SPLITTER + resourcesId;
    }
}
