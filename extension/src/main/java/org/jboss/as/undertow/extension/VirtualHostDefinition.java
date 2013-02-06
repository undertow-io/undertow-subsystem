package org.jboss.as.undertow.extension;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
class VirtualHostDefinition extends SimpleResourceDefinition {
    static final VirtualHostDefinition INSTANCE = new VirtualHostDefinition();

    private VirtualHostDefinition() {
        super(UndertowExtension.VIRTUAL_PATH, UndertowExtension.getResolver(Constants.VIRTUAL_HOST),
                new VirtualHostAdd(), ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public void registerChildren(ManagementResourceRegistration registration) {
        super.registerChildren(registration);
        registration.registerSubModel(AJPListenerResourceDefinition.INSTANCE);
        registration.registerSubModel(HttpListenerResourceDefinition.INSTANCE);
        registration.registerSubModel(HttpsListenerResourceDefinition.INSTANCE);
    }

    private static class VirtualHostAdd extends AbstractAddStepHandler {
        @Override
        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

        }
    }

}
