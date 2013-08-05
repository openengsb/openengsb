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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;

import com.google.common.collect.Maps;

public class EDBFunctionTest extends AbstractEDBFunctionTest {    

    @Test
    public void testOpenDatabase_shouldWork() throws Exception {
        assertThat(db, notNullValue());
    }

    @Test
    public void testCommit_shouldWork() throws Exception {
		super.testCommit_shouldWork(null);
    }

    @Test
    public void testGetCommits_shouldWork() throws Exception {
		super.testGetCommits_shouldWork(null);
    }

    @Test(expected = EDBException.class)
    public void testGetInexistantObject_shouldThrowException() throws Exception {
        db.getObject("/this/object/does/not/exist");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetHistoryAndCheckForElements_shouldWork() throws Exception {
		super.testGetHistoryAndCheckForElements_shouldWork(null);
    }

    @Test
    public void testHistoryOfDeletion_shouldWork() throws Exception {
		super.testHistoryOfDeletion_shouldWork(null);
    }

    @Test
    public void testGetLog_shouldWork() throws Exception {
		super.testGetLog_shouldWork(null);
    }

    @Test
    public void testDiff_shouldWork() throws Exception {
		super.testDiff_shouldWork(null);
    }

    @Test
    public void testGetResurrectedOIDs_shouldWork() throws Exception {
		super.testGetResurrectedOIDs_shouldWork(null);
    }

    @Test(expected = EDBException.class)
    public void testCommitTwiceSameCommit_shouldThrowError() throws Exception {
		super.testCommitTwiceSameCommit_shouldThrowError(null);
    }

    @Test
    public void testCommitEDBObjectsInsert_shouldWork() throws Exception {
		super.testCommitEDBObjectsInsert_shouldWork(null);
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsInsertDouble_shouldThrowException() throws Exception {
		super.testCommitEDBObjectsInsertDouble_shouldThrowException(null);
    }

    @Test(expected = EDBException.class)
    public void testIfConflictDetectionIsWorking_shouldThrowException() throws Exception {
		super.testIfConflictDetectionIsWorking_shouldThrowException(null);
    }

    @Test
    public void testCommitEDBObjectsUpdate_shouldWork() throws Exception {
		super.testCommitEDBObjectsUpdate_shouldWork(null);
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsUpdateVerstionConflict_shouldThrowException() throws Exception {
		super.testCommitEDBObjectsUpdateVerstionConflict_shouldThrowException(null);
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsUpdateVerstionConflict2_shouldThrowException() throws Exception {
		super.testCommitEDBObjectsUpdateVerstionConflict2_shouldThrowException(null);
    }

    @Test
    public void testCommitEDBObjectsDelete_shouldWork() throws Exception {
		super.testCommitEDBObjectsDelete_shouldWork(null);
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsDeleteNonExisting_shouldThrowException() throws Exception {
		super.testCommitEDBObjectsDeleteNonExisting_shouldThrowException(null);
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsDeleteAlreadyDeleted_shouldThrowException() throws Exception {
		super.testCommitEDBObjectsDeleteAlreadyDeleted_shouldThrowException(null);
    }

    @Test
    public void testIfOtherTypesThanStringWorks_shouldProcessInteger() throws Exception {
		super.testIfOtherTypesThanStringWorks_shouldProcessInteger(null);
    }

    @Test
    public void testIfOtherTypesThanStringWorks_shouldProcessBoolean() throws Exception {
		super.testIfOtherTypesThanStringWorks_shouldProcessBoolean(null);
    }

    @Test
    public void testIfOtherTypesThanStringWorks_shouldProcessDate() throws Exception {
		super.testIfOtherTypesThanStringWorks_shouldProcessDate(null);
    }
    
    @Test
    public void testIfCreatedCommitContainsRevisionNumber_shouldReturnNotNull() throws Exception {
        EDBCommit ci = getEDBCommit();
        assertThat(ci.getRevisionNumber(), notNullValue());
    }
    
    @Test(expected = EDBException.class)
    public void testIfWrongParentCausesCommitError_shouldThrowException() throws Exception {
		super.testIfWrongParentCausesCommitError_shouldThrowException(null);
    }
}
