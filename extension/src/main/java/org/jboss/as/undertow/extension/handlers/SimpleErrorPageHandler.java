package org.jboss.as.undertow.extension.handlers;

import io.undertow.server.HttpHandler;
import org.jboss.as.undertow.extension.AbstractHandlerResourceDefinition;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class SimpleErrorPageHandler extends AbstractHandlerResourceDefinition {

    public SimpleErrorPageHandler() {
        super("simple-error-page");
    }

    //@Override
    public HttpHandler registerHandler(HttpHandler next) {
        return new io.undertow.server.handlers.error.SimpleErrorPageHandler(next);
    }
}
