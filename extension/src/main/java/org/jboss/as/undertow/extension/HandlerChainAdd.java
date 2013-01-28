package org.jboss.as.undertow.extension;

import java.util.List;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

/**
* @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
*/
public class HandlerChainAdd extends AbstractAddStepHandler {
    static final HandlerChainAdd INSTANCE = new HandlerChainAdd();

    private HandlerChainAdd() {

    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        super.performRuntime(context, operation, model, verificationHandler, newControllers);
    }
}
