package org.openengsb.config.model;

import org.apache.wicket.model.LoadableDetachableModel;
import org.openengsb.config.dao.BaseDao;
import org.openengsb.config.domain.AbstractDomainObject;

public class DomainModel<T extends AbstractDomainObject> extends LoadableDetachableModel<T> {
    private static final long serialVersionUID = 1L;

    private final BaseDao<T> dao;
    private final Long id;

    public DomainModel(BaseDao<T> dao, T t) {
        super(t);
        this.dao = dao;
        this.id = t.getId();
    }

    @Override
    protected T load() {
        return dao.find(id);
    }
}
