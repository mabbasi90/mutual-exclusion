package ir.aos.singhal.run;

import ir.aos.common.node.dispatcher.Dispatcher;
import ir.aos.singhal.node.processor.SinghalProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class SinghalApplication {

    public static final String DISPATCHER = "dispatcher";
    public static final String PROCESSOR = "processor";

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            System.out.println("Usage: [node type(dispatcher|processor)] [input config]");
            System.exit(-1);
        }
        if (DISPATCHER.equals(args[0])) {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("dispatcherContext.xml");
            Dispatcher dispatcher = (Dispatcher) applicationContext.getBean("dispatcher");
            Properties properties = new Properties();
            properties.load(new FileInputStream(args[1]));
            dispatcher.init(properties);
        } else if (PROCESSOR.equals(args[0])) {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("processorContext.xml");
            SinghalProcessor singhalProcessor = (SinghalProcessor) applicationContext.getBean("processor");
            Properties properties = new Properties();
            properties.load(new FileInputStream(args[1]));
            singhalProcessor.init(properties);
        } else {
            System.out.println("Only dispatcher and processor nodes are supported.");
        }
    }

}
