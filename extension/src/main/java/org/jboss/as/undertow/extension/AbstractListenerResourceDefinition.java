package org.jboss.as.undertow.extension;

import java.util.Arrays;

import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimplePersistentResourceDefinition;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
abstract class AbstractListenerResourceDefinition extends SimplePersistentResourceDefinition {
    protected static final SimpleAttributeDefinition SOCKET_BINDING = new SimpleAttributeDefinitionBuilder(Constants.SOCKET_BINDING, ModelType.STRING)
            .setAllowNull(false)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setValidator(new StringLengthValidator(1))
            .build();

    protected static final SimpleAttributeDefinition WORKER = new SimpleAttributeDefinitionBuilder(Constants.WORKER, ModelType.STRING)
            .setAllowNull(true)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setValidator(new StringLengthValidator(1))
            .build();

    protected static final SimpleAttributeDefinition BUFFER_POOL = new SimpleAttributeDefinitionBuilder(Constants.BUFFER_POOL, ModelType.STRING)
            .setAllowNull(true)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setValidator(new StringLengthValidator(1))
            .build();

    protected static SimpleAttributeDefinition[] ATTRIBUTES = {SOCKET_BINDING, WORKER, BUFFER_POOL};

    public AbstractListenerResourceDefinition(PathElement pathElement, OperationStepHandler addHandler) {
        super(pathElement, UndertowExtension.getResolver(pathElement.getKey()), addHandler, ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public SimpleAttributeDefinition[] getAttributes() {
        return ATTRIBUTES;
    }


    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        OperationStepHandler handler = new ReloadRequiredWriteAttributeHandler(ATTRIBUTES);
        for (SimpleAttributeDefinition attr : getAttributes()) {
            resourceRegistration.registerReadWriteAttribute(attr, null, handler);
        }
    }
}
