package org.openengsb.config.model;

import java.io.Serializable;

import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.openengsb.config.dao.BaseDao;
import org.openengsb.config.domain.AbstractDomainObject;

public class Models {
    public static <T> CompoundPropertyModel<T> compound(T t) {
        return new CompoundPropertyModel<T>(t);
    }

    public static <T, M extends IModel<T>> CompoundPropertyModel<T> compound(M m) {
        return new CompoundPropertyModel<T>(m);
    }

    public static <T extends AbstractDomainObject> DomainModel<T> domain(BaseDao<T> dao, T t) {
        return new DomainModel<T>(dao, t);
    }

    public static <T extends AbstractDomainObject> CompoundPropertyModel<T> compoundDomain(
            BaseDao<T> dao, T t) {
        return compound(domain(dao, t));
    }

    public static <T extends Serializable> Model<T> model(T t) {
        return new Model<T>(t);
    }
}
