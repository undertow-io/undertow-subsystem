package org.jboss.as.undertow.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.undertow.deployment.UndertowDeploymentProcessor;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
class ServerAdd extends AbstractBoottimeAddStepHandler {
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (SimpleAttributeDefinition def : ServerDefinition.ATTRIBUTES) {
            def.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
        final String name = address.getLastElement().getValue();
        final String defaultHost = ServerDefinition.DEFAULT_HOST.resolveModelAttribute(context, model).asString();
        final String servletContainer = ServerDefinition.SERVLET_CONTAINER.resolveModelAttribute(context, model).asString();


        context.addStep(new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {

                //TODO not sure if this is proper place...
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.INSTALL, Phase.INSTALL_WAR_DEPLOYMENT, new UndertowDeploymentProcessor(defaultHost, servletContainer));
            }
        }, OperationContext.Stage.RUNTIME);


        final ServiceName virutalHostServiceName = UndertowServices.SERVER.append(name);
        ServerService service = new ServerService(defaultHost);
        final ServiceBuilder<ServerService> builder = context.getServiceTarget().addService(virutalHostServiceName, service)
                .addDependency(UndertowServices.CONTAINER.append(servletContainer), ServletContainerService.class, service.getServletContainer());
                        /*.addDependencies(dependentComponents)
                        .addDependency(UndertowServices.CONTAINER.append("default"), ServletContainerService.class, service.getContainer())
                        .addDependency(SecurityDomainService.SERVICE_NAME.append(securityDomain), SecurityDomainContext.class, service.getSecurityDomainContextValue());*/

        builder.setInitialMode(ServiceController.Mode.ACTIVE);

        final ServiceController<ServerService> serviceController = builder.install();
        if (newControllers != null) {
            newControllers.add(serviceController);
        }
    }
}
