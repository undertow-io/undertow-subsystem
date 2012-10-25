/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.undertow.deployment;

import javax.servlet.ServletException;

import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.jboss.as.undertow.extension.HttpListenerService;
import org.jboss.as.web.deployment.WebInjectionContainer;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Stuart Douglas
 */
public class UndertowDeploymentService implements Service<UndertowDeploymentService> {

    private final DeploymentInfo deploymentInfo;
    private final InjectedValue<HttpListenerService> connector = new InjectedValue<HttpListenerService>();
    private final WebInjectionContainer webInjectionContainer;
    private volatile DeploymentManager deploymentManager;

    public UndertowDeploymentService(final DeploymentInfo deploymentInfo, final WebInjectionContainer webInjectionContainer) {
        this.deploymentInfo = deploymentInfo;
        this.webInjectionContainer = webInjectionContainer;
    }

    @Override
    public void start(final StartContext startContext) throws StartException {
        WebInjectionContainer.setCurrentInjectionContainer(webInjectionContainer);

        try {
            deploymentManager = connector.getValue().getServletContainer().addDeployment(deploymentInfo);
            deploymentManager.deploy();
            try {
                HttpHandler handler = deploymentManager.start();
                connector.getValue().getPathHandler().addPath(deploymentInfo.getContextPath(), handler);
            } catch (ServletException e) {
                throw new StartException(e);
            }
        } finally {
            WebInjectionContainer.setCurrentInjectionContainer(null);
        }
    }

    @Override
    public void stop(final StopContext stopContext) {
        try {
            deploymentManager.stop();
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        deploymentManager.undeploy();
    }

    @Override
    public UndertowDeploymentService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public InjectedValue<HttpListenerService> getConnector() {
        return connector;
    }
}
