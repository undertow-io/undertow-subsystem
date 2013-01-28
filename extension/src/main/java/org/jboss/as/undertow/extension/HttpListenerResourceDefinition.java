package org.jboss.as.undertow.extension;

import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class HttpListenerResourceDefinition extends SimpleResourceDefinition {
    protected static final HttpListenerResourceDefinition INSTANCE = new HttpListenerResourceDefinition();

    protected static final SimpleAttributeDefinition SOCKET_BINDING =
            new SimpleAttributeDefinitionBuilder(Constants.SOCKET_BINDING, ModelType.STRING)
                    .setAllowNull(false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setValidator(new StringLengthValidator(1))
                    .build();

    protected static SimpleAttributeDefinition[] ATTRIBUTES = {SOCKET_BINDING};

    private HttpListenerResourceDefinition() {
        this(UndertowExtension.HTTP_LISTENER_PATH, UndertowExtension.getResolver(Constants.HTTP_LISTENER),
                HttpListenerAdd.INSTANCE);
    }

    protected HttpListenerResourceDefinition(final PathElement pathElement,
            final ResourceDescriptionResolver descriptionResolver, final OperationStepHandler addHandler) {
        super(pathElement, descriptionResolver, addHandler, ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        OperationStepHandler handler = new ReloadRequiredWriteAttributeHandler(ATTRIBUTES);
        for (SimpleAttributeDefinition attr : getAttributeDefinitions()) {
            resourceRegistration.registerReadWriteAttribute(attr, null, handler);
        }
    }

    protected SimpleAttributeDefinition[] getAttributeDefinitions() {
        return ATTRIBUTES;
    }


}
