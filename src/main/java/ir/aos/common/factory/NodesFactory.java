package ir.aos.common.factory;

import ir.aos.common.node.dispatcher.Dispatcher;
import ir.aos.raymond.node.processor.RaymondProcessor;
import ir.aos.singhal.node.processor.SinghalProcessor;

import java.rmi.RemoteException;

public interface NodesFactory {

    Dispatcher createDispatcherClient(String serverUrl);

    SinghalProcessor createSinghalProcessorClient(String serverUrl);

    RaymondProcessor createRaymondProcessorClient(String serverUrl);

    void exportService(Object object, Class classType, String serviceName, int port) throws RemoteException;
}
