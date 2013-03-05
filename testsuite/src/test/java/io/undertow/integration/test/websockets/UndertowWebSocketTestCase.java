package io.undertow.integration.test.websockets;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
@RunAsClient
public class UndertowWebSocketTestCase {

    @ArquillianResource
    private URL url;

    @Deployment
    public static WebArchive deploy() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "WebSocket.war");
        archive.addClass(WebSocketHandler.class);
        archive.addAsWebInfResource(UndertowWebSocketTestCase.class.getPackage(), "web.xml", "web.xml");
        return archive;
    }

    @Test
    public void test() throws Exception {
        final AtomicReference<String> result = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        WebSocketTestClient client = new WebSocketTestClient(WebSocketVersion.V13, new URI("ws://localhost:8080/WebSocket"));
        client.connect();
        client.send(new TextWebSocketFrame("Stuart"), new WebSocketTestClient.FrameListener() {
            @Override
            public void onFrame(final WebSocketFrame frame) {
                result.set(((TextWebSocketFrame) frame).getText());
                latch.countDown();
            }

            @Override
            public void onError(final Throwable t) {
                latch.countDown();
            }
        });
        latch.await();
        assertEquals("Hello Stuart", result.get());
    }

}
