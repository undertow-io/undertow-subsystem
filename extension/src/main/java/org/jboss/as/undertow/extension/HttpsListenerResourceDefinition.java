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

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.dmr.ModelType;

/**
 * An extension to the {@see HttpListenerResourceDefinition} to allow a security-realm to be associated to obtain a pre-defined
 * SSLContext.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class HttpsListenerResourceDefinition extends HttpListenerResourceDefinition {
    protected static final HttpsListenerResourceDefinition INSTANCE = new HttpsListenerResourceDefinition();

    protected static final SimpleAttributeDefinition SECURITY_REALM = new SimpleAttributeDefinitionBuilder(
            Constants.SECURITY_REALM, ModelType.STRING).setAllowNull(false).setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setValidator(new StringLengthValidator(1)).build();

    protected static final SimpleAttributeDefinition[] ATTRIBUTES = initialiseAttributes();

    private static SimpleAttributeDefinition[] initialiseAttributes() {
        SimpleAttributeDefinition[] ATTRIBUTES = new SimpleAttributeDefinition[HttpListenerResourceDefinition.ATTRIBUTES.length + 1];
        System.arraycopy(HttpListenerResourceDefinition.ATTRIBUTES, 0, ATTRIBUTES, 0,
                HttpListenerResourceDefinition.ATTRIBUTES.length);
        ATTRIBUTES[ATTRIBUTES.length - 1] = SECURITY_REALM;

        return ATTRIBUTES;
    }

    private HttpsListenerResourceDefinition() {
        super(UndertowExtension.HTTPS_LISTENER_PATH,
                UndertowExtension.getResolver(Constants.HTTPS_LISTENER), HttpsListenerAdd.INSTANCE);
    }

    @Override
    protected SimpleAttributeDefinition[] getAttributeDefinitions() {
        return ATTRIBUTES;
    }

}
