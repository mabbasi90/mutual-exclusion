package ir.aos.singhal.node.processor;

import ir.aos.common.node.processor.AbstractProcessor;
import ir.aos.common.task.Task;
import ir.aos.singhal.resource.SinghalResource;
import ir.aos.singhal.resource.SinghalToken;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class SinghalProcessorImpl extends AbstractProcessor implements SinghalProcessor {
    private static final Logger LOG = Logger.getLogger(SinghalProcessorImpl.class);

    private List<SinghalResource> singhalResources;
    private List<SinghalProcessor> singhalProcessors = new ArrayList<>();

    private boolean stopped = false;

    private Map<Integer, Boolean> resourcesNeeded = new ConcurrentHashMap<>();

    private Object resourcesLock = new Object();

    @Override
    public void init(Properties properties) throws RemoteException {
        readConfigs(properties);
        nodesFactory.exportService(this, SinghalProcessor.class, PROCESSOR_SERVICE, exportPort);
        singhalResources = new ArrayList<>();
        for (int resourceId = 0; resourceId < resourcesNum; resourceId++) {
            singhalResources.add(new SinghalResource(resourceId, id, processorsNum));
        }
        for (int i = 0; i < processorsNum; i++) {
            if (id == i) {
                singhalProcessors.add(this);
            } else {
                singhalProcessors.add(nodesFactory.createSinghalProcessorClient(processorUrls[i]));
            }
        }
        dispatcher = nodesFactory.createDispatcherClient(dispatcherUrl);
        this.start();

    }

    private void distributedLog(String log) {
        LOG.info(log);
        dispatcher.log(log, this.id);
    }

    @Override
    public void addTask(Task task) throws InterruptedException {
        tasksQueue.put(task);
        LOG.info("Got task " + task.getId());
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

            specifyRequiredResources(task);
            requestResources(task.getResourcesId());
            waitForReceivingAllTokens(task);
            doTask(task);
            exitCriticalSection(task.getResourcesId());
        }
    }

    private void specifyRequiredResources(Task task) {
        List<Integer> acquiredBefore = new ArrayList<>();
        synchronized (resourcesLock) {
            resourcesNeeded.clear();
            distributedLog("Need singhalResources " + task.getResourcesId() + " for task " + task.getId() + ".");
            for (Integer resourceId : task.getResourcesId()) {
                if (singhalResources.get(resourceId).getSinghalToken() == null) {
                    resourcesNeeded.put(resourceId, false);
                } else {
                    resourcesNeeded.put(resourceId, true);
                    acquiredBefore.add(resourceId);
                    singhalResources.get(resourceId).setState(this.id, ir.aos.singhal.node.State.E);
                }
            }
        }
        if (!acquiredBefore.isEmpty()) {
            distributedLog("Resources " + acquiredBefore + " are acquired before for task " + task.getId() + ".");
        }
    }

    private void doTask(Task task) {
        String resourcesMessages = "";
        Integer messagesSum = 0;
        for (Integer resourceId : resourcesNeeded.keySet()) {
            SinghalResource singhalResource = singhalResources.get(resourceId);
            int receiveCount = singhalResource.getSinghalToken().getReceiveCount();
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

    private void waitForReceivingAllTokens(Task task) {
        while (resourcesNeeded.values().contains(false)) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                LOG.info("Interrupted while waiting for singhalToken.", e);
            }
        }
        distributedLog("All Resources " + task.getResourcesId() + " acquired for task " + task.getId() + ".");
    }

    private void exitCriticalSection(List<Integer> resourcesId) {
        distributedLog("Releasing singhalResources " + resourcesId + ".");
        synchronized (resourcesLock) {
            for (Integer resourceId : resourcesId) {
                singhalResources.get(resourceId).setState(this.id, ir.aos.singhal.node.State.N);
                SinghalToken singhalToken = singhalResources.get(resourceId).getSinghalToken();
                singhalToken.resetReceiveCount();
                singhalToken.setState(this.id, ir.aos.singhal.node.State.N);
                updatingStatuses(resourceId);
                int j;
                for (j = 0; j < processorsNum; j++) {
                    if (!ir.aos.singhal.node.State.N.equals(singhalResources.get(resourceId).getState(j))) {
                        break;
                    }
                }
                if (j == processorsNum) {
                    singhalResources.get(resourceId).setState(this.id, ir.aos.singhal.node.State.H);
                } else {
                    for (int i = 0; i < processorsNum; i++) {
                        if (ir.aos.singhal.node.State.R.equals(singhalResources.get(resourceId).getState(i))) {
                            sendToken(i, resourceId);
                            break;
                        }
                    }
                }
                distributedLog("Released resource " + resourceId + ".");
            }
        }
        distributedLog("Released all singhalResources: " + resourcesId + ".");
    }

    private void updatingStatuses(Integer resourceId) {
        for (int j = 0; j < processorsNum; j++) {
            if (singhalResources.get(resourceId).getReqNum(j) > singhalResources.get(resourceId).getSinghalToken().getReqNum(j)) {
                singhalResources.get(resourceId).getSinghalToken().setState(j, singhalResources.get(resourceId).getState(j));
                singhalResources.get(resourceId).getSinghalToken().setReqNum(j, singhalResources.get(resourceId).getReqNum(j));
            } else {
                singhalResources.get(resourceId).setState(j, singhalResources.get(resourceId).getSinghalToken().getState(j));
                singhalResources.get(resourceId).setReqNum(j, singhalResources.get(resourceId).getSinghalToken().getReqNum(j));
            }
        }
    }

    private void requestResources(List<Integer> resourcesId) {
        for (Integer resourceId : resourcesId) {
            singhalResources.get(resourceId).setState(id, ir.aos.singhal.node.State.R);
            singhalResources.get(resourceId).incReqNum(id);
            List<Integer> rProcessors = singhalResources.get(resourceId).getProcessorsWithState(ir.aos.singhal.node.State.R);
            for (Integer rProcessor : rProcessors) {
                singhalProcessors.get(rProcessor).request(id, singhalResources.get(resourceId).getReqNum(id), resourceId);
            }
        }
    }

    @Override
    public void request(int requesterId, int reqNum, int resourceId) {
        synchronized (resourcesLock) {
            if (singhalResources.get(resourceId).getReqNum(requesterId) < reqNum) {
                singhalResources.get(resourceId).setReqNum(requesterId, reqNum);
                switch (singhalResources.get(resourceId).getState(this.id)) {
                    case N:
                        singhalResources.get(resourceId).setState(requesterId, ir.aos.singhal.node.State.R);
                        break;
                    case R:
                        if (!ir.aos.singhal.node.State.R.equals(singhalResources.get(resourceId).getState(requesterId))) {
                            singhalResources.get(resourceId).setState(requesterId, ir.aos.singhal.node.State.R);
                            singhalProcessors.get(requesterId).request(this.id, singhalResources.get(resourceId).getReqNum(this.id), resourceId);
                        }
                        break;
                    case E:
                        singhalResources.get(resourceId).setState(requesterId, ir.aos.singhal.node.State.R);
                        break;
                    case H:
                        singhalResources.get(resourceId).setState(requesterId, ir.aos.singhal.node.State.R);
                        singhalResources.get(resourceId).getSinghalToken().setState(requesterId, ir.aos.singhal.node.State.R);
                        singhalResources.get(resourceId).getSinghalToken().setReqNum(requesterId, reqNum);
                        singhalResources.get(resourceId).setState(this.id, ir.aos.singhal.node.State.N);
                        sendToken(requesterId, resourceId);
                        break;
                    default:
                        LOG.fatal("None of states are covered?!?!");
                }
            }
        }
        allRequestsCount.incrementAndGet();
        LOG.info("All messages after request is " + allRequestsCount);
    }

    @Override
    public void stopProcessor() {
        this.stopped = true;
        this.interrupt();
    }

    private void sendToken(int requesterId, int resourceId) {
        LOG.info("sending singhalToken " + resourceId + " to processor" + requesterId);
        singhalProcessors.get(requesterId).receiveToken(singhalResources.get(resourceId).getSinghalToken());
        singhalResources.get(resourceId).removeToken();
    }

    @Override
    public void receiveToken(SinghalToken singhalToken) {
        synchronized (resourcesLock) {
            singhalToken.incrementReceive();
            resourcesNeeded.put(singhalToken.getId(), true);
            singhalResources.get(singhalToken.getId()).setState(this.id, ir.aos.singhal.node.State.E);
            singhalResources.get(singhalToken.getId()).setSinghalToken(singhalToken);
            distributedLog("SinghalResource " + singhalToken.getId() + " acquired.");
            if (State.WAITING.equals(this.getState()))
                synchronized (this) {
                    this.notify();
                }
        }
        allRequestsCount.incrementAndGet();
        LOG.info("All messages after receive is " + allRequestsCount);
    }

}
