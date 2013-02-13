package org.jboss.as.undertow.security;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.HttpHandlers;
import io.undertow.util.WorkerDispatcher;
import org.jboss.security.SecurityContext;

/**
 * Handler that creates the security context and attaches it to the exchange. It is not associated with the current
 * thread at this point, as request may not have been dispatched into the final thread that will actually run the
 * servlet request.
 *
 * @author Stuart Douglas
 */
public class SecurityContextCreationHandler implements HttpHandler {

    private final String securityDomain;
    private final HttpHandler next;

    public SecurityContextCreationHandler(final String securityDomain, final HttpHandler next) {
        this.securityDomain = securityDomain;
        this.next = next;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) {
        //TODO: we should not have to dispact here, once we get rid of the thread local stuff a bit
        WorkerDispatcher.dispatch(exchange, new Runnable() {
            @Override
            public void run() {
                try {
                    final SecurityContext sc = SecurityActions.createSecurityContext(securityDomain);
                    exchange.putAttachment(UndertowSecurityAttachments.SECURITY_CONTEXT_ATTACHMENT, sc);
                    SecurityActions.setSecurityContextOnAssociation(sc);

                    HttpHandlers.executeHandler(next, exchange);

                } finally {
                    SecurityActions.clearSecurityContext();
                }
            }
        });


    }

    public static final HandlerWrapper<HttpHandler> wrapper(final String securityDomain) {
        return new HandlerWrapper<HttpHandler>() {
            @Override
            public HttpHandler wrap(final HttpHandler handler) {
                return new SecurityContextCreationHandler(securityDomain, handler);
            }
        };
    }
}
