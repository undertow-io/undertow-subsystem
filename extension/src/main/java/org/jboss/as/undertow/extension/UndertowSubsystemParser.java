package org.jboss.as.undertow.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.requireSingleAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.list;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.operations.common.Util;
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

        if (model.hasDefined(Constants.WORKER)) {
            for (final Property worker : model.get(Constants.WORKER).asPropertyList()) {
                writer.writeStartElement(Constants.WORKER);
                writer.writeAttribute(Constants.NAME, worker.getName());
                for (SimpleAttributeDefinition def : WorkerResourceDefinition.ATTRIBUTES) {
                    def.marshallAsAttribute(worker.getValue(), false, writer);
                }
                writer.writeEndElement();
            }
        }
        if (model.hasDefined(Constants.HTTP_LISTENER)) {
            for (final Property connector : model.get(Constants.HTTP_LISTENER).asPropertyList()) {
                final ModelNode config = connector.getValue();
                writer.writeStartElement(Constants.HTTP_LISTENER);
                writer.writeAttribute(Constants.NAME, connector.getName());
                for (SimpleAttributeDefinition attr : HttpListenerResourceDefinition.ATTRIBUTES) {
                    attr.marshallAsAttribute(config, false, writer);
                }

                writer.writeEndElement();
            }
        }
        if (model.hasDefined(Constants.HTTPS_LISTENER)) {
            for (final Property connector : model.get(Constants.HTTPS_LISTENER).asPropertyList()) {
                final ModelNode config = connector.getValue();
                writer.writeStartElement(Constants.HTTPS_LISTENER);
                writer.writeAttribute(Constants.NAME, connector.getName());
                for (SimpleAttributeDefinition attr : HttpsListenerResourceDefinition.ATTRIBUTES) {
                    attr.marshallAsAttribute(config, false, writer);
                }

                writer.writeEndElement();
            }
        }
        if (model.hasDefined(Constants.HANDLER_CHAIN)) {
            for (final Property chainProp : model.get(Constants.HANDLER_CHAIN).asPropertyList()) {
                final ModelNode config = chainProp.getValue();
                writer.writeStartElement(Constants.HANDLER_CHAIN);
                writer.writeAttribute(Constants.NAME, chainProp.getName());
                Map<String, Handler> handlerMap = HandlerFactory.getHandlerMap();
                for (final Property handlerProp : config.get(Constants.HANDLER).asPropertyList()){
                    Handler handler = handlerMap.get(handlerProp.getName());
                    handler.persist(writer,handlerProp);
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
        final ModelNode subsystem = Util.createAddOperation(address);
        list.add(subsystem);
        final Namespace namespace = Namespace.forUri(reader.getNamespaceURI());
        // elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (namespace) {
                case UNDERTOW_1_0: {
                    switch (reader.getLocalName()) {
                        case Constants.HTTP_LISTENER: {
                            parseHttpListener(reader, address, false, list);
                            break;
                        }
                        case Constants.HTTPS_LISTENER: {
                            parseHttpListener(reader, address, true, list);
                            break;
                        }
                        case Constants.WORKER: {
                            parseWorker(reader, address, list);
                            break;
                        }
                        case Constants.HANDLER_CHAIN: {
                            parseHandlerChain(reader, address, list);
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

    static void parseWorker(XMLExtendedStreamReader reader, PathAddress parent, List<ModelNode> list) throws XMLStreamException {
        ModelNode worker = Util.createAddOperation(parent);
        PathAddress address = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final String name = reader.getAttributeLocalName(i);
            final SimpleAttributeDefinition ad = WorkerResourceDefinition.ATTRIBUTES_BY_XMLNAME.get(name);
            if (ad != null) {
                ad.parseAndSetParameter(value, worker, reader);
            } else if (name.equals(Constants.NAME)) {
                address = parent.append(Constants.WORKER, value);
            } else {
                throw unexpectedAttribute(reader, i);
            }
        }
        if (address == null) {
            HashSet<String> missing = new HashSet<>();
            missing.add(Constants.NAME);
            throw ParseUtils.missingRequired(reader, missing);
        }
        worker.get(ADDRESS).set(address.toModelNode());
        list.add(worker);
        ParseUtils.requireNoContent(reader);
    }

    static void parseHttpListener(XMLExtendedStreamReader reader, PathAddress parent, boolean https, List<ModelNode> list) throws XMLStreamException {
        String name = null;
        final ModelNode connector = Util.createAddOperation();

        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);

            switch (reader.getAttributeLocalName(i)) {
                case Constants.SOCKET_BINDING:
                    HttpListenerResourceDefinition.SOCKET_BINDING.parseAndSetParameter(value, connector, reader);
                    break;
                case Constants.SECURITY_REALM:
                    if (https == false) {
                        throw unexpectedAttribute(reader, i);
                    }
                    HttpsListenerResourceDefinition.SECURITY_REALM.parseAndSetParameter(value, connector, reader);
                    break;
                case Constants.NAME:
                    name = value;
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }

        PathAddress address = PathAddress.pathAddress(parent, PathElement.pathElement(https ? Constants.HTTPS_LISTENER : Constants.HTTP_LISTENER, name));
        connector.get(OP_ADDR).set(address.toModelNode());
        list.add(connector);
        ParseUtils.requireNoContent(reader);
    }

    static void parseHandlerChain(final XMLExtendedStreamReader reader, PathAddress parentAddress, List<ModelNode> list) throws XMLStreamException {
        requireSingleAttribute(reader, NAME);
        String name = reader.getAttributeValue(null, NAME);
        PathAddress address = parentAddress.append(Constants.HANDLER_CHAIN, name);
        list.add(Util.createAddOperation(address));

        Map<String, Handler> handlerMap = HandlerFactory.getHandlerMap();
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            String tagName = reader.getLocalName();
            Handler handler = handlerMap.get(tagName);
            if (handler != null) {
                handler.parse(reader, address, list);
            } else {
                throw unexpectedElement(reader);
            }
        }

    }


}

