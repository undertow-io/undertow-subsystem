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

import static org.xnio.Options.SSL_CLIENT_AUTH_MODE;
import static org.xnio.SslClientAuthMode.REQUESTED;

import java.io.IOException;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLContext;

import org.jboss.as.domain.management.AuthenticationMechanism;
import org.jboss.as.domain.management.SecurityRealm;
import org.jboss.msc.value.InjectedValue;
import org.xnio.ChannelListener;
import org.xnio.IoUtils;
import org.xnio.OptionMap;
import org.xnio.OptionMap.Builder;
import org.xnio.XnioWorker;
import org.xnio.channels.AcceptingChannel;
import org.xnio.channels.ConnectedSslStreamChannel;
import org.xnio.ssl.JsseXnioSsl;
import org.xnio.ssl.XnioSsl;

/**
 * An extension of {@see HttpListenerService} to add SSL.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class HttpsListenerService extends HttpListenerService {

    private final String name;
    private final InjectedValue<SecurityRealm> securityRealm = new InjectedValue<SecurityRealm>();

    private AcceptingChannel<ConnectedSslStreamChannel> sslServer;

    HttpsListenerService(final String name) {
        this.name = name;
    }

    @Override
    protected void startListening(XnioWorker worker, InetSocketAddress socketAddress, ChannelListener acceptListener)
            throws IOException {

        SSLContext sslContext = securityRealm.getValue().getSSLContext();
        Builder builder = OptionMap.builder().addAll(SERVER_OPTIONS);
        if (securityRealm.getValue().getSupportedAuthenticationMechanisms().contains(AuthenticationMechanism.CLIENT_CERT)) {
            builder.set(SSL_CLIENT_AUTH_MODE, REQUESTED);
        }
        OptionMap combined = builder.getMap();

        XnioSsl xnioSsl = new JsseXnioSsl(worker.getXnio(), combined, sslContext);
        sslServer = xnioSsl.createSslTcpServer(worker, socketAddress, acceptListener, combined);
        sslServer.resumeAccepts();

        container.getValue().registerSecurePort(name, socketAddress.getPort());

        UndertowLogger.ROOT_LOGGER.listenerStarted("Https listener", socketAddress);
    }

    @Override
    protected void stopListening() {
        container.getValue().unregisterSecurePort(name);
        IoUtils.safeClose(sslServer);
        sslServer = null;
    }

    public InjectedValue<SecurityRealm> getSecurityRealm() {
        return securityRealm;
    }

}
