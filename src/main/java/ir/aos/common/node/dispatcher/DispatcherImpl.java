package ir.aos.common.node.dispatcher;

import ir.aos.common.input.TasksInputReader;
import ir.aos.common.node.processor.Processor;
import ir.aos.common.task.Task;
import ir.aos.common.factory.NodesFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DispatcherImpl implements Dispatcher {
    public static final Logger LOG = Logger.getLogger(DispatcherImpl.class);
    public static final String PROCESSORS_URL = "processors_url";
    public static final String URL_SPLITTER = ",";
    public static final String INPUT_FILE = "input_file";
    public static final String EXPORT_PORT = "export_port";
    public static final String DISPATCHER_SERVICE = "DispatcherService";

    private NodesFactory nodesFactory;
    private List<Processor> processors = new ArrayList<>();
    private TasksInputReader tasksInputReader;

    private String[] processorUrls;
    private String inputFile;
    private Integer exportPort;

    @Override
    public void init(Properties properties) throws IOException, InterruptedException {
        readConfigs(properties);
        nodesFactory.exportService(this, Dispatcher.class, DISPATCHER_SERVICE, exportPort);
        List<Task> tasks = tasksInputReader.parseInput(inputFile);

        for (String processorUrl : processorUrls) {
            processors.add(nodesFactory.createSinghalProcessorClient(processorUrl));
        }

        for (Task task : tasks) {
            Processor processor = processors.get(task.getProcessorId());
            processor.addTask(task);
        }

    }

    @Override
    public void log(String in, int processorId) {
        LOG.info("Processor " + processorId + "'s log: " + in);
    }

    public void setTasksInputReader(TasksInputReader tasksInputReader) {
        this.tasksInputReader = tasksInputReader;
    }

    private void readConfigs(Properties properties) {
        processorUrls = properties.getProperty(PROCESSORS_URL).split(URL_SPLITTER);
        inputFile = properties.getProperty(INPUT_FILE);
        exportPort = Integer.valueOf(properties.getProperty(EXPORT_PORT));
    }

    public void setNodesFactory(NodesFactory nodesFactory) {
        this.nodesFactory = nodesFactory;
    }
}
