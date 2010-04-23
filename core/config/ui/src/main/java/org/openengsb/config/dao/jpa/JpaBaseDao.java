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
package org.openengsb.config.dao.jpa;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.openengsb.config.dao.BaseDao;
import org.openengsb.config.domain.AbstractDomainObject;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class JpaBaseDao<T extends AbstractDomainObject> implements BaseDao<T> {

    @PersistenceContext
    protected EntityManager em;

    private final Class<T> persistenceClass;

    @SuppressWarnings("unchecked")
    public JpaBaseDao() {
        persistenceClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public void delete(T toDelete) {
        em.remove(em.merge(toDelete));
    }

    @Override
    public T find(Long id) {
        return em.find(persistenceClass, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        return em.createNamedQuery(persistenceClass.getSimpleName() + ".findAll").getResultList();
    }

    @Override
    public void persist(T toPersist) {
        if (toPersist.getId() == null) {
            em.persist(toPersist);
        } else {
            update(toPersist);
        }
    }

    @Override
    public void refresh(T toRefresh) {
        em.refresh(toRefresh);
    }

    @Override
    public void update(T toUpdate) {
        em.persist(em.merge(toUpdate));
    }
}
