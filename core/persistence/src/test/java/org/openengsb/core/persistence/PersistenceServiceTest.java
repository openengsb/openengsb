/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.persistence;

import org.junit.Before;
import org.junit.Test;

public abstract class PersistenceServiceTest {

    private PersistenceService persistence;

    protected abstract PersistenceService createPersitenceService();

    @Before
    public void init() {
        this.persistence = createPersitenceService();
    }

    @Test
    public void testExactMatchQuery_shouldReturnSingleResult() {
    }

    @Test
    public void testMatchQuery_shouldReturnSingleResult() {
    }

    @Test
    public void testMatchQuery_shouldReturnTwoResults() {
    }

    @Test
    public void testMultipleExampleQuery_shouldBehaveLikeMultipleSimpleExampleQueries() {
    }

    @Test
    public void testCreate_shouldStoreElement() {

    }

    @Test
    public void testCreateSameElementTwice_shouldStoreElementTwice() {

    }

    @Test
    public void testMultipleCreate_shouldBehaveLikeMultipleSimpleCreates() {

    }

    @Test
    public void testUpdate_shouldUpdateElement() {

    }

    @Test
    public void testUpdateSourceElementNotPresent_shouldFail() {

    }

    @Test
    public void testUpdateSourceElementNotUnique_shouldFail() {

    }

    @Test
    public void testMultipleUpdate_shouldBehaveLikeMultipleSimpleUpdates() {

    }

    @Test
    public void testMultipleUpdateFailure_noUpdateShouldHaveBeenDone() {

    }

    @Test
    public void testDelete_shouldDeleteElement() {

    }

    @Test
    public void testDeleteElementNotPresent_shouldFail() {

    }

    @Test
    public void testDeleteMultipleHits_shouldWork() {

    }

    @Test
    public void testMultipleDelete_shouldBehaveLikeMultipleSimpleDeletes() {

    }

}
