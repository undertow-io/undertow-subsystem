package org.jboss.as.undertow.extension;

import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

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
