package org.jboss.as.undertow.extension;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;


/**
 * Handler responsible for adding the subsystem resource to the model
 *
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
class UndetowSubsystemAdd extends AbstractBoottimeAddStepHandler {

    static final UndetowSubsystemAdd INSTANCE = new UndetowSubsystemAdd();

    private UndetowSubsystemAdd() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (SimpleAttributeDefinition attr : UndertowRootDefinition.ATTRIBUTES) {
            attr.validateAndSet(operation, model);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
                                ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        int writeThreads = UndertowRootDefinition.WRITE_THREADS.resolveModelAttribute(context, model).asInt();
        int readThreads = UndertowRootDefinition.READ_THREADS.resolveModelAttribute(context, model).asInt();
        int workerThreads = UndertowRootDefinition.WORKER_THREADS.resolveModelAttribute(context, model).asInt();

        final ServiceTarget target = context.getServiceTarget();
        final WorkerService service = new WorkerService(writeThreads, readThreads, workerThreads);
        newControllers.add(target.addService(WebSubsystemServices.WEB, service)
                .setInitialMode(ServiceController.Mode.ON_DEMAND)
                .install());

    }
}
