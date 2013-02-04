package org.jboss.as.undertow.extension.handlers;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.jboss.as.controller.parsing.ParseUtils.requireAttributes;

import java.util.List;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PropertiesAttributeDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.ResourceBuilder;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
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
public class CustomFactoryHandler extends AbstractHandlerResourceDefinition {
    public static final SimpleAttributeDefinition MODULE = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.MODULE, ModelType.STRING, false)
            .setValidator(new StringLengthValidator(1)).build();
    static final SimpleAttributeDefinition CLASS =
            new SimpleAttributeDefinitionBuilder(Constants.CLASS, ModelType.STRING, false)
                    .setValidator(new StringLengthValidator(1))
                    .build();
    private static final PropertiesAttributeDefinition PROPERTIES = new PropertiesAttributeDefinition.Builder(Constants.PROPERTIES, true)
            .setWrapXmlElement(true)
            .setWrapperElement(Constants.PROPERTIES)
            .setXmlName(Constants.PROPERTY)
            .setAllowExpression(true)
            .build();


    private static final SimpleAttributeDefinition CHAIN_REF = SimpleAttributeDefinitionBuilder.create(Constants.CHAIN_REF, ModelType.STRING).build();
    private static final ResourceDefinition CHAIN = ResourceBuilder.Factory.create(PathElement.pathElement(Constants.CHAIN), UndertowExtension.getResolver(Constants.HANDLER_CHAIN, Constants.HANDLER, Constants.HOST))
            .setAddOperation(new AbstractAddStepHandler() {
                @Override
                protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
                    CHAIN_REF.validateAndSet(operation, model);
                }
            })
            .setRemoveOperation(new ReloadRequiredRemoveStepHandler())
            .addReadWriteAttribute(CHAIN_REF, null, new ReloadRequiredWriteAttributeHandler(CHAIN_REF))
            .build();


    public CustomFactoryHandler() {
        super(Constants.HANDLER_FACTORY);
    }

    @Override
    public AttributeDefinition[] getAttributes() {
        return new AttributeDefinition[]{MODULE, CLASS, PROPERTIES};
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        super.registerChildren(resourceRegistration);
        resourceRegistration.registerSubModel(CHAIN);
    }

    @Override
    public void parse(XMLExtendedStreamReader reader, PathAddress parentAddress, List<ModelNode> list) throws XMLStreamException {
        PathAddress address = parentAddress.append(Constants.HANDLER, getName());
        String[] attributes = ParseUtils.requireAttributes(reader, ModelDescriptionConstants.MODULE, Constants.CLASS);
        ModelNode factoryOp = Util.createAddOperation(address);
        MODULE.parseAndSetParameter(attributes[0], factoryOp, reader);
        CLASS.parseAndSetParameter(attributes[1], factoryOp, reader);
        list.add(factoryOp);
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            if (Constants.PROPERTIES.equals(reader.getLocalName())) {
                while (reader.nextTag() != END_ELEMENT) {
                    if (Constants.PROPERTY.equals(reader.getLocalName())) {
                        final String[] array = requireAttributes(reader, org.jboss.as.controller.parsing.Attribute.NAME.getLocalName(), org.jboss.as.controller.parsing.Attribute.VALUE.getLocalName());
                        PROPERTIES.parseAndAddParameterElement(array[0], array[1], factoryOp, reader);
                        ParseUtils.requireNoContent(reader);
                    } else {
                        throw ParseUtils.unexpectedElement(reader);
                    }
                }
            } else if (Constants.CHAINS.equals(reader.getLocalName())) {
                while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                    if (reader.getLocalName().equals(Constants.CHAIN)) {
                        attributes = ParseUtils.requireAttributes(reader, Constants.NAME, Constants.CHAIN_REF);
                        ModelNode chainOp = Util.createAddOperation(address.append(Constants.CHAIN, attributes[0]));
                        CHAIN_REF.parseAndSetParameter(attributes[1], chainOp, reader);
                        list.add(chainOp);
                        ParseUtils.requireNoContent(reader);
                    } else {
                        throw ParseUtils.unexpectedElement(reader);
                    }
                }
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
        }
    }

    @Override
    public void persist(XMLExtendedStreamWriter writer, Property handlerProp) throws XMLStreamException {
        writer.writeStartElement(getName());
        ModelNode handler = handlerProp.getValue();
        MODULE.marshallAsAttribute(handler, writer);
        CLASS.marshallAsAttribute(handler, writer);
        PROPERTIES.marshallAsElement(handler, writer);

        final ModelNode chains = handler.get(Constants.CHAIN);
        if (chains.isDefined()) {
            writer.writeStartElement(Constants.CHAINS);
            for (final Property hostProp : chains.asPropertyList()) {
                writer.writeStartElement(Constants.CHAIN);
                writer.writeAttribute(Constants.NAME, hostProp.getName());
                CHAIN_REF.marshallAsAttribute(hostProp.getValue(), writer);
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

}
