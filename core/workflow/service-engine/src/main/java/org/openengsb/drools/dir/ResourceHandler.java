package org.openengsb.drools.dir;

import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.RuleBaseSource;

public abstract class ResourceHandler<T extends RuleBaseSource> {

    protected T source;

    public ResourceHandler(T source) {
        this.source = source;
    }

    public abstract void create(String name, String code) throws RuleBaseException;

    public abstract String get(String name) throws RuleBaseException;

    public void update(String name, String code) throws RuleBaseException {
        delete(name);
        create(name, code);
    }

    public abstract void delete(String name) throws RuleBaseException;
}
