package org.openengsb.core.security.model;

import java.lang.reflect.Method;

import javax.persistence.Entity;


@Entity
public class AllPermission extends AbstractPermission {

    @Override
    protected boolean internalPermits(Object service, Method operation, Object[] args) {
        return true;
    }
}
