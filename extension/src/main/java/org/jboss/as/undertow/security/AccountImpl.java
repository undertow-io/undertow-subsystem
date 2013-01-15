package org.jboss.as.undertow.security;

import java.security.Principal;
import java.util.Set;

import io.undertow.security.idm.Account;


/**
 * @author Stuart Douglas
 */
public class AccountImpl implements Account, Principal {

    private final String name;
    private Set<String> roles;

    public AccountImpl(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public Set<String> getRoles() {
        return roles;
    }

    void setRoles(final Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AccountImpl account = (AccountImpl) o;

        if (name != null ? !name.equals(account.name) : account.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
