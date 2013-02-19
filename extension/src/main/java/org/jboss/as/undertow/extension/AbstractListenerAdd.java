package org.jboss.as.undertow.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.undertow.extension.AbstractListenerResourceDefinition.BUFFER_POOL;
import static org.jboss.as.undertow.extension.AbstractListenerResourceDefinition.SOCKET_BINDING;
import static org.jboss.as.undertow.extension.AbstractListenerResourceDefinition.WORKER;

import java.util.List;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.network.SocketBinding;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.xnio.Pool;
import org.xnio.XnioWorker;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
abstract class AbstractListenerAdd extends AbstractAddStepHandler {
    private AbstractListenerResourceDefinition listenerDefinition;
    protected String name;
    protected String bindingRef;
    protected String workerName;
    protected String bufferPoolName;


    AbstractListenerAdd(AbstractListenerResourceDefinition definition) {
        this.listenerDefinition = definition;
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (SimpleAttributeDefinition attr : listenerDefinition.getAttributes()) {
            attr.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
        name = address.getLastElement().getValue();
        bindingRef = SOCKET_BINDING.resolveModelAttribute(context, model).asString();
        workerName = WORKER.resolveModelAttribute(context, model).asString();
        bufferPoolName = BUFFER_POOL.resolveModelAttribute(context, model).asString();
        installService(context, model, verificationHandler, newControllers);
    }

    protected void addDefaultDependencies(ServiceBuilder<? extends AbstractListenerService> serviceBuilder, AbstractListenerService service) {
        serviceBuilder.addDependency(UndertowServices.WORKER.append(workerName), XnioWorker.class, service.getWorker())
                .addDependency(SocketBinding.JBOSS_BINDING_NAME.append(bindingRef), SocketBinding.class, service.getBinding())
                .addDependency(UndertowServices.CONTAINER.append("default"), ServletContainerService.class, service.getContainer())
                .addDependency(UndertowServices.BUFFER_POOL.append(bufferPoolName), Pool.class, service.getBufferPool());

    }

    abstract ServiceName constructServiceName(final String name);

    abstract void installService(OperationContext context, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException;

}
