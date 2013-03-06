package org.jboss.as.undertow.extension;

import java.util.Map;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.HttpHandlers;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.CopyOnWriteMap;
import io.undertow.util.Headers;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class DefaultVirtualHostHandler implements HttpHandler {
    private volatile HttpHandler defaultHandler = ResponseCodeHandler.HANDLE_404;
    private final Map<String, HttpHandler> hosts = new CopyOnWriteMap<String, HttpHandler>();


    @Override
    public void handleRequest(final HttpServerExchange exchange) {
        final String host = exchange.getRequestHeaders().getFirst(Headers.HOST);
        if (host != null) {
            //host is in format host:port
            String hostName = host.split(":")[0];

            final HttpHandler handler = hosts.get(hostName);
            if (handler != null) {
                HttpHandlers.executeHandler(handler, exchange);
                return;
            }
        }
        HttpHandlers.executeHandler(defaultHandler, exchange);
    }

    public HttpHandler getDefaultHandler() {
        return defaultHandler;
    }

    public Map<String, HttpHandler> getHosts() {
        return hosts;
    }

    public DefaultVirtualHostHandler setDefaultHandler(final HttpHandler defaultHandler) {
        HttpHandlers.handlerNotNull(defaultHandler);
        this.defaultHandler = defaultHandler;
        return this;
    }

    public DefaultVirtualHostHandler addHost(final String host, final HttpHandler handler) {
        HttpHandlers.handlerNotNull(handler);
        hosts.put(host, handler);
        return this;
    }

    public DefaultVirtualHostHandler removeHost(final String host) {
        hosts.remove(host);
        return this;
    }
}
