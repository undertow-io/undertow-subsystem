package org.jboss.as.undertow.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.form.MultiPartHandler;
import io.undertow.servlet.api.DeploymentInfo;
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
    private InjectedValue<ServerService> server = new InjectedValue<>();
    private final PathHandler pathHandler = new PathHandler();
    private final List<String> allHosts;
    private volatile MultiPartHandler rootHandler;

    protected HostService(String name, List<String> aliases) {
        this.name = name;
        List<String> hosts = new ArrayList<>(aliases.size() + 1);
        hosts.add(name);
        hosts.addAll(aliases);
        allHosts = Collections.unmodifiableList(hosts);
        rootHandler = new MultiPartHandler();
    }

    @Override
    public void start(StartContext context) throws StartException {
        pathHandler.setDefaultHandler(ResponseCodeHandler.HANDLE_404);
        rootHandler.setNext(pathHandler);
        server.getValue().registerHost(this);
    }

    @Override
    public void stop(StopContext context) {
        server.getValue().unRegisterHost(this);
    }

    @Override
    public HostService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    protected InjectedValue<ServerService> getServer() {
        return server;
    }

    protected List<String> getAllHosts() {
        return allHosts;
    }

    public String getName() {
        return name;
    }

    protected HttpHandler getRootHandler() {
        return rootHandler;
    }

    public void registerDeployment(DeploymentInfo deploymentInfo, HttpHandler handler) {
        String path = ServletContainerService.getDeployedContextPath(deploymentInfo);
        pathHandler.addPath(path, handler);
        UndertowLogger.ROOT_LOGGER.registerWebapp(path);
    }

    public void unRegisterDeployment(DeploymentInfo deploymentInfo) {
        String path = ServletContainerService.getDeployedContextPath(deploymentInfo);
        pathHandler.removePath(path);
        UndertowLogger.ROOT_LOGGER.unregisterWebapp(path);
    }
}
