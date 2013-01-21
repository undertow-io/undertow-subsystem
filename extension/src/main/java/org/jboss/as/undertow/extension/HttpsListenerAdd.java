/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.as.undertow.extension;

import static org.jboss.as.undertow.extension.HttpsListenerResourceDefinition.SECURITY_REALM;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.domain.management.SecurityRealm;
import org.jboss.as.domain.management.security.SecurityRealmService;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;

/**
 * Add handler for HTTPS listeners.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class HttpsListenerAdd extends HttpListenerAdd {
    static final HttpsListenerAdd INSTANCE = new HttpsListenerAdd();

    HttpsListenerAdd() {
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (SimpleAttributeDefinition attr : HttpsListenerResourceDefinition.ATTRIBUTES) {
            attr.validateAndSet(operation, model);
        }
    }

    @Override
    protected ServiceName constructServiceName(String name) {
        return WebSubsystemServices.HTTPS_LISTENER.append(name);
    }

    @Override
    protected HttpListenerService createService() {
        return new HttpsListenerService();
    }

    @Override
    protected void additionalDependencies(OperationContext context, ServiceBuilder<HttpListenerService> serviceBuilder, ModelNode model, HttpListenerService service) throws OperationFailedException {
        final String securityRealm = SECURITY_REALM.resolveModelAttribute(context, model).asString();

        serviceBuilder.addDependency(SecurityRealmService.BASE_SERVICE_NAME.append(securityRealm), SecurityRealm.class, ((HttpsListenerService)service).getSecurityRealm());
    }





}
