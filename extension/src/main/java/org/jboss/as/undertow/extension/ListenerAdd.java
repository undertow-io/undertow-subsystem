package org.jboss.as.undertow.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.undertow.extension.ListenerResourceDefinition.PATH;
import static org.jboss.as.undertow.extension.ListenerResourceDefinition.SOCKET_BINDING;

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
import org.xnio.ChannelListener;
import org.xnio.XnioWorker;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class ListenerAdd extends AbstractAddStepHandler {
    static final ListenerAdd INSTANCE = new ListenerAdd();

    private ListenerAdd() {

    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (SimpleAttributeDefinition attr : ListenerResourceDefinition.ATTRIBUTES) {
            attr.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
        final String name = address.getLastElement().getValue();


        final String bindingRef = SOCKET_BINDING.resolveModelAttribute(context, model).asString();
        final String path = PATH.resolveModelAttribute(context, model).asString();

        final ListenerService service = new ListenerService(path);
        final ServiceBuilder<ChannelListener> serviceBuilder = context.getServiceTarget().addService(WebSubsystemServices.LISTENER.append(name), service)
                .addDependency(WebSubsystemServices.WEB, XnioWorker.class, service.getWorker())
                .addDependency(SocketBinding.JBOSS_BINDING_NAME.append(bindingRef), SocketBinding.class, service.getBinding());

        serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE);

        final ServiceController<ChannelListener> serviceController = serviceBuilder.install();
        if (newControllers != null) {
            newControllers.add(serviceController);
        }
    }
}
