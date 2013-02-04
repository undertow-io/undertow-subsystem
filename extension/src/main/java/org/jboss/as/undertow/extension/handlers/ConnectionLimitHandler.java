package org.jboss.as.undertow.extension.handlers;

import io.undertow.server.HttpHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.undertow.extension.AbstractHandlerResourceDefinition;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class ConnectionLimitHandler extends AbstractHandlerResourceDefinition {

    private static SimpleAttributeDefinition HIGH_WATER_MARK = new SimpleAttributeDefinitionBuilder("high-water-mark", ModelType.INT)
            .setAllowExpression(true)
            .setAllowNull(true)
            .build();

    private static SimpleAttributeDefinition LOW_WATER_MARK = new SimpleAttributeDefinitionBuilder("low-water-mark", ModelType.INT)
            .setAllowExpression(true)
            .setAllowNull(true)
            .build();

    /*
    <connection-limit high-water-mark="100" low-water-mark="50"/>
     */

    public ConnectionLimitHandler() {
        super("connection-limit");
    }

    @Override
    public AttributeDefinition[] getAttributes() {
        return new AttributeDefinition[]{HIGH_WATER_MARK, LOW_WATER_MARK};
    }

    //@Override
    public HttpHandler registerHandler(HttpHandler next) {
        return null;
    }
}
