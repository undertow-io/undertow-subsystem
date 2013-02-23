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

import org.jboss.as.network.ManagedBinding;
import org.jboss.as.network.SocketBinding;
import org.jboss.msc.service.Service;
import org.jboss.msc.value.InjectedValue;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Pool;
import org.xnio.XnioWorker;

/**
 * @author Tomaz Cerar
 */
public abstract class AbstractListenerService<T> implements Service<T> {

    protected final InjectedValue<ServletContainerService> container = new InjectedValue<>();
    protected final InjectedValue<XnioWorker> worker = new InjectedValue<>();
    protected final InjectedValue<SocketBinding> binding = new InjectedValue<>();
    protected final InjectedValue<Pool> bufferPool = new InjectedValue<>();

    OptionMap serverOptions = OptionMap.builder()
            .set(Options.WORKER_ACCEPT_THREADS, Runtime.getRuntime().availableProcessors())
            .set(Options.TCP_NODELAY, true)
            .set(Options.REUSE_ADDRESSES, true)
            .getMap();


    public InjectedValue<XnioWorker> getWorker() {
        return worker;
    }

    public InjectedValue<SocketBinding> getBinding() {
        return binding;
    }

    public InjectedValue<ServletContainerService> getContainer() {
        return container;
    }

    public InjectedValue<Pool> getBufferPool() {
        return bufferPool;
    }

    protected int getBufferSize() {
        return 1024;
    }


    protected void registerBinding() {
        binding.getValue().getSocketBindings().getNamedRegistry().registerBinding(new ListenerBinding(binding.getValue()));
        UndertowLogger.ROOT_LOGGER.infof("registering binding: %s", binding.getValue());
    }

    protected void unRegisterBinding() {
        final SocketBinding binding = this.binding.getValue();
        binding.getSocketBindings().getNamedRegistry().unregisterBinding(binding.getName());
    }

    private static class ListenerBinding implements ManagedBinding {

        private final SocketBinding binding;

        private ListenerBinding(final SocketBinding binding) {
            this.binding = binding;
        }

        @Override
        public String getSocketBindingName() {
            return binding.getName();
        }

        @Override
        public InetSocketAddress getBindAddress() {
            return binding.getSocketAddress();
        }

        @Override
        public void close() throws IOException {

        }
    }
}
