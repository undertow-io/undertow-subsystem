package org.jboss.as.undertow.extension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
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
import org.jboss.as.undertow.deployment.WarAnnotationDeploymentProcessor;
import org.jboss.as.undertow.deployment.WarClassloadingDependencyProcessor;
import org.jboss.as.undertow.deployment.WarDeploymentInitializingProcessor;
import org.jboss.as.undertow.deployment.WarDeploymentProcessor;
import org.jboss.as.undertow.deployment.WarMetaDataProcessor;
import org.jboss.as.undertow.deployment.WarStructureDeploymentProcessor;
import org.jboss.as.undertow.deployment.WebComponentProcessor;
import org.jboss.as.undertow.deployment.WebFragmentParsingDeploymentProcessor;
import org.jboss.as.undertow.deployment.WebJBossAllParser;
import org.jboss.as.undertow.deployment.WebParsingDeploymentProcessor;
import org.jboss.as.web.SharedTldsMetaDataBuilder;
import org.jboss.as.web.WebExtension;
import org.jboss.dmr.ModelNode;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
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

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performBoottime(OperationContext context, ModelNode operation, final ModelNode model,
                                ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        //TODO: where should we put this, this does not seem the correct place for it
        try {
            Class.forName("org.apache.jasper.compiler.JspRuntimeContext", true, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            UndertowLogger.ROOT_LOGGER.couldNotInitJsp(e);
        }

        context.addStep(new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {

                final SharedWebMetaDataBuilder sharedWebBuilder = new SharedWebMetaDataBuilder(model.clone());
                final SharedTldsMetaDataBuilder sharedTldsBuilder;
                try {
                    //horrible hack alert, we need to fix this in the AS
                    final Constructor<SharedTldsMetaDataBuilder> declaredConstructor = SharedTldsMetaDataBuilder.class.getDeclaredConstructor(ModelNode.class);
                    declaredConstructor.setAccessible(true);
                    sharedTldsBuilder = declaredConstructor.newInstance(model.clone());
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, Phase.STRUCTURE_REGISTER_JBOSS_ALL_XML_PARSER, new JBossAllXmlParserRegisteringProcessor<JBossWebMetaData>(WebJBossAllParser.ROOT_ELEMENT, WebJBossAllParser.ATTACHMENT_KEY, new WebJBossAllParser()));
                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, Phase.STRUCTURE_WAR_DEPLOYMENT_INIT, new WarDeploymentInitializingProcessor());
                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, Phase.STRUCTURE_WAR, new WarStructureDeploymentProcessor(sharedWebBuilder.create(), sharedTldsBuilder));
                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_DEPLOYMENT, new WebParsingDeploymentProcessor());
                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_DEPLOYMENT_FRAGMENT, new WebFragmentParsingDeploymentProcessor());
                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_JBOSS_WEB_DEPLOYMENT, new JBossWebParsingDeploymentProcessor());
                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_ANNOTATION_WAR, new WarAnnotationDeploymentProcessor());
                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_EAR_CONTEXT_ROOT, new EarContextRootProcessor());
                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_MERGE_METADATA, new WarMetaDataProcessor());
                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_MERGE_METADATA + 1, new TldParsingDeploymentProcessor()); //todo: fix priority
                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_MERGE_METADATA + 2, new WebComponentProcessor()); //todo: fix priority

                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, Phase.DEPENDENCIES_WAR_MODULE, new WarClassloadingDependencyProcessor());

                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, Phase.POST_MODULE_EL_EXPRESSION_FACTORY, new ELExpressionFactoryProcessor());

                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.INSTALL, Phase.INSTALL_SERVLET_INIT_DEPLOYMENT, new ServletContainerInitializerDeploymentProcessor());
                processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.INSTALL, Phase.INSTALL_WAR_DEPLOYMENT, new WarDeploymentProcessor("default"));
            }
        }, OperationContext.Stage.RUNTIME);

        UndertowContainerService container = new UndertowContainerService();
        final ServiceTarget target = context.getServiceTarget();
        newControllers.add(target.addService(WebSubsystemServices.CONTAINER.append("default"), container)
                .setInitialMode(Mode.ON_DEMAND)
                .install());
    }
}
