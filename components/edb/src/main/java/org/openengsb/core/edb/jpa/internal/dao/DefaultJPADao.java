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

package org.openengsb.core.edb.jpa.internal.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.jpa.internal.JPACommit;
import org.openengsb.core.edb.jpa.internal.JPAHead;
import org.openengsb.core.edb.jpa.internal.JPAObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultJPADao extends AbstractJPADao implements JPADao {
    

    public DefaultJPADao() {
    }

    public DefaultJPADao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public JPAHead getJPAHead(long timestamp) throws EDBException {
        return super.getJPAHead(null, timestamp);
    }
	
	@Override
	public JPAHead getJPAHead(long timestamp, String sid) throws EDBException
	{
		return super.getJPAHead(sid, timestamp);
	}

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPAObject> getJPAObjectHistory(String oid) throws EDBException {
        return super.getJPAObjectHistory(oid, null);
    }
	
	@Override
	public List<JPAObject> getJPAObjectHistory(String oid, String sid) throws EDBException
	{
		return super.getJPAObjectHistory(oid, sid);
	}

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPAObject> getJPAObjectHistory(String oid, long from, long to) throws EDBException {
        return super.getJPAObjectHistory(oid, null, from, to);
    }
	
	@Override
	public List<JPAObject> getJPAObjectHistory(String oid, String sid, long from, long to) throws EDBException
	{
		return super.getJPAObjectHistory(oid, sid, from, to);
	}
	
	

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public JPAObject getJPAObject(String oid, long timestamp) throws EDBException {
        return this.getJPAObject(oid, null, timestamp);
    }
	
	@Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public JPAObject getJPAObject(String oid, String sid, long timestamp) throws EDBException {
        return super.getJPAObject(oid, sid, timestamp);
    }

    @Override
    public JPAObject getJPAObject(String oid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading newest object {}", oid);
            return this.getJPAObject(oid, System.currentTimeMillis());
        }
    }
	
	@Override
    public JPAObject getJPAObject(String oid, String sid) throws EDBException {
        synchronized (entityManager) {
            LOGGER.debug("Loading newest object {} from stage {}", oid, sid);
            return this.getJPAObject(oid, sid, System.currentTimeMillis());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<JPAObject> getJPAObjects(List<String> oids) throws EDBException {
        return super.getJPAObjects(oids, null);
    }
	
	@Override
	public List<JPAObject> getJPAObjects(List<String> oids, String sid) throws EDBException
	{
		return super.getJPAObjects(oids, sid);
	}

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<JPACommit> getJPACommit(String oid, long from, long to) throws EDBException {
        return super.getJPACommit(oid, null, from, to);
    }
	
	@Override
	public List<JPACommit> getJPACommit(String oid, String sid, long from, long to) throws EDBException
	{
		return super.getJPACommit(oid, sid, from, to);
	}
	
	

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<String> getResurrectedOIDs() throws EDBException {
        return super.getResurrectedOIDs(null);
    }
	
	@Override
	public List<String> getResurrectedOIDs(String sid) throws EDBException
	{
		return super.getResurrectedOIDs(sid);
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<JPACommit> getJPACommit(long timestamp) throws EDBException {
        //return (List<JPACommit>)(List<?>)super.getJPACommit(JPABaseCommit.class, timestamp, null);
		return super.getJPACommit(timestamp, null);
    }
	
	@Override
	public List<JPACommit> getJPACommit(long timestamp, String sid) throws EDBException
	{
		return super.getJPACommit(timestamp, sid);
	}

    @Override
    public List<JPACommit> getCommits(Map<String, Object> param) throws EDBException {
        return super.getCommits(param, null);
    }
	
	@Override
	public List<JPACommit> getCommits(Map<String, Object> param, String sid) throws EDBException
	{
		return super.getCommits(param, sid);
	}

    @Override
    public JPACommit getLastCommit(Map<String, Object> param) throws EDBException {
        return super.getLastCommit(param, null);
    }
	
	@Override
	public JPACommit getLastCommit(Map<String, Object> param, String sid) throws EDBException
	{
		return super.getLastCommit(param, sid);
	}

    @Override
    public List<JPAObject> query(Map<String, Object> values) throws EDBException {
        return super.query(values, null);
    }
	
	@Override
	public List<JPAObject> query(Map<String, Object> values, String sid) throws EDBException
	{
		return super.query(values, sid);
	}

    @Override
    public Integer getVersionOfOid(String oid) throws EDBException {
        return super.getVersionOfOid(oid, null);
    }
	
	@Override
	public Integer getVersionOfOid(String oid, String sid) throws EDBException
	{
		return super.getVersionOfOid(oid, sid);
	}

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<JPAObject> query(Map<String, Object> values, Long timestamp) throws EDBException {
        return super.query(values, timestamp, null);
    }
	
	@Override
	public List<JPAObject> query(Map<String, Object> values, String sid, Long timestamp) throws EDBException
	{
		return super.query(values, timestamp, sid);
	}

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}