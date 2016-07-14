package ir.aos.raymond.resource;

import ir.aos.common.factory.NodesFactory;
import ir.aos.raymond.node.processor.RaymondProcessor;
import ir.aos.raymond.node.processor.RaymondProcessorImpl;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Mohammad on 7/10/2016.
 */
public class RaymondResource {

    private static final Logger LOG = Logger.getLogger(RaymondResource.class);
    private Integer resourceId;
    private BlockingQueue<RaymondProcessor> processorsQueue = new LinkedBlockingQueue<>();
    private RaymondToken token = null;

    private RaymondProcessor parent;
    private Map<Integer, RaymondProcessor> children = new HashMap<>();

    public RaymondResource(int resourceId, RaymondProcessor processor, String[] processorUrls, NodesFactory nodesFactory) {
        this.resourceId = resourceId;
        int processorId = processor.getProcessorId();
        if (processorId == 0) {
            parent = processor;
            token = new RaymondToken(resourceId);
        } else {
            int parentId = (processorId - 1) / 2;
            parent = nodesFactory.createRaymondProcessorClient(processorUrls[parentId]);
        }
        int leftChildId = 2 * processorId + 1;
        if (leftChildId < processorUrls.length) {
            children.put(leftChildId, nodesFactory.createRaymondProcessorClient(processorUrls[leftChildId]));
        }
        int rightChildId = 2 * processorId + 2;
        if (rightChildId < processorUrls.length) {
            children.put(rightChildId, nodesFactory.createRaymondProcessorClient(processorUrls[rightChildId]));
        }
    }

    public synchronized void addRequest(RaymondProcessor raymondProcessor) {
        processorsQueue.add(raymondProcessor);
    }

    public synchronized boolean isQueueEmpty() {
        return processorsQueue.isEmpty();
    }

    public synchronized RaymondProcessor getFirst() throws InterruptedException {
        return processorsQueue.take();
    }

    public synchronized Integer getId() {
        return resourceId;
    }

    public synchronized boolean hasToken() {
        return token != null;
    }

    public synchronized RaymondToken getToken() {
        RaymondToken tempToken = token;
        token = null;
        return tempToken;
    }

    public synchronized RaymondProcessor getParent() {
        return parent;
    }

    public synchronized RaymondProcessor getChild(int id) {
        return children.get(id);
    }

    public synchronized void setParent(RaymondProcessor parent) {
        this.parent = parent;
    }

    public synchronized void removeChild(RaymondProcessor first) {
        children.remove(first.getProcessorId());
    }

    public synchronized void setToken(RaymondToken token) {
        this.token = token;
    }

    public synchronized void addChild(RaymondProcessor parent) {
        children.put(parent.getProcessorId(), parent);
    }


    @Override
    public String toString() {
        return "Resource " + resourceId;
    }
}
