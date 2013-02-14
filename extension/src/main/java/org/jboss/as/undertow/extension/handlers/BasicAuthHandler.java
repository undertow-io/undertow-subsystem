package org.jboss.as.undertow.extension.handlers;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.undertow.extension.AbstractHandlerResourceDefinition;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class BasicAuthHandler extends AbstractHandlerResourceDefinition {

    private static SimpleAttributeDefinition SECURITY_DOMAIN = new SimpleAttributeDefinitionBuilder("security-domain", ModelType.STRING)
            .setAllowNull(false)
            .build();

    public BasicAuthHandler() {
        super("basic-auth");
    }

    @Override
    public AttributeDefinition[] getAttributes() {
        return new AttributeDefinition[]{SECURITY_DOMAIN};
    }
}
