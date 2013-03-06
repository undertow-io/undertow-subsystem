package org.jboss.as.undertow.extension;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.LinkedList;
import java.util.List;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimplePersistentResourceDefinition;
import org.jboss.as.controller.StringListAttributeDefinition;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
class HostHandlerDefinition extends SimplePersistentResourceDefinition {
    protected static final StringListAttributeDefinition ALIAS = new StringListAttributeDefinition.Builder(Constants.ALIAS)
            .setAllowNull(true)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setValidator(new StringLengthValidator(1))
            .build();
    private static final AttributeDefinition[] ATTRIBUTES = new AttributeDefinition[]{ALIAS};

    static final HostHandlerDefinition INSTANCE = new HostHandlerDefinition();


    private HostHandlerDefinition() {
        super(UndertowExtension.HOST_PATH, UndertowExtension.getResolver(Constants.HOST),
                new HostAdd(),
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public AttributeDefinition[] getAttributes() {
        return ATTRIBUTES;
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        ReloadRequiredWriteAttributeHandler writer = new ReloadRequiredWriteAttributeHandler(getAttributes());
        for (AttributeDefinition attr : getAttributes()) {
            resourceRegistration.registerReadWriteAttribute(attr, null, writer);
        }
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        super.registerChildren(resourceRegistration);
        for (Handler handler : HandlerFactory.getHandlers()) {
            resourceRegistration.registerSubModel(handler);
        }
        resourceRegistration.registerSubModel(PathsHandlerDefinition.INSTANCE);
    }

    public void parse(XMLExtendedStreamReader reader, PathAddress parentAddress, List<ModelNode> list) throws XMLStreamException {
        String[] attrs = ParseUtils.requireAttributes(reader, Constants.NAME, Constants.ALIAS);
        String name = attrs[0];
        String aliases = attrs[1];
        PathAddress address = parentAddress.append(Constants.HOST, name);
        ModelNode op = Util.createAddOperation(address);
        for (String alias : aliases.split(",")) {
            ALIAS.parseAndAddParameterElement(alias, op, reader);
        }
        list.add(op);
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (reader.getLocalName()) {
                case Constants.HANDLERS: {
                    HandlerFactory.parseHandlers(reader, address, list);
                    break;
                }
                case Constants.PATHS: {
                    PathsHandlerDefinition.INSTANCE.parse(reader, address, list);
                    break;
                }
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }

    }


    public void persist(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        if (!model.hasDefined(Constants.HOST)) {
            return;
        }
        for (final Property hostProp : model.get(Constants.HOST).asPropertyList()) {
            writer.writeStartElement(Constants.HOST);
            ModelNode host = hostProp.getValue();
            writer.writeAttribute(Constants.NAME, hostProp.getName());
            StringBuilder aliases = new StringBuilder();
            if (host.hasDefined(ALIAS.getName())) {
                for (ModelNode p : host.get(ALIAS.getName()).asList()) {
                    aliases.append(p.asString()).append(", ");
                }
            }
            if (aliases.length() > 3) {
                aliases.setLength(aliases.length() - 2);
            }
            writer.writeAttribute(Constants.ALIAS, aliases.toString());
            HandlerFactory.persistHandlers(writer, host, true);
            PathsHandlerDefinition.INSTANCE.persist(writer, host);
            writer.writeEndElement();
        }

    }

    private static class HostAdd extends AbstractAddStepHandler {
        @Override
        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
            ALIAS.validateAndSet(operation, model);
        }

        @Override
        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
            final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
            final PathAddress parent = address.subAddress(0, address.size() - 1);
            final String name = address.getLastElement().getValue();
            List<String> aliases = ALIAS.unwrap(context, model);
            final String serverName = parent.getLastElement().getValue();
            final ServiceName virtualHostServiceName = UndertowServices.virtualHostName(serverName, name);
            HostService service = new HostService(name, aliases == null ? new LinkedList<String>() : aliases);
            final ServiceBuilder<HostService> builder = context.getServiceTarget().addService(virtualHostServiceName, service)
                    .addDependency(UndertowServices.SERVER.append(serverName), ServerService.class, service.getServer());


            builder.setInitialMode(ServiceController.Mode.ACTIVE);

            final ServiceController<HostService> serviceController = builder.install();
            if (newControllers != null) {
                newControllers.add(serviceController);
            }
        }
    }
}
