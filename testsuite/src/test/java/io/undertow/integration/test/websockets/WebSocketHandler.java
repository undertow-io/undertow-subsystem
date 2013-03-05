package io.undertow.integration.test.websockets;

import java.io.IOException;

import io.undertow.websockets.api.AbstractAssembledFrameHandler;
import io.undertow.websockets.api.WebSocketFrameHeader;
import io.undertow.websockets.api.WebSocketSession;
import io.undertow.websockets.api.WebSocketSessionHandler;
import io.undertow.websockets.spi.WebSocketHttpExchange;

/**
 * @author Stuart Douglas
 */
public class WebSocketHandler implements WebSocketSessionHandler {
    @Override
    public void onSession(final WebSocketSession session, final WebSocketHttpExchange exchange) {
        session.setFrameHandler(new AbstractAssembledFrameHandler() {
            @Override
            public void onTextFrame(final WebSocketSession session, final WebSocketFrameHeader header, final CharSequence payload) {
                try {
                    session.sendText("Hello " + payload);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
