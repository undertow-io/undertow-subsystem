package org.jboss.as.undertow.extension.handlers;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.undertow.extension.AbstractHandlerResourceDefinition;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class FileErrorPageHandler extends AbstractHandlerResourceDefinition {

    private static SimpleAttributeDefinition CODE = new SimpleAttributeDefinitionBuilder("code", ModelType.INT)
            .setAllowExpression(true)
            .setAllowNull(true)
            .build();

    private static SimpleAttributeDefinition FILE = new SimpleAttributeDefinitionBuilder("file", ModelType.STRING)
            .setAllowExpression(true)
            .setAllowNull(true)
            .build();
    /*<file-error-page code="404" file="/my/error/page.html"/>*/

    public FileErrorPageHandler() {
        super("file-error-page");
    }

    @Override
    public AttributeDefinition[] getAttributes() {
        return new AttributeDefinition[]{CODE, FILE};
    }

    private void create() {

    }


}
