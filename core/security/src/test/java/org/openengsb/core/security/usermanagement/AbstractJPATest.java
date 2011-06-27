package org.openengsb.core.security.usermanagement;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.test.AbstractOpenEngSBTest;

public abstract class AbstractJPATest extends AbstractOpenEngSBTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    protected EntityManager entityManager;

    @Before
    public void setupPersistence() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.ConnectionURL", "jdbc:h2:" + tmpFolder.getRoot().getAbsolutePath() + "/TEST");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(getPersistenceUnitName(), props);
        this.entityManager = emf.createEntityManager();
    }

    public abstract String getPersistenceUnitName();

    @SuppressWarnings("unchecked")
    protected <T> T createWrapInTransactionProxy(T original, Class<T> interfaze) {
        InvocationHandler invocationHandler = new WrapInTransactionHandler(original);
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{ interfaze },
            invocationHandler);
    }

    private final class WrapInTransactionHandler implements InvocationHandler {
        private final Object original;

        private WrapInTransactionHandler(Object original) {
            this.original = original;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            entityManager.getTransaction().begin();
            Object result;
            try {
                result = method.invoke(original, args);
            } catch (InvocationTargetException e) {
                entityManager.getTransaction().rollback();
                throw e.getCause();
            }
            entityManager.getTransaction().commit();
            return result;
        }
    }

    protected void persist(Object o) {
        entityManager.getTransaction().begin();
        entityManager.persist(o);
        entityManager.getTransaction().commit();
    }
}
