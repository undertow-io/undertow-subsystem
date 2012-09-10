package org.jboss.as.undertow.extension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.remoting.Attribute;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.xnio.Option;
import org.xnio.Options;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class WorkerResourceDefinition extends SimpleResourceDefinition {


    //The defaults for these come from XnioWorker

    static final SimpleAttributeDefinition THREAD_DAEMON = createAttribute(Options.THREAD_DAEMON, "thread-daemon", false);
    static final SimpleAttributeDefinition WORKER_TASK_CORE_THREADS = createAttribute(Options.WORKER_TASK_CORE_THREADS, Attribute.WORKER_TASK_CORE_THREADS, 4);
    static final SimpleAttributeDefinition WORKER_TASK_MAX_THREADS = createAttribute(Options.WORKER_TASK_MAX_THREADS, Attribute.WORKER_TASK_LIMIT, 16);
    static final SimpleAttributeDefinition WORKER_TASK_KEEPALIVE = createAttribute(Options.WORKER_TASK_KEEPALIVE, Attribute.WORKER_TASK_KEEPALIVE, 60);
    static final SimpleAttributeDefinition STACK_SIZE = createAttribute(Options.STACK_SIZE, "stack-size", 10);
    static final SimpleAttributeDefinition WORKER_READ_THREADS = createAttribute(Options.WORKER_READ_THREADS, Attribute.WORKER_READ_THREADS, 1);
    static final SimpleAttributeDefinition WORKER_WRITE_THREADS = createAttribute(Options.WORKER_WRITE_THREADS, Attribute.WORKER_WRITE_THREADS, 1);
    static final SimpleAttributeDefinition WORKER_TASK_LIMIT = createAttribute(Options.WORKER_TASK_LIMIT, Attribute.WORKER_TASK_LIMIT, 0x4000);

    /*WORKER_NAME
    THREAD_DAEMON
    WORKER_TASK_CORE_THREADS
    WORKER_TASK_MAX_THREADS
    WORKER_TASK_KEEPALIVE
    STACK_SIZE
    WORKER_READ_THREADS
    WORKER_WRITE_THREADS
    WORKER_TASK_LIMIT*/

    /*
    dmlloyd:	correct
    workers support...
    WORKER_NAME THREAD_DAEMON WORKER_TASK_CORE_THREADS WORKER_TASK_MAX_THREADS WORKER_TASK_KEEPALIVE STACK_SIZE WORKER_READ_THREADS WORKER_WRITE_THREADS
    WORKER_NAME should be derived from the resource name for ease of debugging and whatnot
    maybe something like "%s I/O" where %s is the name of the resource
    in current upstream, WORKER_TASK_CORE_THREADS and WORKER_TASK_KEEPALIVE have no effect
    or WORKER_TASK_LIMIT which should also be supported
    actually, forget that last one
    WORKER_TASK_LIMIT should diaf
    limiting the work queue will just lead to 500 errors and other problems
     */


    static AttributeDefinition[] ATTRIBUTES = new AttributeDefinition[]{
            WORKER_READ_THREADS,
            WORKER_TASK_CORE_THREADS,
            WORKER_TASK_KEEPALIVE,
            WORKER_TASK_LIMIT,
            WORKER_TASK_MAX_THREADS,
            WORKER_WRITE_THREADS,
            THREAD_DAEMON,
            WORKER_TASK_LIMIT,
            STACK_SIZE
    };
    public static final WorkerResourceDefinition INSTANCE = new WorkerResourceDefinition();


    private static SimpleAttributeDefinition createAttribute(Option option, Attribute attribute, Object defaultValue) {
        return createAttribute(option, attribute.getLocalName(), defaultValue);
    }

    private static SimpleAttributeDefinition createAttribute(Option option, String xmlName, Object defaultValue) {

        try {
            Field typeField = option.getClass().getDeclaredField("type");
            typeField.setAccessible(true);
            Class type = (Class) typeField.get(option);
            ModelType modelType;
            ModelNode defaultModel = new ModelNode();
            if (type.isAssignableFrom(Integer.class)) {
                modelType = ModelType.INT;
                defaultModel.set((Integer) defaultValue);
            } else if (type.isAssignableFrom(Long.class)) {
                modelType = ModelType.LONG;
                defaultModel.set((Long) defaultValue);
            } else if (type.isAssignableFrom(BigInteger.class)) {
                modelType = ModelType.BIG_INTEGER;
                defaultModel.set((BigInteger) defaultValue);
            } else if (type.isAssignableFrom(Double.class)) {
                modelType = ModelType.DOUBLE;
                defaultModel.set((Double) defaultValue);
            } else if (type.isAssignableFrom(BigDecimal.class)) {
                modelType = ModelType.BIG_DECIMAL;
                defaultModel.set((BigDecimal) defaultValue);
            } else if (type.isAssignableFrom(String.class)) {
                modelType = ModelType.STRING;
                defaultModel.set((String) defaultValue);
            } else if (type.isAssignableFrom(Boolean.class)) {
                modelType = ModelType.BOOLEAN;
                defaultModel.set((Boolean) defaultValue);
            } else {
                modelType = ModelType.OBJECT;
            }
            return new SimpleAttributeDefinitionBuilder(option.getName(), modelType)
                    .setDefaultValue(defaultModel)
                    .setXmlName(xmlName)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private WorkerResourceDefinition() {
        super(UndertowExtension.WORKER_PATH,
                UndertowExtension.getResourceDescriptionResolver(Constants.WORKER),
                WorkerAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE
        );
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        for (AttributeDefinition attr : WorkerResourceDefinition.ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attr, null, new ReloadRequiredWriteAttributeHandler(attr));
        }
    }
}
