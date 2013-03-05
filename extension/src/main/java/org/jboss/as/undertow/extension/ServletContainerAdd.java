package org.jboss.as.undertow.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.jbossallxml.JBossAllXmlParserRegisteringProcessor;
import org.jboss.as.undertow.deployment.ELExpressionFactoryProcessor;
import org.jboss.as.undertow.deployment.EarContextRootProcessor;
import org.jboss.as.undertow.deployment.JBossWebParsingDeploymentProcessor;
import org.jboss.as.undertow.deployment.ServletContainerInitializerDeploymentProcessor;
import org.jboss.as.undertow.deployment.TldParsingDeploymentProcessor;
import org.jboss.as.undertow.deployment.UndertowDependencyProcessor;
import org.jboss.as.undertow.deployment.UndertowWebSocketDeploymentProcessor;
import org.jboss.as.undertow.deployment.WarAnnotationDeploymentProcessor;
import org.jboss.as.undertow.deployment.WarDeploymentInitializingProcessor;
import org.jboss.as.undertow.deployment.WarMetaDataProcessor;
import org.jboss.as.undertow.deployment.WarStructureDeploymentProcessor;
import org.jboss.as.undertow.deployment.WebFragmentParsingDeploymentProcessor;
import org.jboss.as.undertow.deployment.WebJBossAllParser;
import org.jboss.as.undertow.deployment.WebParsingDeploymentProcessor;
import org.jboss.as.web.common.SharedTldsMetaDataBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
final class ServletContainerAdd extends AbstractBoottimeAddStepHandler {
    static final ServletContainerAdd INSTANCE = new ServletContainerAdd();

    ServletContainerAdd() {
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (AttributeDefinition def : ServletContainerDefinition.INSTANCE.getAttributes()) {
            def.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, final ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
        String name = address.getLastElement().getValue();

        context.addStep(new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {

                final SharedWebMetaDataBuilder sharedWebBuilder = new SharedWebMetaDataBuilder(model.clone());
                final SharedTldsMetaDataBuilder sharedTldsBuilder = new SharedTldsMetaDataBuilder(model.clone());

                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, Phase.STRUCTURE_REGISTER_JBOSS_ALL_XML_PARSER, new JBossAllXmlParserRegisteringProcessor<JBossWebMetaData>(WebJBossAllParser.ROOT_ELEMENT, WebJBossAllParser.ATTACHMENT_KEY, new WebJBossAllParser()));
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, Phase.STRUCTURE_WAR_DEPLOYMENT_INIT, new WarDeploymentInitializingProcessor());
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, Phase.STRUCTURE_WAR, new WarStructureDeploymentProcessor(sharedWebBuilder.create(), sharedTldsBuilder));
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_DEPLOYMENT, new WebParsingDeploymentProcessor());
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_DEPLOYMENT_FRAGMENT, new WebFragmentParsingDeploymentProcessor());
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_JBOSS_WEB_DEPLOYMENT, new JBossWebParsingDeploymentProcessor());
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_ANNOTATION_WAR, new WarAnnotationDeploymentProcessor());
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_EAR_CONTEXT_ROOT, new EarContextRootProcessor());
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_MERGE_METADATA, new WarMetaDataProcessor());
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_MERGE_METADATA + 1, new TldParsingDeploymentProcessor()); //todo: fix priority
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_MERGE_METADATA + 2, new org.jboss.as.undertow.deployment.WebComponentProcessor()); //todo: fix priority

                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, Phase.DEPENDENCIES_WAR_MODULE, new UndertowDependencyProcessor());

                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, Phase.POST_MODULE_EL_EXPRESSION_FACTORY, new ELExpressionFactoryProcessor());
                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, Phase.POST_MODULE_EL_EXPRESSION_FACTORY+1, new UndertowWebSocketDeploymentProcessor());

                processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.INSTALL, Phase.INSTALL_SERVLET_INIT_DEPLOYMENT, new ServletContainerInitializerDeploymentProcessor());
                // TODO registered in ServerAdd, we should probably reverse dependencies
                //processorTarget.addDeploymentProcessor(UndertowExtension.SUBSYSTEM_NAME, Phase.INSTALL, Phase.INSTALL_WAR_DEPLOYMENT, new UndertowDeploymentProcessor("default"));

            }
        }, OperationContext.Stage.RUNTIME);

        ServletContainerService container = new ServletContainerService();
        final ServiceTarget target = context.getServiceTarget();
        newControllers.add(target.addService(UndertowServices.CONTAINER.append(name), container)
                .setInitialMode(ServiceController.Mode.ON_DEMAND)
                .install());

    }
}
