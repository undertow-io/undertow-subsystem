package org.jboss.as.undertow.extension;

import java.util.List;

import io.undertow.server.HttpHandler;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class LocationService implements Service<LocationService> {

    private final String locationPath;
    private final List<HttpHandler> handlers;
    private InjectedValue<Host> host = new InjectedValue<>();

    public LocationService(String locationPath, List<HttpHandler> handlers) {
        this.locationPath = locationPath;
        this.handlers = handlers;
    }

    @Override
    public void start(StartContext context) throws StartException {
        HttpHandler h = handlers.get(0);
        UndertowLogger.ROOT_LOGGER.infof("registering handler %s under path '%s'", h, locationPath);
        host.getValue().registerHandler(locationPath, handlers.get(0));
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public LocationService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public InjectedValue<Host> getHost() {
        return host;
    }
}
