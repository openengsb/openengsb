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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public abstract class PersistenceServiceTest {

    private PersistenceService persistence;
    private PersistenceTestBean beanA;
    private PersistenceTestBean beanB;
    private PersistenceTestBean beanC;

    protected abstract PersistenceService createPersitenceService() throws Exception;

    @Before
    public void init() throws Exception {
        this.persistence = createPersitenceService();

        beanA = new PersistenceTestBean("A", 1, null);
        beanB = new PersistenceTestBean("B", 1, null);
        beanC = new PersistenceTestBean("C", 3, beanB);
        beanB.setReference(beanC);

        persistence.create(Arrays.asList(new PersistenceTestBean[]{beanA, beanB, beanC}));
    }

    @Test
    public void testExactMatchQuery_shouldReturnSingleResult() {
        PersistenceTestBean example = new PersistenceTestBean("A", 1, null);
        List<PersistenceTestBean> results = persistence.query(example);
        assertThat(results.size(), is(1));
        PersistenceTestBean result = results.get(0);
        assertThat(result, is(beanA));
    }

    @Test
    public void testMatchQuery_shouldReturnSingleResult() {
        PersistenceTestBean example = new PersistenceTestBean("A", null, null);
        List<PersistenceTestBean> results = persistence.query(example);
        assertThat(results.size(), is(1));
        PersistenceTestBean result = results.get(0);
        assertThat(result, is(beanA));
    }

    @Test
    public void testMatchQuery_shouldReturnTwoResults() {
        PersistenceTestBean example = new PersistenceTestBean(null, 1, null);
        List<PersistenceTestBean> results = persistence.query(example);
        assertThat(results.size(), is(2));
        assertThat(results.contains(beanA), is(true));
        assertThat(results.contains(beanB), is(true));
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
