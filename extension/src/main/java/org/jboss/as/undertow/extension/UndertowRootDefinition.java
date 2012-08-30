package org.jboss.as.undertow.extension;

import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.global.WriteAttributeHandlers;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class UndertowRootDefinition extends SimpleResourceDefinition {
    public static final UndertowRootDefinition INSTANCE = new UndertowRootDefinition();

    protected static final SimpleAttributeDefinition WORKER_THREADS =
            new SimpleAttributeDefinitionBuilder(Constants.WORKER_THREADS, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setDefaultValue(new ModelNode(10))
                    .build();

    protected static final SimpleAttributeDefinition READ_THREADS =
            new SimpleAttributeDefinitionBuilder(Constants.READ_THREADS, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setDefaultValue(new ModelNode(4))
                    .build();
    protected static final SimpleAttributeDefinition WRITE_THREADS =
            new SimpleAttributeDefinitionBuilder(Constants.WRITE_THREADS, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setDefaultValue(new ModelNode(4))
                    .build();

    protected static SimpleAttributeDefinition[] ATTRIBUTES = {WORKER_THREADS, READ_THREADS, WRITE_THREADS};

    private UndertowRootDefinition() {
        super(UndertowExtension.SUBSYSTEM_PATH,
                UndertowExtension.getResourceDescriptionResolver(null),
                UndetowSubsystemAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        for (SimpleAttributeDefinition attr : ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attr, null, new WriteAttributeHandlers.AttributeDefinitionValidatingHandler(attr));
        }
    }


}
