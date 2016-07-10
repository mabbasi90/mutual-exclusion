package ir.aos.common.node.processor;

import ir.aos.common.task.Task;

import java.rmi.RemoteException;
import java.util.Properties;

/**
 * Created by Mohammad on 7/10/2016.
 */
public interface Processor {
    void init(Properties properties) throws RemoteException;

    void addTask(Task task) throws InterruptedException;
}
