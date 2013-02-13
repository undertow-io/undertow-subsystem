package org.jboss.as.undertow.extension;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimplePersistentResourceDefinition;
import org.jboss.as.controller.StringListAttributeDefinition;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
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
        super(UndertowExtension.HOST_PATH, UndertowExtension.getResolver(Constants.HOST), new AbstractAddStepHandler() {
            @Override
            protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
                ALIAS.validateAndSet(operation, model);
            }
        },
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
        Map<String, Handler> handlerMap = HandlerFactory.getHandlerMap();
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (reader.getLocalName()) {
                case Constants.HANDLERS: {
                    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                        String tagName = reader.getLocalName();
                        Handler handler = handlerMap.get(tagName);
                        if (handler != null) {
                            handler.parse(reader, address, list);
                        } else {
                            throw unexpectedElement(reader);
                        }
                    }
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
            StringBuffer aliases = new StringBuffer();
            for (ModelNode p : host.get(ALIAS.getName()).asList()) {
                aliases.append(p.asString()).append(", ");
            }
            if (aliases.length() > 3) {
                aliases.setLength(aliases.length() - 2);
            }
            writer.writeAttribute(Constants.ALIAS, aliases.toString());

            if (host.hasDefined(Constants.HANDLER)) {
                writer.writeStartElement(Constants.HANDLERS);
                Map<String, Handler> handlerMap = HandlerFactory.getHandlerMap();
                for (final Property handlerProp : host.get(Constants.HANDLER).asPropertyList()) {
                    Handler handler = handlerMap.get(handlerProp.getName());
                    handler.persist(writer, handlerProp);
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }

    }

}
