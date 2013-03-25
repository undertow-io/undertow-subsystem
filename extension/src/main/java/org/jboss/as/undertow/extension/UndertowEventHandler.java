package org.jboss.as.undertow.extension;

import io.undertow.servlet.api.DeploymentInfo;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public interface UndertowEventHandler {
    void onStartup(UndertowService undertowService);

    void onShutdown();

    void onAddDeployment(DeploymentInfo deploymentInfo);

    void onDeploymentStart(DeploymentInfo deploymentInfo);

    void onDeploymentStop(DeploymentInfo deploymentInfo);

    void onDeploymentRemove(DeploymentInfo deploymentInfo);

    void onHostAdd(Host host);

    void onHostRemove(Host host);

    void onHostStart(Host host);

    void onHostStop(Host host);

    void onServerAdd(Server server);

    void onServerRemove(Server server);

    void onServerStart(Server server);

    void onServerStop(Server server);
}
