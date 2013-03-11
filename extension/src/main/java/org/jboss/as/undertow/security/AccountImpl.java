package org.jboss.as.undertow.security;

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;

import io.undertow.security.idm.Account;

/**
 *
 * @author Stuart Douglas
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class AccountImpl implements Account, Serializable {

    private final String name;
    private Set<String> roles;
    private final Principal principal;

    public AccountImpl(final String name) {
        this.name = name;
        this.principal = new AccountPrincipal();
    }

    public AccountImpl(final Principal principal) {
        this.principal = principal;
        this.name = principal.getName();
    }

    public Set<String> getRoles() {
        return roles;
    }

    void setRoles(final Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final AccountImpl account = (AccountImpl) o;

        if (name != null ? !name.equals(account.name) : account.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInGroup(String group) {
        return roles.contains(group);
    }

    private final class AccountPrincipal implements Principal {

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AccountPrincipal ? equals((AccountPrincipal) obj) : false;
        }

        private boolean equals(AccountPrincipal other) {
            return name.equals(other.getName());
        }

    }
}
