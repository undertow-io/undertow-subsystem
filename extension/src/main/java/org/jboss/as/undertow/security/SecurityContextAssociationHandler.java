/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.as.undertow.security;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.security.jacc.PolicyContext;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.blocking.BlockingHttpHandler;
import io.undertow.servlet.handlers.ServletAttachments;
import io.undertow.servlet.handlers.ServletPathMatch;
import org.jboss.as.undertow.extension.UndertowLogger;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityRolesAssociation;

public class SecurityContextAssociationHandler implements BlockingHttpHandler {

    private final Map<String, Set<String>> principleVsRoleMap;
    private final String contextId;
    private final BlockingHttpHandler next;

    public SecurityContextAssociationHandler(final Map<String, Set<String>> principleVsRoleMap, final String contextId, final BlockingHttpHandler next) {
        this.principleVsRoleMap = principleVsRoleMap;
        this.contextId = contextId;
        this.next = next;
    }

    @Override
    public void handleBlockingRequest(final HttpServerExchange exchange) throws Exception {
        SecurityContext sc = exchange.getAttachment(UndertowSecurityAttachments.SECURITY_CONTEXT_ATTACHMENT);
        String previousContextID = null;
        String identity = null;
        try {
            SecurityActions.setSecurityContextOnAssociation(sc);
            ServletPathMatch servlet = exchange.getAttachment(ServletAttachments.SERVLET_PATH_MATCH);
            identity = servlet.getHandler().getManagedServlet().getServletInfo().getRunAs();
            RunAsIdentity runAsIdentity = null;
            if (identity != null) {
                UndertowLogger.ROOT_LOGGER.tracef("%s, runAs: %s", servlet.getHandler().getManagedServlet().getServletInfo().getName(), identity);
                final Set<String> roles = principleVsRoleMap.get(identity);
                runAsIdentity = new RunAsIdentity(identity, identity, roles == null ? Collections.<String>emptySet() : roles);
            }
            SecurityActions.pushRunAsIdentity(runAsIdentity);

            // set JACC contextID
            previousContextID = setContextID(contextId);

            // Perform the request
            next.handleBlockingRequest(exchange);
        } finally {
            if (identity != null) {
                SecurityActions.popRunAsIdentity();
            }
            SecurityActions.clearSecurityContext();
            SecurityRolesAssociation.setSecurityRoles(null);
            setContextID(previousContextID);
        }
    }

    private static class SetContextIDAction implements PrivilegedAction<String> {

        private String contextID;

        SetContextIDAction(String contextID) {
            this.contextID = contextID;
        }

        @Override
        public String run() {
            String currentContextID = PolicyContext.getContextID();
            PolicyContext.setContextID(this.contextID);
            return currentContextID;
        }
    }

    private String setContextID(String contextID) {
        PrivilegedAction<String> action = new SetContextIDAction(contextID);
        return AccessController.doPrivileged(action);
    }


    public static HandlerWrapper<BlockingHttpHandler> wrapper(final Map<String, Set<String>> principleVsRoleMap, final String contextId) {
        return new HandlerWrapper<BlockingHttpHandler>() {
            @Override
            public BlockingHttpHandler wrap(final BlockingHttpHandler handler) {
                return new SecurityContextAssociationHandler(principleVsRoleMap, contextId, handler);
            }
        };
    }
}
