package org.jboss.as.undertow.extension;

import java.io.IOException;

import io.undertow.ajp.AjpOpenListener;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.xnio.ChannelListener;
import org.xnio.ChannelListeners;
import org.xnio.channels.AcceptingChannel;
import org.xnio.channels.ConnectedStreamChannel;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class AJPListenerService extends AbstractListenerService<AJPListenerService> {
    @Override
    public void start(StartContext context) throws StartException {
        try {
            AjpOpenListener openListener = new AjpOpenListener(getBuffers(), getBufferSize());
            //openListener.setRootHandler(rootHandler);
            ChannelListener<AcceptingChannel<ConnectedStreamChannel>> acceptListener = ChannelListeners.openListenerAdapter(openListener);
            AcceptingChannel<? extends ConnectedStreamChannel> server = worker.getValue().createStreamServer(binding.getValue().getSocketAddress(), acceptListener, serverOptions);
            server.resumeAccepts();
            UndertowMessages.MESSAGES.listenerStarted("AJP listener", binding.getValue().getSocketAddress());
        } catch (IOException e) {
            throw new StartException("Could not start ajp listener", e);
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public AJPListenerService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }
}
