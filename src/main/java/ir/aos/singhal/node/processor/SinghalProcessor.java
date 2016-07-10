package ir.aos.singhal.node.processor;

import ir.aos.common.node.processor.Processor;
import ir.aos.singhal.resource.SinghalToken;

public interface SinghalProcessor extends Processor {

    void request(int id, int reqNum,int resourceId);

    void stopProcessor();

    void receiveToken(SinghalToken singhalToken);
}
