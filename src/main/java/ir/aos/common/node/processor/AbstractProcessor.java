package ir.aos.common.node.processor;

import ir.aos.common.factory.NodesFactory;
import ir.aos.common.node.dispatcher.Dispatcher;
import ir.aos.common.task.Task;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Mohammad on 7/10/2016.
 */
public abstract class AbstractProcessor extends Thread{
    private static final String RESOURCES_NUM = "resources_num";
    private static final String PROCESSORS_URL = "processors_url";
    private static final String DISPATCHER_URL = "dispatcher_url";
    private static final String PROCESSOR_ID = "processor_id";
    private static final String URL_SPLITTER = ",";
    private static final String EXPORT_PORT = "export_port";
    public static final String PROCESSOR_SERVICE = "ProcessorService";

    protected Integer resourcesNum;
    protected Integer processorsNum;
    protected int id;

    protected Dispatcher dispatcher;

    protected BlockingQueue<Task> tasksQueue = new LinkedBlockingQueue<>();
    protected String[] processorUrls;
    protected String dispatcherUrl;
    protected NodesFactory nodesFactory;
    protected int exportPort;

    protected boolean stopped = false;

    protected void readConfigs(Properties properties) {
        resourcesNum = Integer.valueOf(properties.getProperty(RESOURCES_NUM));
        processorUrls = properties.getProperty(PROCESSORS_URL).split(URL_SPLITTER);
        processorsNum = processorUrls.length;
        dispatcherUrl = properties.getProperty(DISPATCHER_URL);
        exportPort = Integer.valueOf(properties.getProperty(EXPORT_PORT));
        id = Integer.valueOf(properties.getProperty(PROCESSOR_ID));
    }

    public void setNodesFactory(NodesFactory nodesFactory) {
        this.nodesFactory = nodesFactory;
    }

}
