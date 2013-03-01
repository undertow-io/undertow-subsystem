package org.jboss.as.undertow.extension;

import java.util.List;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class HostService implements Service<HostService> {
    private String name;
    private List<String> aliases;
    private InjectedValue<ServerService> server = new InjectedValue<>();

    public HostService(String name, List<String> aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    @Override
    public void start(StartContext context) throws StartException {
        final PathHandler paths = new PathHandler();
        /*paths.setDefaultHandler(host.defaultHandler);
        for (final Map.Entry<String, HttpHandler> entry : host.handlers.entrySet()) {
            paths.addPath(entry.getKey(), entry.getValue());
        }*/
        HttpHandler handler = paths;
        /*for (HandlerWrapper<HttpHandler> wrapper : host.wrappers) {
            handler = wrapper.wrap(handler);
        }*/
        server.getValue().addHost(name, handler);


        /*handler = addLoginConfig(handler, host.loginConfig);
        if (host.defaultHost) {
            virtualHostHandler.setDefaultHandler(handler);
        }*/

        /*final PathHandler paths = new PathHandler();
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
                   }*/
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public HostService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public InjectedValue<ServerService> getServer() {
        return server;
    }
}
