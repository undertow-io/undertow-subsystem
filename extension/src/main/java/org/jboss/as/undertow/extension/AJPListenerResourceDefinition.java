package org.jboss.as.undertow.extension;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class AJPListenerResourceDefinition extends AbstractListenerResourceDefinition {
    protected static final AJPListenerResourceDefinition INSTANCE = new AJPListenerResourceDefinition();


    private AJPListenerResourceDefinition() {
        super(UndertowExtension.AJP_LISTENER_PATH, HttpListenerAdd.INSTANCE);
    }


}
