/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.edb.jpa.internal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.hooks.EDBBeginCommitHook;
import org.openengsb.core.edb.api.hooks.EDBErrorHook;
import org.openengsb.core.edb.api.hooks.EDBPostCommitHook;
import org.openengsb.core.edb.api.hooks.EDBPreCommitHook;
import org.openengsb.core.edb.jpa.internal.dao.JPADao;

/**
 * A test implementation of the EDB service, which add a real implementation for the transaction management, since
 * this is not covered by the container in the unit tests.
 */
public class TestEDBService extends JPADatabase {
    private EntityTransaction utx;

    public TestEDBService(JPADao dao, AuthenticationContext authenticationContext,
            List<EDBBeginCommitHook> beginCommitHooks, List<EDBPreCommitHook> preCommitHooks,
            List<EDBPostCommitHook> postCommitHooks, List<EDBErrorHook> errorHooks, Boolean revisionCheckEnabled,
            EntityManager entityManager) {
        super(dao, authenticationContext, beginCommitHooks, preCommitHooks, postCommitHooks, errorHooks,
            revisionCheckEnabled);
        this.setEntityManager(entityManager);
    }
    
    /**
     * Starts the EDBService for testing purpose
     */
    public void open() throws EDBException {
        LOGGER.debug("starting to open EDB for testing via JPA");
        utx = entityManager.getTransaction();
        LOGGER.debug("starting of EDB successful");
    }

    /**
     * Closes the EDBService for testing purpose
     */
    public void close() {
        entityManager.close();
        utx = null;
        entityManager = null;
    }

    @Override
    protected void beginTransaction() {
        utx.begin();
    }

    @Override
    protected void commitTransaction() {
        utx.commit();
    }

    @Override
    protected void rollbackTransaction() {
        utx.rollback();
    }
}
