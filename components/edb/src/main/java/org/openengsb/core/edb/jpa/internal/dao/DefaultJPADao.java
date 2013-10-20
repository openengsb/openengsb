/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openengsb.core.edb.jpa.internal.dao;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.openengsb.core.api.model.CommitMetaInfo;
import org.openengsb.core.api.model.CommitQueryRequest;
import org.openengsb.core.api.model.QueryRequest;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.jpa.internal.JPACommit;
import org.openengsb.core.edb.jpa.internal.JPAHead;
import org.openengsb.core.edb.jpa.internal.JPAObject;


public class DefaultJPADao extends AbstractJPADao implements JPADao {

    public DefaultJPADao() {
    }

    public DefaultJPADao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public JPAHead getJPAHead(long timestamp) throws EDBException {
        return super.getJPAHead(timestamp, null);
    }

    @Override
    public JPAHead getJPAHead(long timestamp, String sid) throws EDBException {
        return super.getJPAHead(timestamp, sid);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<JPAObject> getJPAObjectHistory(String oid) throws EDBException {
        return super.getJPAObjectHistory(oid, null);
    }

    @Override
    public List<JPAObject> getJPAObjectHistory(String oid, String sid) throws EDBException {
        return super.getJPAObjectHistory(oid, sid);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<JPAObject> getJPAObjectHistory(String oid, long from, long to) throws EDBException {
        return super.getJPAObjectHistory(oid, from, to, null);
    }

    @Override
    public List<JPAObject> getJPAObjectHistory(String oid, String sid, long from, long to) throws EDBException {
        return super.getJPAObjectHistory(oid, from, to, sid);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public JPAObject getJPAObject(String oid, long timestamp) throws EDBException {
        return this.getJPAObject(oid, null, timestamp);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public JPAObject getJPAObject(String oid, String sid, long timestamp) throws EDBException {
        return super.getJPAObject(oid, timestamp, sid);
    }

    @Override
    public JPAObject getJPAObject(String oid) throws EDBException {
        LOGGER.debug("Loading newest object {}", oid);
        return this.getJPAObject(oid, System.currentTimeMillis());
    }

    @Override
    public JPAObject getJPAObject(String oid, String sid) throws EDBException {
        LOGGER.debug("Loading newest object {} from stage {}", oid, sid);
        return this.getJPAObject(oid, sid, System.currentTimeMillis());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<JPAObject> getJPAObjects(List<String> oids) throws EDBException {
        return super.getJPAObjects(oids, null);
    }

    @Override
    public List<JPAObject> getJPAObjects(List<String> oids, String sid) throws EDBException {
        return super.getJPAObjects(oids, sid);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<JPACommit> getJPACommit(String oid, long from, long to) throws EDBException {
        return super.getJPACommit(oid, from, to, null);
    }

    @Override
    public List<JPACommit> getJPACommit(String oid, String sid, long from, long to) throws EDBException {
        return super.getJPACommit(oid, from, to, sid);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<String> getResurrectedOIDs() throws EDBException {
        return super.getResurrectedOIDs(null);
    }

    @Override
    public List<String> getResurrectedOIDs(String sid) throws EDBException {
        return super.getResurrectedOIDs(sid);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<JPACommit> getJPACommit(long timestamp) throws EDBException {
        //return (List<JPACommit>)(List<?>)super.getJPACommit(JPABaseCommit.class, timestamp, null);
        return super.getJPACommit(timestamp, null);
    }

    @Override
    public List<JPACommit> getJPACommit(long timestamp, String sid) throws EDBException {
        return super.getJPACommit(timestamp, sid);
    }

    @Override
    public JPACommit getJPACommit(String revision) throws EDBException {
        return super.getJPACommit(revision, null);
    }

    @Override
    public JPACommit getJPACommit(String revision, String sid) throws EDBException {
        return super.getJPACommit(revision, sid);
    }

    @Override
    public List<JPACommit> getCommits(Map<String, Object> param) throws EDBException {
        return super.getCommits(param, null);
    }

    @Override
    public List<JPACommit> getCommits(Map<String, Object> param, String sid) throws EDBException {
        return super.getCommits(param, sid);
    }

    @Override
    public JPACommit getLastCommit(Map<String, Object> param) throws EDBException {
        return super.getLastCommit(param, null);
    }

    @Override
    public JPACommit getLastCommit(Map<String, Object> param, String sid) throws EDBException {
        return super.getLastCommit(param, sid);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<CommitMetaInfo> getRevisionsOfMatchingCommits(CommitQueryRequest request) throws EDBException {
        return super.getRevisionsOfMatchingCommits(request, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<CommitMetaInfo> getRevisionsOfMatchingCommits(CommitQueryRequest request, String sid) 
        throws EDBException {
        return super.getRevisionsOfMatchingCommits(request, sid);
    }

    @Override
    public Integer getVersionOfOid(String oid) throws EDBException {
        return super.getVersionOfOid(oid, null);
    }

    @Override
    public Integer getVersionOfOid(String oid, String sid) throws EDBException {
        return super.getVersionOfOid(oid, sid);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<JPAObject> query(QueryRequest request) throws EDBException {
        return super.query(request, null);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<JPAObject> query(QueryRequest request, String sid) throws EDBException {
        return super.query(request, sid);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
