package ir.aos.common.factory;

import ir.aos.common.node.dispatcher.Dispatcher;
import ir.aos.common.node.dispatcher.DispatcherImpl;
import ir.aos.common.node.processor.Processor;
import ir.aos.raymond.node.processor.RaymondProcessor;
import ir.aos.raymond.node.processor.RaymondProcessorImpl;
import ir.aos.singhal.node.processor.SinghalProcessor;
import ir.aos.singhal.node.processor.SinghalProcessorImpl;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.remoting.rmi.RmiServiceExporter;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class RMIClientNodesFactory implements NodesFactory {

    private Map<String, Processor> processorMap = new HashMap<>();

    @Override
    public Dispatcher createDispatcherClient(String serverUrl) {
        RmiProxyFactoryBean factory = new RmiProxyFactoryBean();
        factory.setServiceInterface(Dispatcher.class);
        factory.setServiceUrl("rmi://" + serverUrl + "/" + DispatcherImpl.DISPATCHER_SERVICE);
        factory.setLookupStubOnStartup(false);
        factory.afterPropertiesSet();
        return (Dispatcher) factory.getObject();
    }

    @Override
    public SinghalProcessor createSinghalProcessorClient(String serverUrl) {
        if (processorMap.containsKey(serverUrl)) {
            return (SinghalProcessor) processorMap.get(serverUrl);
        }
        RmiProxyFactoryBean factory = new RmiProxyFactoryBean();
        factory.setServiceInterface(SinghalProcessor.class);
        factory.setServiceUrl("rmi://" + serverUrl + "/" + SinghalProcessorImpl.PROCESSOR_SERVICE);
        factory.setLookupStubOnStartup(false);
        factory.afterPropertiesSet();
        SinghalProcessor singhalProcessor = (SinghalProcessor) factory.getObject();
        processorMap.put(serverUrl, singhalProcessor);
        return singhalProcessor;
    }

    @Override
    public RaymondProcessor createRaymondProcessorClient(String serverUrl) {
        if (processorMap.containsKey(serverUrl)) {
            return (RaymondProcessor) processorMap.get(serverUrl);
        }
        RmiProxyFactoryBean factory = new RmiProxyFactoryBean();
        factory.setServiceInterface(RaymondProcessor.class);
        factory.setServiceUrl("rmi://" + serverUrl + "/" + RaymondProcessorImpl.PROCESSOR_SERVICE);
        factory.setLookupStubOnStartup(false);
        factory.afterPropertiesSet();
        RaymondProcessor raymondProcessor = (RaymondProcessor) factory.getObject();
        processorMap.put(serverUrl, raymondProcessor);
        return raymondProcessor;
    }

    @Override
    public void exportService(Object service, Class classType, String serviceName, int port) throws RemoteException {
        RmiServiceExporter rmiServiceExporter = new RmiServiceExporter();
        rmiServiceExporter.setService(service);
        rmiServiceExporter.setServiceInterface(classType);
        rmiServiceExporter.setReplaceExistingBinding(true);
        rmiServiceExporter.setServiceName(serviceName);
        rmiServiceExporter.setRegistryPort(port);
        rmiServiceExporter.afterPropertiesSet();
    }

}
