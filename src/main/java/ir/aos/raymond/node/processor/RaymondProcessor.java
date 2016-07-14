package ir.aos.raymond.node.processor;

import ir.aos.common.node.processor.Processor;
import ir.aos.raymond.resource.RaymondToken;

/**
 * Created by Mohammad on 7/10/2016.
 */
public interface RaymondProcessor extends Processor {
    void request(int id, int resourceId);

    void receiveToken(RaymondToken raymondToken);

    int getProcessorId();
}
