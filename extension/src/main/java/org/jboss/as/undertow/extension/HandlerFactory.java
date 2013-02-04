package org.jboss.as.undertow.extension;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public class HandlerFactory {
    private static Map<String, Handler> handlerMap = new HashMap<>();
    private static List<Handler> handlers = new LinkedList<>();

    static {
        loadRegisteredHandlers();
    }

    private static void loadRegisteredHandlers() {
        ServiceLoader<Handler> loader = ServiceLoader.load(Handler.class);

        //todo use module loader
        //final Module module = Module.getCallerModule();
        //for (final Handler handler : module.loadService(Handler.class)) {
        for (final Handler handler : loader) {
            handlers.add(handler);
            handlerMap.put(handler.getName(), handler);
        }
    }

    public static Map<String, Handler> getHandlerMap() {
        return handlerMap;
    }

    public static List<Handler> getHandlers() {
        return handlers;
    }
}
