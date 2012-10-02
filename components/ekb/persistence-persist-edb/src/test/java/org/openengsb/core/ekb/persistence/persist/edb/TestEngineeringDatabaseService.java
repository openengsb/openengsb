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

package org.openengsb.core.ekb.persistence.persist.edb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBDiff;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.common.EDBConverterUtils;
import org.openengsb.core.ekb.persistence.persist.edb.models.EngineeringObjectModel;
import org.openengsb.core.ekb.persistence.persist.edb.models.SourceModelA;
import org.openengsb.core.ekb.persistence.persist.edb.models.SourceModelB;

/**
 * The TestEngineeringDatabaseService is a simple implementation of the EngineeringDatabaseService for testing the
 * Engineering Object support.
 */
public class TestEngineeringDatabaseService implements EngineeringDatabaseService {

    @Override
    public EDBObject getObject(String arg0) throws EDBException {
        if (arg0.equals("test/test/objectA/reference/1")) {
            EDBObject edbObject = new EDBObject("test/test/objectA/reference/1");
            edbObject.putEDBObjectEntry("nameA", "firstObject");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, SourceModelA.class.getName());
            return edbObject;
        }
        if (arg0.equals("test/test/objectA/reference/2")) {
            EDBObject edbObject = new EDBObject("test/test/objectA/reference/2");
            edbObject.putEDBObjectEntry("nameA", "updatedFirstObject");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, SourceModelA.class.getName());
            return edbObject;
        }
        if (arg0.equals("test/test/objectB/reference/1")) {
            EDBObject edbObject = new EDBObject("test/test/objectB/reference/1");
            edbObject.putEDBObjectEntry("nameB", "secondObject");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, SourceModelB.class.getName());
            return edbObject;
        }
        if (arg0.equals("test/test/objectB/reference/2")) {
            EDBObject edbObject = new EDBObject("test/test/objectB/reference/2");
            edbObject.putEDBObjectEntry("nameB", "updatedSecondObject");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, SourceModelB.class.getName());
            return edbObject;
        }
        if (arg0.equals("test/test/common/reference/1")) {
            EDBObject edbObject = new EDBObject("test/test/common/reference/1");
            edbObject.putEDBObjectEntry("modelAId", "test/test/objectA/reference/1");
            edbObject.putEDBObjectEntry("modelBId", "test/test/objectB/reference/1");
            edbObject.putEDBObjectEntry("nameA", "firstObject");
            edbObject.putEDBObjectEntry("nameB", "secondObject");
            edbObject.putEDBObjectEntry("internalModelName", "common/reference/1");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, EngineeringObjectModel.class.getName());
            return edbObject;
        }

        return null;
    }

    @Override
    public List<EDBObject> query(Map<String, Object> arg0, Long arg1) throws EDBException {
        String reference = (String) arg0.get(EDBConverterUtils.REFERENCE_PREFIX + "%");
        if (reference.equals("test/test/objectA/reference/1") || reference.equals("test/test/objectB/reference/1")) {
            EDBObject edbObject = new EDBObject("test/test/common/reference/1");
            edbObject.putEDBObjectEntry("modelAId", "test/test/objectA/reference/1");
            edbObject.putEDBObjectEntry("modelBId", "test/test/objectB/reference/1");
            edbObject.putEDBObjectEntry("nameA", "firstObject");
            edbObject.putEDBObjectEntry("nameB", "secondObject");
            edbObject.putEDBObjectEntry("internalModelName", "common/reference/1");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, EngineeringObjectModel.class.getName());
            return Arrays.asList(edbObject);
        }
        if (reference.equals("test/test/common/reference/1")) {
            return new ArrayList<EDBObject>();
        }
        return new ArrayList<EDBObject>();
    }

    @Override
    public Long commit(EDBCommit arg0) throws EDBException {
        return null;
    }

    @Override
    public void commitEDBObjects(List<EDBObject> arg0, List<EDBObject> arg1, List<EDBObject> arg2) throws EDBException {
    }

    @Override
    public EDBCommit createCommit(String arg0, String arg1) {
        return null;
    }

    @Override
    public EDBCommit getCommit(Long arg0) throws EDBException {
        return null;
    }

    @Override
    public List<EDBCommit> getCommits(Map<String, Object> arg0) throws EDBException {
        return null;
    }

    @Override
    public List<EDBCommit> getCommitsByKeyValue(String arg0, Object arg1) throws EDBException {
        return null;
    }

    @Override
    public EDBDiff getDiff(Long arg0, Long arg1) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHead() throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHead(long arg0) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHistory(String arg0) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHistoryForTimeRange(String arg0, Long arg1, Long arg2) throws EDBException {
        return null;
    }

    @Override
    public EDBCommit getLastCommit(Map<String, Object> arg0) throws EDBException {
        return null;
    }

    @Override
    public EDBCommit getLastCommitByKeyValue(String arg0, Object arg1) throws EDBException {
        return null;
    }

    @Override
    public List<EDBLogEntry> getLog(String arg0, Long arg1, Long arg2) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getObjects(List<String> arg0) throws EDBException {
        return null;
    }

    @Override
    public List<String> getResurrectedOIDs() throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(Map<String, Object> arg0) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatchingByKeyValue(String arg0, Object arg1) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> queryByKeyValue(String arg0, Object arg1) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> queryByMap(Map<String, Object> arg0) throws EDBException {
        return null;
    }

}
