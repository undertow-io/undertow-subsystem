/**
 *
 */
package org.jboss.as.undertow.extension;

import org.jboss.msc.service.ServiceName;

/**
 * Service name constants.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 * @author Emanuel Muckenhuber
 */
public final class WebSubsystemServices {

    public static final ServiceName UNDERTOW = ServiceName.JBOSS.append("undertow");

    public static final ServiceName XNIO_WORKER = UNDERTOW.append("worker");

    /** The base name for jboss.web connector services. */
    public static final ServiceName LISTENER = UNDERTOW.append("listener");

    /** The base name for jboss.web deployments. */
    static final ServiceName JBOSS_WEB_DEPLOYMENT_BASE = UNDERTOW.append("deployment");

    public static ServiceName deploymentServiceName(final String virtualHost, final String contextPath) {
        return JBOSS_WEB_DEPLOYMENT_BASE.append(virtualHost).append("".equals(contextPath) ? "/" : contextPath);
    }

    private WebSubsystemServices() {
    }
}
