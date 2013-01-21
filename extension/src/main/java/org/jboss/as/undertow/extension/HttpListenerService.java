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

package org.jboss.as.undertow.extension;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.jboss.as.network.SocketBinding;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.xnio.ChannelListener;
import org.xnio.IoUtils;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.XnioWorker;
import org.xnio.channels.AcceptingChannel;
import org.xnio.channels.ConnectedStreamChannel;

/**
 * @author Stuart Douglas
 */
public class HttpListenerService implements Service<HttpListenerService> {

    private final InjectedValue<UndertowContainerService> container = new InjectedValue<UndertowContainerService>();
    private final InjectedValue<XnioWorker> worker = new InjectedValue<XnioWorker>();
    private final InjectedValue<SocketBinding> binding = new InjectedValue<SocketBinding>();

    protected static final OptionMap SERVER_OPTIONS = OptionMap.builder()
                                                     .set(Options.WORKER_ACCEPT_THREADS, 3)
                                                     .set(Options.TCP_NODELAY, true)
                                                     .set(Options.REUSE_ADDRESSES, true)
                                                     .getMap();

    private volatile AcceptingChannel<? extends ConnectedStreamChannel> server;

    @Override
    public void start(final StartContext startContext) throws StartException {
        try {
            final InetSocketAddress socketAddress = binding.getValue().getSocketAddress();
            startListening(worker.getValue(), socketAddress, container.getValue().getAcceptListener());
        } catch (IOException e) {
            throw new StartException("Could not start http listener", e);
        }
    }

    @Override
    public void stop(final StopContext stopContext) {
        stopListening();
    }

    protected void startListening(final XnioWorker worker, final InetSocketAddress socketAddress,
                                  final ChannelListener acceptListener) throws IOException {
        server = worker.createStreamServer(socketAddress, acceptListener, SERVER_OPTIONS);
        server.resumeAccepts();

        UndertowMessages.MESSAGES.listenerStarted("Http listener", socketAddress);
    }

    protected void stopListening() {
        IoUtils.safeClose(server);
        server = null;
    }

    @Override
    public HttpListenerService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public InjectedValue<XnioWorker> getWorker() {
        return worker;
    }

    public InjectedValue<SocketBinding> getBinding() {
        return binding;
    }

    public InjectedValue<UndertowContainerService> getContainer() {
        return container;
    }
}
