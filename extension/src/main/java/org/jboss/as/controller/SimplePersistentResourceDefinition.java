package org.jboss.as.controller;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public abstract class SimplePersistentResourceDefinition extends SimpleResourceDefinition {

    protected SimplePersistentResourceDefinition(PathElement pathElement, ResourceDescriptionResolver descriptionResolver) {
        super(pathElement, descriptionResolver);
    }

    protected SimplePersistentResourceDefinition(PathElement pathElement, ResourceDescriptionResolver descriptionResolver, OperationStepHandler addHandler, OperationStepHandler removeHandler) {
        super(pathElement, descriptionResolver, addHandler, removeHandler);
    }

    protected SimplePersistentResourceDefinition(PathElement pathElement, ResourceDescriptionResolver descriptionResolver, OperationStepHandler addHandler, OperationStepHandler removeHandler, OperationEntry.Flag addRestartLevel, OperationEntry.Flag removeRestartLevel) {
        super(pathElement, descriptionResolver, addHandler, removeHandler, addRestartLevel, removeRestartLevel);
    }


    public abstract AttributeDefinition[] getAttributes();

    protected Map<String, AttributeDefinition> getAttributeMap() {
        Map<String, AttributeDefinition> res = new HashMap<>();
        for (AttributeDefinition def : getAttributes()) {
            res.put(def.getName(), def);
        }
        return res;
    }

    public String getXmlElementName() {
        return getPathElement().getKey();
    }

    public void parse(final XMLExtendedStreamReader reader, PathAddress parentAddress, List<ModelNode> list) throws XMLStreamException {
        boolean needName = getPathElement().isWildcard();
        String name = null;
        ModelNode op = Util.createAddOperation();
        Map<String, AttributeDefinition> attributes = getAttributeMap();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attributeName = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            if (needName && NAME.equals(attributeName)) {
                name = value;
            } else if (attributes.containsKey(attributeName)) {
                AttributeDefinition def = attributes.get(attributeName);
                if (def instanceof SimpleAttributeDefinition) {
                    ((SimpleAttributeDefinition) def).parseAndSetParameter(value, op, reader);
                } else {
                    throw new IllegalArgumentException("we should know how to handle " + def);
                }
            } else {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }
        PathElement path = needName ? PathElement.pathElement(getPathElement().getKey(), name) : getPathElement();
        PathAddress address = parentAddress.append(path);
        op.get(ADDRESS).set(address.toModelNode());
        list.add(op);
        parseChildren(reader, address, list);
    }

    public void parseChildren(final XMLExtendedStreamReader reader, PathAddress parentAddress, List<ModelNode> list) throws XMLStreamException {
        ParseUtils.requireNoContent(reader);
    }


    public void persist(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        boolean wildcard = getPathElement().isWildcard();
        model = wildcard ? model.get(getPathElement().getKey()) : model.get(getPathElement().getKeyValuePair());
        if (!model.isDefined()) {
            return;
        }

        writer.writeStartElement(getXmlElementName());
        if (wildcard) {
            for (Property p : model.asPropertyList()) {
                writer.writeAttribute(NAME, p.getName());
                for (AttributeDefinition def : getAttributes()) {
                    def.getAttributeMarshaller().marshallAsAttribute(def, p.getValue(), false, writer);
                }
                persistChildren(writer, p.getValue());
            }
        } else {
            for (AttributeDefinition def : getAttributes()) {
                def.getAttributeMarshaller().marshallAsAttribute(def, model, false, writer);
            }
            persistChildren(writer, model);
        }
        writer.writeEndElement();

    }

    protected void persistChildren(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        //
    }


}
