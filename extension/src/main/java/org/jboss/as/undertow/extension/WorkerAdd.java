package org.jboss.as.undertow.extension;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class WorkerAdd extends AbstractAddStepHandler {
    public static final WorkerAdd INSTANCE = new WorkerAdd();

    private WorkerAdd(){

    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (AttributeDefinition attr : WorkerResourceDefinition.ATTRIBUTES) {
            attr.validateAndSet(operation, model);
        }
    }
}
