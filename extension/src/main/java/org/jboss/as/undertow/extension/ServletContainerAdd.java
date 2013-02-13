package org.jboss.as.undertow.extension;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

/**
* @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
*/
final class ServletContainerAdd extends AbstractAddStepHandler {
    static final ServletContainerAdd INSTANCE = new ServletContainerAdd();

    ServletContainerAdd() {
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

    }
}
