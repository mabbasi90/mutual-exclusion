package ir.aos.raymond.node.processor;

import ir.aos.common.node.processor.AbstractProcessor;
import ir.aos.common.task.Task;
import ir.aos.raymond.resource.RaymondResource;
import ir.aos.raymond.resource.RaymondToken;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mohammad on 7/10/2016.
 */
public class RaymondProcessorImpl extends AbstractProcessor implements RaymondProcessor {
    private static final Logger LOG = Logger.getLogger(RaymondProcessorImpl.class);

    private List<RaymondResource> resources = new ArrayList<>();

    private Map<Integer, Boolean> resourcesNeeded = new ConcurrentHashMap<>();
    private final Object resourcesLock = new Object();

    @Override
    public void init(Properties properties) throws RemoteException {
        readConfigs(properties);
        nodesFactory.exportService(this, RaymondProcessor.class, PROCESSOR_SERVICE, exportPort);
        for (int resourceId = 0; resourceId < resourcesNum; resourceId++) {
            resources.add(new RaymondResource(resourceId, this, processorUrls, nodesFactory));
        }

        dispatcher = nodesFactory.createDispatcherClient(dispatcherUrl);
        this.start();

    }


    @Override
    public void addTask(Task task) throws InterruptedException {
        tasksQueue.put(task);
        LOG.info("Got task " + task);
    }

    private void distributedLog(String log) {
        LOG.info(log);
        dispatcher.log(log, this.id);
    }

    @Override
    public void run() {
        while (!stopped || !tasksQueue.isEmpty()) {
            Task task;
            try {
                task = tasksQueue.take();
            } catch (InterruptedException e) {
                LOG.info("RaymondProcessor " + id + " is stopped.", e);
                continue;
            }
            requestingToken(task);
            waitingForTokens(task);
            doTask(task);
            exitingCriticalSection(task);

        }
    }

    private void requestingToken(Task task) {
        distributedLog("Need raymondResources " + task.getResourcesId() + " for task " + task.getId() + ".");
        for (Integer resourceId : task.getResourcesId()) {
            resourcesNeeded.put(resourceId, false);
            RaymondResource raymondResource = resources.get(resourceId);
            boolean hasToken = raymondResource.hasToken();
            if (!hasToken) {
                boolean queueEmpty = raymondResource.isQueueEmpty();
                raymondResource.addRequest(this);
                if (queueEmpty) {
                    LOG.info("Requesting from " + raymondResource.getParent().getProcessorId() + " resource " + resourceId);
                    raymondResource.getParent().request(this.id, resourceId);
                }
            } else {
                distributedLog("Has token " + raymondResource.getId());
                resourcesNeeded.put(resourceId, true);
            }
        }
    }

    private void waitingForTokens(Task task) {
        while (resourcesNeeded.values().contains(false)) {
            try {
                LOG.info("Tokens stat " + resourcesNeeded);
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                LOG.info("Interrupted!", e);
            }
        }
        distributedLog("All Resources " + task.getResourcesId() + " acquired for task " + task.getId() + ".");
    }

    private void exitingCriticalSection(Task task) {
        synchronized (resourcesLock) {
            for (Integer resourceId : task.getResourcesId()) {
                resources.get(resourceId).resetTokenReceiveCount();
                boolean queueEmpty = resources.get(resourceId).isQueueEmpty();
                if (!queueEmpty) {
                    RaymondProcessor first;
                    try {
                        first = resources.get(resourceId).getFirst();
                        if (first != this) {
                            sendToken(first, resources.get(resourceId).getToken());
                        }
                        resources.get(resourceId).removeChild(first);
                        resources.get(resourceId).setParent(first);
                    } catch (InterruptedException e) {
                        LOG.info("Interrupted!", e);
                    }
                }
                queueEmpty = resources.get(resourceId).isQueueEmpty();
                if (!queueEmpty) {
                    RaymondProcessor parent = resources.get(resourceId).getParent();
                    parent.request(this.id, resourceId);
                    LOG.info("sent request to " + parent + " in exit.");
                }
                distributedLog("Released resource " + resourceId + ".");
                resourcesNeeded.clear();
            }
        }
        distributedLog("Released all raymondResources: " + task.getResourcesId() + ".");
    }

    private void sendToken(RaymondProcessor first, RaymondToken raymondToken) {
        distributedLog("Sending " + raymondToken + "  to " + first.getProcessorId());
        first.receiveToken(raymondToken);
    }

    private void doTask(Task task) {
        String resourcesMessages = "";
        Integer messagesSum = 0;
        for (Integer resourceId : resourcesNeeded.keySet()) {
            RaymondResource raymondResource = resources.get(resourceId);
            int receiveCount = raymondResource.getReceiveCount();
            resourcesMessages += resourceId + ":" + String.valueOf(receiveCount) + " ";
            messagesSum += receiveCount;
        }

        LOG.info("Messages sum: " + messagesSum + ", " + resourcesMessages);
        distributedLog("Started task " + task.getId() + ".");
        try {
            Thread.sleep(task.getTaskTime());
        } catch (InterruptedException e) {
            LOG.fatal("interrupted while doing my task " + task.getId() + "!!!");
        }
        distributedLog("End of task " + task.getId() + ".");
    }

    @Override
    public void request(int requesterId, int resourceId) {
        synchronized (resourcesLock) {
            distributedLog("Request from " + requesterId + " for resource " + resourceId);
            RaymondProcessor requester;
            RaymondResource raymondResource = resources.get(resourceId);
            if (this.id == requesterId) {
                requester = this;
            } else {
                requester = getChildById(requesterId, resourceId);
            }
            if (requester == null) {
                LOG.fatal("Requester is not a child!");
            }
            boolean queueEmpty = raymondResource.isQueueEmpty();
            if (raymondResource.hasToken()) {
                if (!resourcesNeeded.containsKey(resourceId) && queueEmpty) {
                    sendToken(requester, raymondResource.getToken());
                    raymondResource.removeChild(requester);
                    raymondResource.setParent(requester);
                } else {
                    raymondResource.addRequest(requester);
                }
            } else {
                raymondResource.addRequest(requester);
                if (queueEmpty) {
                    raymondResource.getParent().request(this.id, resourceId);
                }
            }
        }
        allRequestsCount.incrementAndGet();
        LOG.info("All messages after request is " + allRequestsCount);
    }

    @Override
    public void receiveToken(RaymondToken raymondToken) {
        raymondToken.incrementReceive();
        try {
            Integer resourceId = raymondToken.getId();
            RaymondProcessor first = resources.get(resourceId).getFirst();
            RaymondProcessor parent = resources.get(resourceId).getParent();
            LOG.info("received token " + resourceId + " from " + parent.getProcessorId());
            if (first == null) {
                resources.get(resourceId).setToken(raymondToken);
                return;
            }
            if (parent != this) {
                resources.get(resourceId).addChild(parent);
            }
            resources.get(resourceId).setParent(first);
            if (first != this) {
                LOG.info("send after receive to somebody else: " + first.getProcessorId());
                sendToken(first, raymondToken);
                if (!resources.get(resourceId).isQueueEmpty()) {
                    first.request(this.id, resourceId);
                }
            } else {
                distributedLog("Got resource " + resourceId);
                resources.get(resourceId).setToken(raymondToken);
                resourcesNeeded.put(resourceId, true);
                synchronized (this) {
                    this.notify();
                }
            }
            allRequestsCount.incrementAndGet();
            LOG.info("All messages after receive is " + allRequestsCount);
        } catch (InterruptedException e) {
            LOG.info("Interrupted!", e);
        }
    }


    @Override
    public int getProcessorId() {
        return id;
    }

    private RaymondProcessor getChildById(int id, int resourceId) {
        return resources.get(resourceId).getChild(id);
    }

    @Override
    public String toString() {
        return "Processor " + id;
    }

}
