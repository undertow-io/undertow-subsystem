package org.jboss.as.undertow.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.List;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class UndertowSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {
    protected static final UndertowSubsystemParser INSTANCE = new UndertowSubsystemParser();

    private UndertowSubsystemParser() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(Namespace.CURRENT.getUriString(), false);
        ModelNode model = context.getModelNode();
        for (SimpleAttributeDefinition def : UndertowRootDefinition.ATTRIBUTES) {
            def.marshallAsAttribute(model, false, writer);
        }
        if (model.hasDefined(Constants.LISTENER)) {
            for (final Property connector : model.get(Constants.LISTENER).asPropertyList()) {
                final ModelNode config = connector.getValue();
                writer.writeStartElement(Element.LISTENER.getLocalName());
                writer.writeAttribute(Attribute.NAME.getLocalName(), connector.getName());
                for (SimpleAttributeDefinition attr : ListenerResourceDefinition.ATTRIBUTES) {
                    attr.marshallAsAttribute(config, false, writer);

                }

                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        PathAddress address = PathAddress.pathAddress(UndertowExtension.SUBSYSTEM_PATH);
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).set(address.toModelNode());
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case WORKER_THREADS:
                    UndertowRootDefinition.WORKER_THREADS.parseAndSetParameter(value, subsystem, reader);
                    break;
                case READ_THREADS:
                    UndertowRootDefinition.READ_THREADS.parseAndSetParameter(value, subsystem, reader);
                    break;
                case WRITE_THREADS:
                    UndertowRootDefinition.WRITE_THREADS.parseAndSetParameter(value, subsystem, reader);
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        list.add(subsystem);
        final Namespace namespace = Namespace.forUri(reader.getNamespaceURI());
        // elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (namespace) {
                case UNDERTOW_1_0: {
                    final Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case LISTENER: {
                            parseListener(reader, address, list);
                            break;
                        }
                        default: {
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
        ParseUtils.requireNoContent(reader);

    }

    static void parseListener(XMLExtendedStreamReader reader, PathAddress parent, List<ModelNode> list) throws XMLStreamException {
        String name = null;
        final ModelNode connector = new ModelNode();

        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case SOCKET_BINDING:
                    ListenerResourceDefinition.SOCKET_BINDING.parseAndSetParameter(value, connector, reader);
                    break;
                case PATH:
                    ListenerResourceDefinition.PATH.parseAndSetParameter(value, connector, reader);
                    break;
                case NAME:
                    name = value;
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }

        connector.get(OP).set(ADD);
        PathAddress address = PathAddress.pathAddress(parent, PathElement.pathElement(Constants.LISTENER, name));
        connector.get(OP_ADDR).set(address.toModelNode());
        list.add(connector);

    }


}

