package ir.aos.raymond.resource;

import ir.aos.raymond.node.processor.RaymondProcessor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Mohammad on 7/10/2016.
 */
public class RaymondlResource {

    private Integer id;
    private BlockingQueue<RaymondProcessor> raymondProcessors = new LinkedBlockingQueue<>();

    public RaymondlResource(int resourceId) {
        this.id = resourceId;
    }
}
