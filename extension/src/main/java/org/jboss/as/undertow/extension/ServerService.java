package org.jboss.as.undertow.extension;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.CanonicalPathHandler;
import io.undertow.server.handlers.CookieHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
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
    private List<AbstractListenerService> listeners = new LinkedList<>();
    private final ConcurrentHashMap<String, Host> registerHosts = new ConcurrentHashMap<>();


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
        root = new CanonicalPathHandler(root);

/*
        if (cacheSize > 0) {
            root = new CacheHandler(new DirectBufferCache<CachedHttpRequest>(1024, cacheSize * 1024 * 1024), root);
        }*/

        UndertowLogger.ROOT_LOGGER.infof("Starting server server service: %s", startContext.getController().getName());
        servletContainer.getValue().registerServer(this);
    }

    protected void registerListener(AbstractListenerService listener) {
        listeners.add(listener);
        if (listener.isSecure()) {
            //servletContainer.getValue().registerSecurePort(listener.getName(), listener.getBinding().getValue().getPort());
        }
    }

    protected void unRegisterListener(AbstractListenerService listener) {
        listeners.add(listener);
        if (listener.isSecure()) {
            servletContainer.getValue().unregisterSecurePort(listener.getName());
        }
    }

    protected void registerHost(Host host) {
        for (String hostName : host.getAllHosts()) {
            registerHosts.putIfAbsent(hostName, host);
            virtualHostHandler.addHost(hostName, host.getRootHandler());
        }
        if (host.getName().equals(getDefaultHost())) {
            virtualHostHandler.setDefaultHandler(host.getRootHandler());
        }
    }

    protected void unRegisterHost(Host host) {
        for (String hostName : host.getAllHosts()) {
            registerHosts.remove(hostName);
            virtualHostHandler.removeHost(hostName);
        }
        if (host.getName().equals(getDefaultHost())) {
            virtualHostHandler.setDefaultHandler(ResponseCodeHandler.HANDLE_404);
        }
    }

    protected Host getHost(String hostname) {
        return registerHosts.get(hostname);
    }
    @Override
    public void stop(StopContext stopContext) {
        servletContainer.getValue().unRegisterServer(this);
    }

    @Override
    public ServerService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public String getDefaultHost() {
        return defaultHost;
    }

    protected InjectedValue<ServletContainerService> getServletContainer() {
        return servletContainer;
    }

    protected HttpHandler getRoot() {
        return root;
    }
}
