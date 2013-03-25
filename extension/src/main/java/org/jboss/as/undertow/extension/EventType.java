package org.jboss.as.undertow.extension;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2013 Red Hat Inc.
 */
public enum EventType {
    SERVER_ADD,
    SERVER_REMOVE,
    SERVER_START,
    SERVER_STOP,
    HOST_ADD,
    HOST_REMOVE,
    HOST_START,
    HOST_STOP
}
