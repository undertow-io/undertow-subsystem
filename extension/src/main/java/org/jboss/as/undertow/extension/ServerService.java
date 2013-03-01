package org.jboss.as.undertow.extension;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.CookieHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.server.handlers.form.FormEncodedDataHandler;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class ServerService implements Service<ServerService> {


    private final String defaultHost;

    private volatile HttpHandler root;
    private final NameVirtualHostHandler virtualHostHandler = new NameVirtualHostHandler();
    private final InjectedValue<ServletContainerService> servletContainer = new InjectedValue<>();

    protected ServerService(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    @Override
    public void start(StartContext startContext) throws StartException {


       /* for (Undertow.VirtualHost host : hosts) {
            final PathHandler paths = new PathHandler();
            paths.setDefaultHandler(host.defaultHandler);
            for (final Map.Entry<String, HttpHandler> entry : host.handlers.entrySet()) {
                paths.addPath(entry.getKey(), entry.getValue());
            }
            HttpHandler handler = paths;
            for (HandlerWrapper<HttpHandler> wrapper : host.wrappers) {
                handler = wrapper.wrap(handler);
            }
            handler = addLoginConfig(handler, host.loginConfig);
            if (host.defaultHost) {
                virtualHostHandler.setDefaultHandler(handler);
            }
            for (String hostName : host.hostNames) {
                virtualHostHandler.addHost(hostName, handler);
            }
        }
*/
        root = virtualHostHandler;
        root = new CookieHandler(root);
        root = new FormEncodedDataHandler(root);
        root = new SimpleErrorPageHandler(root);
        //TODO: multipart
/*
        if (cacheSize > 0) {
            root = new CacheHandler(new DirectBufferCache<CachedHttpRequest>(1024, cacheSize * 1024 * 1024), root);
        }*/
    }

    public void addHost(String name, HttpHandler handler) {
        virtualHostHandler.addHost(name, handler);
    }

    @Override
    public void stop(StopContext stopContext) {

    }

    @Override
    public ServerService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public String getDefaultHost() {
        return defaultHost;
    }

    public InjectedValue<ServletContainerService> getServletContainer() {
        return servletContainer;
    }

    public NameVirtualHostHandler getVirtualHostHandler() {
        return virtualHostHandler;
    }

    public HttpHandler getRoot() {
        return root;
    }
}
