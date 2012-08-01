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

import java.util.List;
import java.util.Map;

import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.jpa.internal.JPACommit;
import org.openengsb.core.edb.jpa.internal.JPAHead;
import org.openengsb.core.edb.jpa.internal.JPAObject;

/**
 * Represents a dao interface for connection to the JPA Database
 */
public interface JPADao {

    /**
     * Loads the JPAHead with the given timestamp.
     */
    JPAHead getJPAHead(long timestamp) throws EDBException;

    /**
     * Returns the history (all objects) of a given object.
     */
    List<JPAObject> getJPAObjectHistory(String oid) throws EDBException;

    /**
     * Returns the history (between from and to) of a given object.
     */
    List<JPAObject> getJPAObjectHistory(String oid, long from, long to) throws EDBException;

    /**
     * Returns a JPAObject with the given timestamp
     */
    JPAObject getJPAObject(String oid, long timestamp) throws EDBException;
    
    /**
     * Returns the newest JPAObject with the given oid
     */
    JPAObject getJPAObject(String oid) throws EDBException;
    
    /**
     * Returns the newest JPAObjects with the given oids
     */
    List<JPAObject> getJPAObjects(List<String> oids) throws EDBException;

    /**
     * Returns all commits which are involved with the given oid which are between from and to
     */
    List<JPACommit> getJPACommit(String oid, long from, long to) throws EDBException;
    
    /**
     * Returns a list of oids from the JPAObjects which has been resurrected
     */
    List<String> getResurrectedOIDs() throws EDBException;

    /**
     * Loads a JPACommit with the given timestamp
     */
    List<JPACommit> getJPACommit(long timestamp) throws EDBException;

    /**
     * Get all commits which are given with the param map. In the map there are values like commiter, role, etc.
     */
    List<JPACommit> getCommits(Map<String, Object> param) throws EDBException;

    /**
     * like getCommits, but it returns only the newest commit
     */
    JPACommit getLastCommit(Map<String, Object> param) throws EDBException;
    
    /**
     * Returns a list of JPAObjects which have all JPAEntries with the given keys and values.
     */
    List<JPAObject> query(Map<String, Object> values) throws EDBException;
    
    /**
     * Returns a list of JPAObjects which have all JPAEntries with the given keys and values at a specific timestamp
     * (similar to getHead)
     */
    List<JPAObject> query(Map<String, Object> values, Long timestamp) throws EDBException;
    
    /**
     * Returns the version of the element under the given oid. If oid isn't existing, 0 is returned.
     */
    Integer getVersionOfOid(String oid) throws EDBException;
}
