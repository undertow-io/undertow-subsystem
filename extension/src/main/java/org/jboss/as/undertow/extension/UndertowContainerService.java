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

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpOpenListener;
import io.undertow.server.HttpTransferEncodingHandler;
import io.undertow.server.handlers.CanonicalPathHandler;
import io.undertow.server.handlers.CookieHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.server.handlers.form.FormEncodedDataHandler;
import io.undertow.server.handlers.form.MultiPartHandler;
import io.undertow.servlet.api.ServletContainer;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.xnio.BufferAllocator;
import org.xnio.ByteBufferSlicePool;
import org.xnio.ChannelListener;
import org.xnio.ChannelListeners;
import org.xnio.channels.AcceptingChannel;
import org.xnio.channels.ConnectedStreamChannel;

/**
 * Central Undertow 'Container' HTTP listeners will make this container accessible whilst deployers will add content.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class UndertowContainerService implements Service<UndertowContainerService> {

    private volatile HttpOpenListener openListener;
    private volatile ChannelListener<? super AcceptingChannel<ConnectedStreamChannel>> acceptListener;
    private volatile PathHandler pathHandler = new PathHandler();
    private volatile ServletContainer servletContainer;

    /*
     * Service Methods
     */

    public void start(StartContext arg0) throws StartException {
        //TODO: make this configurable, and use a more realistic buffer size by default.
        //this is only this large to work around an XNIO bug
        openListener = new HttpOpenListener(new ByteBufferSlicePool(BufferAllocator.DIRECT_BYTE_BUFFER_ALLOCATOR, 8192, 8192 * 8192), 8192);
        acceptListener = ChannelListeners.openListenerAdapter(openListener);

        FormEncodedDataHandler formEncodedDataHandler = new FormEncodedDataHandler();
        formEncodedDataHandler.setNext(pathHandler);
        MultiPartHandler multiPartHandler = new MultiPartHandler();
        multiPartHandler.setNext(formEncodedDataHandler);
        final CookieHandler cookie = new CookieHandler();
        cookie.setNext(new SimpleErrorPageHandler(multiPartHandler));
        CanonicalPathHandler canonicalPathHandler = new CanonicalPathHandler(cookie);
        final HttpTransferEncodingHandler transferEncodingHandler = new HttpTransferEncodingHandler(canonicalPathHandler);
        openListener.setRootHandler(transferEncodingHandler);

        servletContainer = ServletContainer.Factory.newInstance();
    }

    public void stop(StopContext arg0) {

    }

    public UndertowContainerService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    /*
     * Access Methods
     */

    public HttpHandler getRootHandler() {
        return openListener.getRootHandler();
    }

    public void setRootHandler(HttpHandler handler) {
        openListener.setRootHandler(handler);
    }

    public ChannelListener<? super AcceptingChannel<ConnectedStreamChannel>> getAcceptListener() {
        return acceptListener;
    }

    public PathHandler getPathHandler() {
        return pathHandler;
    }

    public void setPathHandler(final PathHandler pathHandler) {
        this.pathHandler = pathHandler;
    }

    public ServletContainer getServletContainer() {
        return servletContainer;
    }

}
