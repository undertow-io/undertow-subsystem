/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import static org.jboss.logging.Logger.Level.INFO;

import java.net.InetSocketAddress;

import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.Messages;

/**
 * This module is using message IDs in the range 17300 - 17699.
 * <p/>
 * This file is using the subset 11530-11599 for non-logger messages.
 * <p/>
 * See <a href="http://community.jboss.org/docs/DOC-16810">http://community.jboss.org/docs/DOC-16810</a> for the full
 * list of currently reserved JBAS message id blocks.
 * <p/>

 *

 */
@MessageBundle(projectCode = "JBAS")
public interface UndertowMessages {

    /**
     * The default messages.
     */
    UndertowMessages MESSAGES = Messages.getBundle(UndertowMessages.class);

    /**
     * Creates an exception indicating the class, represented by the {@code className} parameter, cannot be accessed.
     *
     * @param name    name of the listener
     * @param address socket address
     * @return the message.
     */
    @LogMessage(level = INFO)
    @Message(id = 17300, value = "Undertow listener %s listening on %s")
    String listenerStarted(String name, InetSocketAddress address);


}
