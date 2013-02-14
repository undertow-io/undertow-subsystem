package org.jboss.as.undertow.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.undertow.extension.AbstractListenerResourceDefinition.WORKER;
import static org.jboss.as.undertow.extension.HttpListenerResourceDefinition.SOCKET_BINDING;

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
import org.xnio.XnioWorker;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class AJPListenerAdd extends AbstractAddStepHandler {
    static final AJPListenerAdd INSTANCE = new AJPListenerAdd();

    AJPListenerAdd() {
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (SimpleAttributeDefinition attr : HttpListenerResourceDefinition.ATTRIBUTES) {
            attr.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
        final String name = address.getLastElement().getValue();

        final String bindingRef = SOCKET_BINDING.resolveModelAttribute(context, model).asString();
        final String worker = WORKER.resolveModelAttribute(context, model).asString();

        final HttpListenerService service = createService(name);
        final ServiceBuilder<HttpListenerService> serviceBuilder = context.getServiceTarget().addService(constructServiceName(name), service)
                .addDependency(UndertowServices.WORKER.append(worker), XnioWorker.class, service.getWorker())
                .addDependency(SocketBinding.JBOSS_BINDING_NAME.append(bindingRef), SocketBinding.class, service.getBinding())
                .addDependency(UndertowServices.CONTAINER.append("default"), UndertowContainerService.class, service.getContainer());

        additionalDependencies(context, serviceBuilder, model, service);
        serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE);

        final ServiceController<HttpListenerService> serviceController = serviceBuilder.install();
        if (newControllers != null) {
            newControllers.add(serviceController);
        }
    }

    protected ServiceName constructServiceName(final String name) {
        return UndertowServices.HTTP_LISTENER.append(name);
    }

    protected HttpListenerService createService(final String name) {
        return new HttpListenerService();
    }

    protected void additionalDependencies(OperationContext context, ServiceBuilder<HttpListenerService> serviceBuilder, ModelNode model, HttpListenerService service) throws OperationFailedException {
    }
}
