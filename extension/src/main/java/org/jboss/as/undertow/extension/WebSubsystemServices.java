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

    public static final ServiceName CONTAINER = UNDERTOW.append("container");

    /** The base name for jboss.web connector services. */
    public static final ServiceName HTTP_LISTENER = UNDERTOW.append("http-listener");
    public static final ServiceName HTTPS_LISTENER = UNDERTOW.append("https-listener");

    /** The base name for jboss.web deployments. */
    static final ServiceName JBOSS_WEB_DEPLOYMENT_BASE = UNDERTOW.append("deployment");

    public static ServiceName deploymentServiceName(final String virtualHost, final String contextPath) {
        return JBOSS_WEB_DEPLOYMENT_BASE.append(virtualHost).append("".equals(contextPath) ? "/" : contextPath);
    }

    private WebSubsystemServices() {
    }
}
