package org.jboss.as.undertow.extension;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import io.undertow.server.HttpHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public interface Handler extends ResourceDefinition {

    String getName();

    AttributeDefinition[] getAttributes();

    void parse(final XMLExtendedStreamReader reader, PathAddress parentAddress, List<ModelNode> list) throws XMLStreamException;

    void persist(final XMLExtendedStreamWriter writer, Property handler) throws XMLStreamException;

    //todo not sure about this one
    //HttpHandler registerHandler(HttpHandler next);

}
