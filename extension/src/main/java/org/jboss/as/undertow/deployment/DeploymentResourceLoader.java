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

package org.jboss.as.undertow.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Set;

import io.undertow.servlet.api.ResourceLoader;
import org.jboss.vfs.VirtualFile;
import org.xnio.Xnio;

/**
 * @author Stuart Douglas
 */
public class DeploymentResourceLoader implements ResourceLoader {

    private final VirtualFile deploymentRoot;

    public DeploymentResourceLoader(final VirtualFile deploymentRoot) {
        this.deploymentRoot = deploymentRoot;
    }

    @Override
    public URL getResource(final String resource) throws MalformedURLException {
        VirtualFile child = deploymentRoot.getChild(resource);
        if (!child.exists()) {
            return null;
        } else {
            return child.toURL();
        }
    }

    @Override
    public InputStream getResourceAsStream(final String resource) {
        VirtualFile child = deploymentRoot.getChild(resource);
        if (!child.exists()) {
            return null;
        } else {
            try {
                return child.openStream();
            } catch (IOException e) {
                return null;
            }
        }
    }

    @Override
    public FileChannel getResourceAsChannel(final String resource, final Xnio xnio) throws IOException {
        return null;
    }

    @Override
    public Set<String> getResourcePaths(final String path) {
        return null;
    }
}
