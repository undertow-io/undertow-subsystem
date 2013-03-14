/**
 *
 */
package org.jboss.as.undertow.extension;

import org.jboss.msc.service.ServiceName;

/**
 * Service name constants.
 *
 * @author Tomaz Cerar
 * @author Stuart Douglas
 */
public final class UndertowServices {

    public static final ServiceName UNDERTOW = ServiceName.JBOSS.append("undertow");

    public static final ServiceName WORKER = UNDERTOW.append("worker");

    public static final ServiceName SERVLET_CONTAINER = UNDERTOW.append(Constants.SERVLET_CONTAINER);

    public static final ServiceName SERVER = UNDERTOW.append(Constants.SERVER);

    /**
     * The base name for jboss.web connector services.
     */
    public static final ServiceName AJP_LISTENER = UNDERTOW.append("ajp-listener");
    public static final ServiceName HTTP_LISTENER = UNDERTOW.append("http-listener");
    public static final ServiceName HTTPS_LISTENER = UNDERTOW.append("https-listener");

    public static final ServiceName BUFFER_POOL = UNDERTOW.append("buffer-pool");

    /**
     * The base name for jboss.web deployments.
     */
    static final ServiceName WEB_DEPLOYMENT_BASE = UNDERTOW.append("deployment");

    public static ServiceName deploymentServiceName(final String virtualHost, final String contextPath) {
        return WEB_DEPLOYMENT_BASE.append(virtualHost).append("".equals(contextPath) ? "/" : contextPath);
    }
    public static ServiceName virtualHostName(final String server, final String virtualHost) {
        return SERVER.append(server).append(Constants.HOST).append(virtualHost);
    }
    public static ServiceName locationServiceName(final String server, final String virtualHost, final String locationName) {
        return virtualHostName(server,virtualHost).append(Constants.LOCATION,locationName);
    }

    private UndertowServices() {
    }
}
