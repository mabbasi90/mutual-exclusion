package ir.aos.raymond.node.processor;

import ir.aos.common.node.processor.AbstractProcessor;
import ir.aos.common.task.Task;
import ir.aos.raymond.resource.RaymondlResource;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by Mohammad on 7/10/2016.
 */
public class RaymondProcessorImpl extends AbstractProcessor implements RaymondProcessor {
    private static final Logger LOG = Logger.getLogger(RaymondProcessorImpl.class);

    private RaymondProcessor parent;
    private Set<RaymondProcessor> children = new HashSet<>();
    private List<RaymondlResource> raymondResources;

    @Override
    public void init(Properties properties) throws RemoteException {
        readConfigs(properties);
        nodesFactory.exportService(this, RaymondProcessor.class, PROCESSOR_SERVICE, exportPort);
        raymondResources = new ArrayList<>();
        for (int resourceId = 0; resourceId < resourcesNum; resourceId++) {
            raymondResources.add(new RaymondlResource(resourceId));
        }

        // TODO setting parent and children
//        for (int i = 0; i < processorsNum; i++) {
//            if (id == i) {
//                raymondPR.add(this);
//            } else {
//                singhalProcessors.add(nodesFactory.createSinghalProcessorClient(processorUrls[i]));
//            }
//        }
        dispatcher = nodesFactory.createDispatcherClient(dispatcherUrl);
        this.start();

    }


    @Override
    public void addTask(Task task) throws InterruptedException {

    }

    @Override
    public void run() {
        while (!stopped || !tasksQueue.isEmpty()) {
            Task task;
            try {
                task = tasksQueue.take();
            } catch (InterruptedException e) {
                LOG.info("SinghalProcessor " + id + " is stopped.", e);
                continue;
            }
//
//            specifyRequiredResources(task);
//            requestResources(task.getResourcesId());
//            waitForReceivingAllTokens(task);
//            doTask(task);
//            exitCriticalSection(task.getResourcesId());
        }
    }
}
