package org.openengsb.core.security.model;

import java.lang.reflect.Method;

import javax.persistence.Entity;


@Entity
public class AllPermission extends AbstractPermission {

    @Override
    public boolean permits(Object service, Method operation, Object[] args) {
        return true;
    }
}
