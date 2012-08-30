package org.jboss.as.undertow.extension;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import io.undertow.server.HttpOpenListener;
import io.undertow.server.HttpTransferEncodingHandler;
import io.undertow.server.handlers.CanonicalPathHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.server.handlers.file.CachingFileCache;
import io.undertow.server.handlers.file.FileHandler;
import org.jboss.as.network.SocketBinding;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.xnio.BufferAllocator;
import org.xnio.ByteBufferSlicePool;
import org.xnio.ChannelListener;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.XnioWorker;
import org.xnio.channels.AcceptingChannel;
import org.xnio.channels.ConnectedStreamChannel;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class ListenerService implements Service<ChannelListener> {
    //this is temporary
    private String path;

    private final InjectedValue<SocketBinding> binding = new InjectedValue<SocketBinding>();
    private final InjectedValue<XnioWorker> worker = new InjectedValue<XnioWorker>();

    final OptionMap map = OptionMap.builder()
            .set(Options.WORKER_WRITE_THREADS, 4)
            .set(Options.WORKER_READ_THREADS, 4)
            .set(Options.CONNECTION_LOW_WATER, 1000000)
            .set(Options.CONNECTION_HIGH_WATER, 1000000)
            .set(Options.WORKER_TASK_CORE_THREADS, 10)
            .set(Options.WORKER_TASK_MAX_THREADS, 16)
            .set(Options.TCP_NODELAY, true)
            .getMap();

    public ListenerService(String path) {
        this.path = path;
    }

    public InjectedValue<SocketBinding> getBinding() {
        return binding;
    }

    public InjectedValue<XnioWorker> getWorker() {
        return worker;
    }

    @Override
    public void start(StartContext startContext) throws StartException {

        final SocketBinding binding = this.binding.getValue();
        final InetSocketAddress address = binding.getSocketAddress();
        final XnioWorker worker = this.worker.getValue();
        final HttpOpenListener openListener = new HttpOpenListener(new ByteBufferSlicePool(BufferAllocator.DIRECT_BYTE_BUFFER_ALLOCATOR, 8192, 8192 * 8192));
        final FileHandler fileHandler = new FileHandler(new File(path));
        //fileHandler.setFileCache(new PermanentFileCache());
        //fileHandler.setFileCache(new DirectFileCache());
        fileHandler.setFileCache(new CachingFileCache(1024, 10480));
        openListener.setRootHandler(new HttpTransferEncodingHandler(new CanonicalPathHandler(new SimpleErrorPageHandler(fileHandler))));
        try {
            AcceptingChannel channel = worker.createStreamServer(address, new ChannelListener<AcceptingChannel<ConnectedStreamChannel>>() {
                public void handleEvent(final AcceptingChannel<ConnectedStreamChannel> channel) {
                    try {
                        final ConnectedStreamChannel accept = channel.accept();
                        if (accept == null) {
                            return;
                        }
                        openListener.handleEvent(accept);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, map);

            channel.resumeAccepts();
        } catch (IOException e) {
            throw new StartException("Could not start listener", e);
        }
    }

    @Override
    public void stop(StopContext stopContext) {

    }

    @Override
    public ChannelListener getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }
}
