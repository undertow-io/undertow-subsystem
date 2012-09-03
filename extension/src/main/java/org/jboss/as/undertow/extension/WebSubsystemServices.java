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

    /** The base name for jboss.web services. */
    public static final ServiceName XNIO_WORKER = ServiceName.JBOSS.append("xnioWorker");
    /** The jboss.web server name, there can only be one. */
    public static final ServiceName WEB_SERVER = XNIO_WORKER.append("server");
    /** The base name for jboss.web connector services. */
    public static final ServiceName LISTENER = XNIO_WORKER.append("listener");
    /** The base name for jboss.web host services. */
    public static final ServiceName WEB_HOST = XNIO_WORKER.append("host");
    /** The base name for jboss.web deployments. */
    static final ServiceName JBOSS_WEB_DEPLOYMENT_BASE = XNIO_WORKER.append("deployment");

    public static ServiceName deploymentServiceName(final String virtualHost, final String contextPath) {
        return JBOSS_WEB_DEPLOYMENT_BASE.append(virtualHost).append("".equals(contextPath) ? "/" : contextPath);
    }

    private WebSubsystemServices() {
    }
}
