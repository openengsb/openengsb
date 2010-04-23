/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
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
