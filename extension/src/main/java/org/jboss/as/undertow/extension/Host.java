package org.jboss.as.undertow.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.Servlet;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.form.MultiPartHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import org.jboss.as.undertow.deployment.FileResourceLoader;
import org.jboss.as.web.host.ServletBuilder;
import org.jboss.as.web.host.WebDeploymentBuilder;
import org.jboss.as.web.host.WebDeploymentController;
import org.jboss.as.web.host.WebHost;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class Host implements Service<Host>, WebHost {
    private final PathHandler pathHandler = new PathHandler();
    private final List<String> allHosts;
    private String name;
    private InjectedValue<ServerService> server = new InjectedValue<>();
    private volatile MultiPartHandler rootHandler;

    protected Host(String name, List<String> aliases) {
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
        UndertowLogger.ROOT_LOGGER.infof("Starting host %s", name);
    }

    @Override
    public void stop(StopContext context) {
        server.getValue().unRegisterHost(this);
        pathHandler.clearPaths();
        UndertowLogger.ROOT_LOGGER.infof("Stopping host %s", name);
    }

    @Override
    public Host getValue() throws IllegalStateException, IllegalArgumentException {
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
        registerHandler(path, handler);
        UndertowLogger.ROOT_LOGGER.registerWebapp(path);
    }

    public void unRegisterDeployment(DeploymentInfo deploymentInfo) {
        String path = ServletContainerService.getDeployedContextPath(deploymentInfo);
        unRegisterHandler(path);
        UndertowLogger.ROOT_LOGGER.unregisterWebapp(path);
    }

    public void registerHandler(String path, HttpHandler handler) {
        pathHandler.addPath(path, handler);
    }

    public void unRegisterHandler(String path) {
        pathHandler.removePath(path);
    }

    @Override
    public WebDeploymentController addWebDeployment(final WebDeploymentBuilder webDeploymentBuilder) throws Exception {

        DeploymentInfo d = new DeploymentInfo();
        d.setContextPath(webDeploymentBuilder.getContextRoot());
        d.setClassLoader(webDeploymentBuilder.getClassLoader());
        d.setResourceLoader(new FileResourceLoader(webDeploymentBuilder.getDocumentRoot()));
        for (ServletBuilder servlet : webDeploymentBuilder.getServlets()) {
            ServletInfo s;
            if (servlet.getServlet() == null) {
                s = new ServletInfo(servlet.getServletName(), (Class<? extends Servlet>) servlet.getServletClass());
            } else {
                s = new ServletInfo(servlet.getServletName(), (Class<? extends Servlet>) servlet.getServletClass(), new ImmediateInstanceFactory<>(servlet.getServlet()));
            }
            s.addMappings(servlet.getUrlMappings());
            for (Map.Entry<String, String> param : servlet.getInitParams().entrySet()) {
                s.addInitParam(param.getKey(), param.getValue());
            }
            d.addServlet(s);
        }

        return new WebDeploymentControllerImpl(d);
    }

    private class WebDeploymentControllerImpl implements WebDeploymentController {

        private final DeploymentInfo deploymentInfo;
        private volatile DeploymentManager manager;

        private WebDeploymentControllerImpl(final DeploymentInfo deploymentInfo) {
            this.deploymentInfo = deploymentInfo;
        }

        @Override
        public void create() throws Exception {
            ServletContainer container = getServer().getValue().getServletContainer().getValue().getServletContainer();
            manager = container.addDeployment(deploymentInfo);
            manager.deploy();
        }

        @Override
        public void start() throws Exception {
            manager.start();
        }

        @Override
        public void stop() throws Exception {
            manager.stop();
        }

        @Override
        public void destroy() throws Exception {
            manager.undeploy();
            ServletContainer container = getServer().getValue().getServletContainer().getValue().getServletContainer();
            container.removeDeployment(deploymentInfo.getDeploymentName());
        }
    }

}