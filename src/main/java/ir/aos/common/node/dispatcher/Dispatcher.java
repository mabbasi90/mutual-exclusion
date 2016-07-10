package ir.aos.common.node.dispatcher;

import java.io.IOException;
import java.util.Properties;

public interface Dispatcher {

    void init(Properties properties) throws IOException, InterruptedException;

    void log(String in, int processorId);
}
