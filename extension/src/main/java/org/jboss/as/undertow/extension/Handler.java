package org.jboss.as.undertow.extension;

import org.jboss.as.controller.PersistentResourceDefinition;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public interface Handler extends PersistentResourceDefinition {

    String getName();

    //todo not sure about this one
    //HttpHandler registerHandler(HttpHandler next);

}
