package org.jboss.as.undertow.extension;

import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class UndertowRootDefinition extends SimpleResourceDefinition {
    public static final UndertowRootDefinition INSTANCE = new UndertowRootDefinition();

    private UndertowRootDefinition() {
        super(UndertowExtension.SUBSYSTEM_PATH,
                UndertowExtension.getResolver(),
                UndertowSubsystemAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }


}
