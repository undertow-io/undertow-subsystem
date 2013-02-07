package org.jboss.as.undertow.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.requireSingleAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;
import static org.jboss.as.undertow.extension.Constants.NAME;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
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
        WorkerResourceDefinition.INSTANCE.persist(writer, model);
        BufferPoolResourceDefinition.INSTANCE.persist(writer, model);
        VirtualHostHandlerDefinition.INSTANCE.persist(writer, model);
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
                        case Constants.WORKER: {
                            WorkerResourceDefinition.INSTANCE.parse(reader, address, list);
                            break;
                        }
                        case Constants.BUFFER_POOL: {
                            BufferPoolResourceDefinition.INSTANCE.parse(reader, address, list);
                            break;
                        }
                        case Constants.VIRTUAL_HOST: {
                            parseVirtualHost(reader, address, list);
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

    static void parseVirtualHost(XMLExtendedStreamReader reader, PathAddress parent, List<ModelNode> list) throws XMLStreamException {
        PathAddress address;

        requireSingleAttribute(reader, Constants.DEFAULT_HOST);
        String defaultHost = reader.getAttributeValue(0);
        address = parent.append(Constants.VIRTUAL_HOST, defaultHost);
        list.add(Util.createAddOperation(address));

        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (reader.getLocalName()) {
                case Constants.HTTP_LISTENER: {
                    HttpListenerResourceDefinition.INSTANCE.parse(reader, address, list);
                    break;
                }
                case Constants.HTTPS_LISTENER: {
                    HttpsListenerResourceDefinition.INSTANCE.parse(reader, address, list);
                    break;
                }
                case Constants.AJP_LISTENER: {
                    AJPListenerResourceDefinition.INSTANCE.parse(reader, address, list);
                    break;
                }
                case Constants.HANDLERS: {
                    parseHandlers(reader, address, list);
                    break;
                }
                case Constants.HOST: {
                    HostHandlerDefinition.INSTANCE.parse(reader, address, list);
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
            } else if (name.equals(NAME)) {
                address = parent.append(Constants.WORKER, value);
            } else {
                throw unexpectedAttribute(reader, i);
            }
        }
        if (address == null) {
            HashSet<String> missing = new HashSet<>();
            missing.add(NAME);
            throw ParseUtils.missingRequired(reader, missing);
        }
        worker.get(ADDRESS).set(address.toModelNode());
        list.add(worker);
        ParseUtils.requireNoContent(reader);
    }


    static void parseHandlers(final XMLExtendedStreamReader reader, PathAddress parentAddress, List<ModelNode> list) throws XMLStreamException {
        PathAddress address = parentAddress.append(Constants.HANDLER_CHAIN, "default");
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






    /*static void parseHandlerChain(final XMLExtendedStreamReader reader, PathAddress parentAddress, List<ModelNode> list) throws XMLStreamException {
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

*/

}

