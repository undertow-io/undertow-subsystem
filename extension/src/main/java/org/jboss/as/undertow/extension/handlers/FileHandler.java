package org.jboss.as.undertow.extension.handlers;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.undertow.extension.AbstractHandlerResourceDefinition;
import org.jboss.as.undertow.extension.Constants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class FileHandler extends AbstractHandlerResourceDefinition {
    /*<file path="/opt/data" cache-buffer-size="1024" cache-buffers="1024"/>*/
    private static SimpleAttributeDefinition PATH = new SimpleAttributeDefinitionBuilder(Constants.PATH, ModelType.STRING)
            .setAllowNull(true)
            .setAllowExpression(true)
            .build();

    private static SimpleAttributeDefinition CACHE_BUFFER_SIZE = new SimpleAttributeDefinitionBuilder("cache-buffer-size", ModelType.LONG)
            .setAllowNull(true)
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode(1024))
            .build();

    private static SimpleAttributeDefinition CACHE_BUFFERS = new SimpleAttributeDefinitionBuilder("cache-buffers", ModelType.LONG)
            .setAllowNull(true)
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode(1024))
            .build();

    public FileHandler() {
        super("file");
    }

    @Override
    public AttributeDefinition[] getAttributes() {
        return new AttributeDefinition[]{PATH, CACHE_BUFFER_SIZE, CACHE_BUFFERS};
    }
}
