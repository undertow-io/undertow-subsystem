package org.jboss.as.undertow.extension;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class HandlerChainDefinition extends SimpleResourceDefinition {
    static final HandlerChainDefinition INSTANCE = new HandlerChainDefinition();

    private HandlerChainDefinition() {
        super(UndertowExtension.HANDLER_CHAIN_PATH, UndertowExtension.getResolver(Constants.HANDLER_CHAIN),
                HandlerChainAdd.INSTANCE, ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        super.registerChildren(resourceRegistration);
        for (Handler handler : HandlerFactory.getHandlers()) {
            resourceRegistration.registerSubModel(handler);
        }
    }


}
