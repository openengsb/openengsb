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
