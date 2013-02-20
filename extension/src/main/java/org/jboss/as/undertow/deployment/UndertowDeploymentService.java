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
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.api.ConfidentialPortManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.jboss.as.security.plugins.SecurityDomainContext;
import org.jboss.as.undertow.extension.ServletContainerService;
import org.jboss.as.undertow.security.IdentityManagerImpl;
import org.jboss.as.web.common.StartupContext;
import org.jboss.as.web.common.WebInjectionContainer;
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
    private final InjectedValue<ServletContainerService> container = new InjectedValue<>();
    private final WebInjectionContainer webInjectionContainer;
    private final InjectedValue<SecurityDomainContext> securityDomainContextValue = new InjectedValue<SecurityDomainContext>();
    private volatile DeploymentManager deploymentManager;

    public UndertowDeploymentService(final DeploymentInfo deploymentInfo, final WebInjectionContainer webInjectionContainer) {
        this.deploymentInfo = deploymentInfo;
        this.webInjectionContainer = webInjectionContainer;
    }

    @Override
    public void start(final StartContext startContext) throws StartException {

        deploymentInfo.setIdentityManager(new IdentityManagerImpl(securityDomainContextValue.getValue(), deploymentInfo.getPrincipleVsRoleMapping()));
        deploymentInfo.setConfidentialPortManager(getConfidentialPortManager());
        StartupContext.setInjectionContainer(webInjectionContainer);
        try {
            deploymentManager = container.getValue().getServletContainer().addDeployment(deploymentInfo);
            deploymentManager.deploy();
            try {
                HttpHandler handler = deploymentManager.start();
                container.getValue().getPathHandler().addPath(deploymentInfo.getContextPath(), handler);
            } catch (ServletException e) {
                throw new StartException(e);
            }
        } finally {
            StartupContext.setInjectionContainer(null);
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
        deploymentInfo.setIdentityManager(null);
    }

    @Override
    public UndertowDeploymentService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public InjectedValue<ServletContainerService> getContainer() {
        return container;
    }

    public InjectedValue<SecurityDomainContext> getSecurityDomainContextValue() {
        return securityDomainContextValue;
    }

    private ConfidentialPortManager getConfidentialPortManager() {
        return new ConfidentialPortManager() {

            @Override
            public int getConfidentialPort(HttpServerExchange exchange) {
                return container.getValue().lookupSecurePort("default");
            }
        };
    }
}
