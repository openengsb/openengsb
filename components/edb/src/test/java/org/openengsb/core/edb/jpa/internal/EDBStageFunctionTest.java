/*
 * Copyright 2013 vauve_000.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.core.edb.jpa.internal;

import java.util.Arrays;
import java.util.Date;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import org.openengsb.core.edb.api.EDBStage;
import static org.hamcrest.Matchers.notNullValue;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;

public class EDBStageFunctionTest extends AbstractEDBFunctionTest
{
	private JPAStage getStage() {
		JPAStage stage = new JPAStage();
		stage.setStageId("stage1");
		stage.setCreator("sveti");
		return stage;
	}
	
	@Test
    public void testCommit_shouldWork() throws Exception {
		super.testCommit_shouldWork(getStage());
    }
	
	@Test
    public void testGetCommits_shouldWork() throws Exception {
		super.testGetCommits_shouldWork(getStage());
	}
	
	@Test(expected = EDBException.class)
    public void testGetInexistantObject_shouldThrowException() throws Exception {
        db.getObject("/this/object/does/not/exist", "this/stage/neither");
    }
	
	@SuppressWarnings("unchecked")
    @Test
    public void testGetHistoryAndCheckForElements_shouldWork() throws Exception {
		super.testGetHistoryAndCheckForElements_shouldWork(getStage());
	}
	
	 @Test
    public void testHistoryOfDeletion_shouldWork() throws Exception {
		super.testHistoryOfDeletion_shouldWork(getStage());
	 }
	 
	 @Test
    public void testGetLog_shouldWork() throws Exception {
		super.testGetLog_shouldWork(getStage());
	 }
	 
	 @Test
    public void testDiff_shouldWork() throws Exception {
		super.testDiff_shouldWork(getStage());
	 }
	 
	 @Test
    public void testGetResurrectedOIDs_shouldWork() throws Exception {
		super.testGetResurrectedOIDs_shouldWork(getStage());
	 }
	 
	 @Test(expected = EDBException.class)
    public void testCommitTwiceSameCommit_shouldThrowError() throws Exception {
		super.testCommitTwiceSameCommit_shouldThrowError(getStage());
	 }
	 
	 @Test
    public void testCommitEDBObjectsInsert_shouldWork() throws Exception {
		super.testCommitEDBObjectsInsert_shouldWork(getStage());
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsInsertDouble_shouldThrowException() throws Exception {
		super.testCommitEDBObjectsInsertDouble_shouldThrowException(getStage());
    }
	
	@Test(expected = EDBException.class)
    public void testIfConflictDetectionIsWorking_shouldThrowException() throws Exception {
		super.testIfConflictDetectionIsWorking_shouldThrowException(getStage());
    }
	
	@Test
    public void testCommitEDBObjectsUpdate_shouldWork() throws Exception {
		super.testCommitEDBObjectsUpdate_shouldWork(getStage());
    }
	
	@Test(expected = EDBException.class)
    public void testCommitEDBObjectsUpdateVerstionConflict_shouldThrowException() throws Exception {
		super.testCommitEDBObjectsUpdateVerstionConflict_shouldThrowException(getStage());
    }
	
	@Test(expected = EDBException.class)
    public void testCommitEDBObjectsUpdateVerstionConflict2_shouldThrowException() throws Exception {
		super.testCommitEDBObjectsUpdateVerstionConflict2_shouldThrowException(getStage());
    }
	
	@Test
    public void testCommitEDBObjectsDelete_shouldWork() throws Exception {
		super.testCommitEDBObjectsDelete_shouldWork(getStage());
    }
	
	@Test(expected = EDBException.class)
    public void testCommitEDBObjectsDeleteNonExisting_shouldThrowException() throws Exception {
		super.testCommitEDBObjectsDeleteNonExisting_shouldThrowException(getStage());
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsDeleteAlreadyDeleted_shouldThrowException() throws Exception {
		super.testCommitEDBObjectsDeleteAlreadyDeleted_shouldThrowException(getStage());
    }
	
	@Test
    public void testIfOtherTypesThanStringWorks_shouldProcessInteger() throws Exception {
		super.testIfOtherTypesThanStringWorks_shouldProcessInteger(getStage());
    }

    @Test
    public void testIfOtherTypesThanStringWorks_shouldProcessBoolean() throws Exception {
		super.testIfOtherTypesThanStringWorks_shouldProcessBoolean(getStage());
    }
	
	@Test
    public void testIfOtherTypesThanStringWorks_shouldProcessDate() throws Exception {
		super.testIfOtherTypesThanStringWorks_shouldProcessDate(getStage());
    }
    
    @Test
    public void testIfCreatedCommitContainsRevisionNumber_shouldReturnNotNull() throws Exception {
        EDBCommit ci = getEDBCommit(getStage());
        assertThat(ci.getRevisionNumber(), notNullValue());
    }
	
	@Test(expected = EDBException.class)
    public void testIfWrongParentCausesCommitError_shouldThrowException() throws Exception {
		super.testIfWrongParentCausesCommitError_shouldThrowException(getStage());
    }
}
