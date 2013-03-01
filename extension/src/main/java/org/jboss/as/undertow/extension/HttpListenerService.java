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

import io.undertow.server.HttpOpenListener;
import io.undertow.server.OpenListener;
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
public class HttpListenerService extends AbstractListenerService<HttpListenerService> {
    protected static final OptionMap SERVER_OPTIONS = OptionMap.builder()
            .set(Options.WORKER_ACCEPT_THREADS, 3)
            .set(Options.TCP_NODELAY, true)
            .set(Options.REUSE_ADDRESSES, true)
            .getMap();

    /*private volatile HttpOpenListener openListener;
    private volatile ChannelListener<? super AcceptingChannel<ConnectedStreamChannel>> acceptListener;*/
    private volatile AcceptingChannel<? extends ConnectedStreamChannel> server;

    /*@Override
    public void start(final StartContext startContext) throws StartException {
        try {
            final InetSocketAddress socketAddress = binding.getValue().getSocketAddress();
            openListener = new HttpOpenListener(getBufferPool().getValue(), getBufferSize());
            acceptListener = ChannelListeners.openListenerAdapter(openListener);
            FormEncodedDataHandler formEncodedDataHandler = new FormEncodedDataHandler();
            formEncodedDataHandler.setNext(container.getValue().getPathHandler());
            MultiPartHandler multiPartHandler = new MultiPartHandler();
            multiPartHandler.setNext(formEncodedDataHandler);
            final CookieHandler cookie = new CookieHandler();
            cookie.setNext(new SimpleErrorPageHandler(multiPartHandler));
            CanonicalPathHandler canonicalPathHandler = new CanonicalPathHandler(cookie);
            openListener.setRootHandler(canonicalPathHandler);
            startListening(worker.getValue(), socketAddress, acceptListener);

            registerBinding();
        } catch (IOException e) {
            throw new StartException("Could not start http listener", e);
        }
    }*/

    @Override
    protected OpenListener createOpenListener() {
        return new HttpOpenListener(getBufferPool().getValue(), getBufferSize());
    }

    protected void startListening(XnioWorker worker, InetSocketAddress socketAddress, ChannelListener<? super AcceptingChannel<ConnectedStreamChannel>> acceptListener)
            throws IOException {
        server = worker.createStreamServer(socketAddress, acceptListener, SERVER_OPTIONS);
        server.resumeAccepts();
        UndertowLogger.ROOT_LOGGER.listenerStarted("Http listener", socketAddress);
    }

    @Override
    protected void stopListening() {
        IoUtils.safeClose(server);
        server = null;
    }

    @Override
    public HttpListenerService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

}
