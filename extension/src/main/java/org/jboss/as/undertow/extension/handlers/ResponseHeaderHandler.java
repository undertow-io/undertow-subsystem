package org.jboss.as.undertow.extension.handlers;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.undertow.extension.AbstractHandlerResourceDefinition;
import org.jboss.as.undertow.extension.Constants;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class ResponseHeaderHandler extends AbstractHandlerResourceDefinition {

    private static SimpleAttributeDefinition NAME = new SimpleAttributeDefinitionBuilder(Constants.NAME, ModelType.STRING)
            .setAllowNull(false)
            .setAllowExpression(true)
            .build();

    private static SimpleAttributeDefinition VALUE = new SimpleAttributeDefinitionBuilder("value", ModelType.STRING)
            .setAllowNull(false)
            .setAllowExpression(true)
            .build();

    public ResponseHeaderHandler() {
        super("response-header");
    }

    @Override
    public AttributeDefinition[] getAttributes() {
        return new AttributeDefinition[]{NAME, VALUE};
    }
}
