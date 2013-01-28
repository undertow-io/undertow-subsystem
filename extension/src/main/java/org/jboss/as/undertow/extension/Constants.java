/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import org.jboss.as.controller.PropertiesAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public interface Constants {
    String HANDLER_FACTORY= "handler-factory";
    String HANDLER_CHAIN = "handler-chain";
    String SECURITY_REALM = "security-realm";
    String SOCKET_BINDING = "socket-binding";
    String PATH = "path";
    String HTTP_LISTENER = "http-listener";
    String HTTPS_LISTENER = "https-listener";
    String NAME = "name";
    String WORKER = "worker";

    String WORKER_READ_THREADS = "read-threads";
    String WORKER_TASK_CORE_THREADS = "task-core-threads";
    String WORKER_TASK_KEEPALIVE = "task-keepalive";
    String WORKER_TASK_LIMIT = "task-limit";
    String WORKER_TASK_MAX_THREADS = "task-max-threads";
    String WORKER_WRITE_THREADS = "write-threads";
    String THREAD_DAEMON = "thread-daemon";
    String STACK_SIZE = "stack-size";

    String HANDLER = "handler";
    String VIRTUAL_HOST = "virtual-host";
    String HOST = "host";
    String CHAIN = "chain";
    String CHAINS = "chains";
    String CHAIN_REF = "chain-ref";
    String PROPERTIES = "properties";
    String CLASS = "class";
    String PROPERTY = "property";
}
