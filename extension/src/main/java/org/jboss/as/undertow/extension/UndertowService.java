package org.jboss.as.undertow.extension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.undertow.Version;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class UndertowService implements Service<Void> {
    private final String defaultContainer;
    private final String defaultServer;
    private final String defaultVirtualHost;
    private List<String> registeredServers = new CopyOnWriteArrayList<>();

    protected UndertowService(String defaultContainer, String defaultServer, String defaultVirtualHost) {
        this.defaultContainer = defaultContainer;
        this.defaultServer = defaultServer;
        this.defaultVirtualHost = defaultVirtualHost;
    }

    @Override
    public void start(StartContext context) throws StartException {
        UndertowLogger.ROOT_LOGGER.serverStarting(Version.getVersionString());
    }

    @Override
    public void stop(StopContext context) {
        UndertowLogger.ROOT_LOGGER.serverStopping(Version.getVersionString());
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    protected void registerServer(ServerService server) {
        registeredServers.add(server.getName());
    }

    protected void unRegisterServer(ServerService server) {
        registeredServers.remove(server.getName());
    }

    public String getDefaultContainer() {
        return defaultContainer;
    }

    public String getDefaultServer() {
        return defaultServer;
    }

    public String getDefaultVirtualHost() {
        return defaultVirtualHost;
    }

    public List<String> getServers() {
        return Collections.unmodifiableList(registeredServers);
    }
}
