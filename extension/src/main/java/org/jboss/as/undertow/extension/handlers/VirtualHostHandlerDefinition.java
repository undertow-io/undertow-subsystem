package org.jboss.as.undertow.extension.handlers;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.List;
import javax.xml.stream.XMLStreamException;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.ResourceBuilder;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.undertow.extension.AbstractHandlerResourceDefinition;
import org.jboss.as.undertow.extension.Constants;
import org.jboss.as.undertow.extension.UndertowExtension;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class VirtualHostHandlerDefinition extends AbstractHandlerResourceDefinition {

    private static final SimpleAttributeDefinition CHAIN_REF = SimpleAttributeDefinitionBuilder.create(Constants.CHAIN_REF, ModelType.STRING).build();
    private static final ResourceDefinition HOST = ResourceBuilder.Factory.create(PathElement.pathElement(Constants.HOST), UndertowExtension.getResolver(Constants.HANDLER_CHAIN, Constants.HANDLER, Constants.HOST))
            .setAddOperation(new AbstractAddStepHandler() {
                @Override
                protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
                    CHAIN_REF.validateAndSet(operation, model);
                }
            })
            .setRemoveOperation(new ReloadRequiredRemoveStepHandler())
            .addReadWriteAttribute(CHAIN_REF, null, new ReloadRequiredWriteAttributeHandler(CHAIN_REF))
            .build();


    public VirtualHostHandlerDefinition() {
        super(Constants.VIRTUAL_HOST);
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        super.registerChildren(resourceRegistration);
        resourceRegistration.registerSubModel(HOST);
    }

    @Override
    public void parse(XMLExtendedStreamReader reader, PathAddress parentAddress, List<ModelNode> list) throws XMLStreamException {
        PathAddress address = parentAddress.append(Constants.HANDLER, getName());
        list.add(Util.createAddOperation(address));
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            if (reader.getLocalName().equals(Constants.HOST)) {
                String[] attributes = ParseUtils.requireAttributes(reader, Constants.NAME, Constants.CHAIN_REF);
                ModelNode op = Util.createAddOperation(address.append(Constants.HOST, attributes[0]));
                CHAIN_REF.parseAndSetParameter(attributes[1], op, reader);
                list.add(op);
                ParseUtils.requireNoContent(reader);
            } else {
                throw unexpectedElement(reader);
            }
        }
    }

    @Override
    public void persist(XMLExtendedStreamWriter writer, Property handler) throws XMLStreamException {
        writer.writeStartElement(getName());
        for (final Property hostProp : handler.getValue().get(Constants.HOST).asPropertyList()) {
            writer.writeStartElement(Constants.HOST);
            writer.writeAttribute(Constants.NAME, hostProp.getName());
            CHAIN_REF.marshallAsAttribute(hostProp.getValue(), writer);
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }



    public HttpHandler registerHandler(HttpHandler next) {
        return new NameVirtualHostHandler();//todo
    }
}
