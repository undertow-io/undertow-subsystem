package org.jboss.as.undertow.extension;

import java.util.Map;
import javax.xml.stream.XMLStreamException;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimplePersistentResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class VirtualHostHandlerDefinition extends SimplePersistentResourceDefinition {
    static final VirtualHostHandlerDefinition INSTANCE = new VirtualHostHandlerDefinition();

    public VirtualHostHandlerDefinition() {
        super(UndertowExtension.VIRTUAL_HOST_PATH, UndertowExtension.getResolver(Constants.VIRTUAL_HOST), new AbstractAddStepHandler() {
            @Override
            protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

            }
        }, ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public AttributeDefinition[] getAttributes() {
        return new AttributeDefinition[0];
    }

    @Override
    public void registerChildren(ManagementResourceRegistration registration) {
        super.registerChildren(registration);
        registration.registerSubModel(AJPListenerResourceDefinition.INSTANCE);
        registration.registerSubModel(HttpListenerResourceDefinition.INSTANCE);
        registration.registerSubModel(HttpsListenerResourceDefinition.INSTANCE);
        registration.registerSubModel(HandlerChainDefinition.INSTANCE);
        registration.registerSubModel(HostHandlerDefinition.INSTANCE);
    }

    /*  @Override
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
   */
    @Override
    public void persist(XMLExtendedStreamWriter writer, ModelNode model) throws XMLStreamException {
        if (!model.hasDefined(Constants.VIRTUAL_HOST)) { return; }

        for (final Property virtualHost : model.get(Constants.VIRTUAL_HOST).asPropertyList()) {
            final ModelNode config = virtualHost.getValue();
            writer.writeStartElement(Constants.VIRTUAL_HOST);
            writer.writeAttribute(Constants.DEFAULT_HOST, virtualHost.getName());
            HttpListenerResourceDefinition.INSTANCE.persist(writer, config);
            HttpsListenerResourceDefinition.INSTANCE.persist(writer, config);
            AJPListenerResourceDefinition.INSTANCE.persist(writer, config);

            if (config.hasDefined(Constants.HANDLER_CHAIN)) { //todo remove handler_chain
                for (final Property chainProp : config.get(Constants.HANDLER_CHAIN).asPropertyList()) {
                    //final ModelNode config = chainProp.getValue();
                    writer.writeStartElement(Constants.HANDLERS);
                    //writer.writeAttribute(Constants.NAME, chainProp.getName());
                    Map<String, Handler> handlerMap = HandlerFactory.getHandlerMap();
                    for (final Property handlerProp : chainProp.getValue().get(Constants.HANDLER).asPropertyList()) {
                        Handler handler = handlerMap.get(handlerProp.getName());
                        handler.persist(writer, handlerProp);
                    }
                    writer.writeEndElement();
                }
            }
            HostHandlerDefinition.INSTANCE.persist(writer,config);

            writer.writeEndElement();
        }

    }


    public HttpHandler registerHandler(HttpHandler next) {
        return new NameVirtualHostHandler();//todo
    }
}
