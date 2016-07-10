package ir.aos.common.factory;

import ir.aos.common.node.dispatcher.Dispatcher;
import ir.aos.common.node.dispatcher.DispatcherImpl;
import ir.aos.raymond.node.processor.RaymondProcessor;
import ir.aos.raymond.node.processor.RaymondProcessorImpl;
import ir.aos.singhal.node.processor.SinghalProcessor;
import ir.aos.singhal.node.processor.SinghalProcessorImpl;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.remoting.rmi.RmiServiceExporter;

import java.rmi.RemoteException;

public class RMIClientNodesFactory implements NodesFactory {

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
        RmiProxyFactoryBean factory = new RmiProxyFactoryBean();
        factory.setServiceInterface(SinghalProcessor.class);
        factory.setServiceUrl("rmi://" + serverUrl + "/" + SinghalProcessorImpl.PROCESSOR_SERVICE);
        factory.setLookupStubOnStartup(false);
        factory.afterPropertiesSet();
        return (SinghalProcessor) factory.getObject();
    }

    @Override
    public RaymondProcessor createRaymondProcessorClient(String serverUrl) {
        RmiProxyFactoryBean factory = new RmiProxyFactoryBean();
        factory.setServiceInterface(SinghalProcessor.class);
        factory.setServiceUrl("rmi://" + serverUrl + "/" + RaymondProcessorImpl.PROCESSOR_SERVICE);
        factory.setLookupStubOnStartup(false);
        factory.afterPropertiesSet();
        return (RaymondProcessor) factory.getObject();
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
