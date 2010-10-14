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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        persistence = createPersitenceService();

        beanA = new PersistenceTestBean("A", 1, null);
        beanB = new PersistenceTestBean("B", 1, null);
        beanC = new PersistenceTestBean("C", 3, beanB);
        beanB.setReference(beanC);

        persistence.create(Arrays.asList(new PersistenceTestBean[]{ beanA, beanB, beanC }));
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
        PersistenceTestBean example = new PersistenceTestBean("A", null, null);
        PersistenceTestBean example2 = new PersistenceTestBean("B", null, null);
        List<PersistenceTestBean> results = persistence.query(example);
        results.addAll(persistence.query(example2));
        List<PersistenceTestBean> results2 =
            persistence.query(Arrays.asList(new PersistenceTestBean[]{ example, example2 }));
        assertThat(results2, is(results));
    }

    @Test
    public void testCreate_shouldStoreElement() throws PersistenceException {
        PersistenceTestBean additional = new PersistenceTestBean("Test", 1, null);
        persistence.create(additional);

        List<PersistenceTestBean> results = persistence.query(new PersistenceTestBean("Test", null, null));
        assertThat(results.contains(additional), is(true));
    }

    @Test
    public void testCreateSameElementTwice_shouldStoreElementTwice() throws PersistenceException {
        PersistenceTestBean additional = new PersistenceTestBean("Test", 1, null);
        persistence.create(additional);
        persistence.create(additional);

        List<PersistenceTestBean> results = persistence.query(new PersistenceTestBean("Test", null, null));
        assertThat(results.size(), is(2));
        assertThat(results.get(0), is(additional));
        assertThat(results.get(1), is(additional));
    }

    @Test
    public void testCreateAndChange_shouldNotAffectStoredElement() throws PersistenceException {
        PersistenceTestBean additional = new PersistenceTestBean("Test", 1, null);
        persistence.create(additional);

        additional.setStringValue("Foo");

        List<PersistenceTestBean> results = persistence.query(new PersistenceTestBean("Test", null, null));
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getStringValue(), is("Test"));
    }

    @Test
    public void testMultiCreate_shouldWork() throws PersistenceException {
        PersistenceTestBean additional1 = new PersistenceTestBean("Test", 1, null);
        PersistenceTestBean additional2 = new PersistenceTestBean("Test", 2, null);
        persistence.create(Arrays.asList(new PersistenceTestBean[]{ additional1, additional2 }));

        List<PersistenceTestBean> results = persistence.query(new PersistenceTestBean("Test", null, null));
        assertThat(results.size(), is(2));
        assertThat(results.contains(additional1), is(true));
        assertThat(results.contains(additional2), is(true));
    }

    @Test
    public void testUpdate_shouldUpdateElement() throws PersistenceException {
        PersistenceTestBean newBeanA = new PersistenceTestBean("Foo", 1, null);
        persistence.update(beanA, newBeanA);

        PersistenceTestBean example = new PersistenceTestBean("A", null, null);
        List<PersistenceTestBean> results = persistence.query(example);
        assertThat(results.isEmpty(), is(true));

        PersistenceTestBean example2 = new PersistenceTestBean("Foo", null, null);
        List<PersistenceTestBean> results2 = persistence.query(example2);
        assertThat(results2.size(), is(1));
        PersistenceTestBean result = results2.get(0);
        assertThat(result, is(newBeanA));
    }

    @Test(expected = PersistenceException.class)
    public void testUpdateSourceElementNotPresent_shouldFail() throws PersistenceException {
        PersistenceTestBean newBeanA = new PersistenceTestBean("Foo", 1, null);
        persistence.update(newBeanA, beanB);
    }

    @Test(expected = PersistenceException.class)
    public void testUpdateSourceElementNotUnique_shouldFail() throws PersistenceException {
        PersistenceTestBean additional = new PersistenceTestBean("Test", 1, null);
        persistence.create(additional);
        persistence.create(additional);

        PersistenceTestBean newBean = new PersistenceTestBean("Foo", 1, null);

        persistence.update(additional, newBean);
    }

    @Test
    public void testMultiUpdate_shouldWork() throws PersistenceException {
        PersistenceTestBean updated1 = new PersistenceTestBean("Test", 1, null);
        PersistenceTestBean updated2 = new PersistenceTestBean("Foo", 2, null);
        PersistenceTestBean updated3 = new PersistenceTestBean("Bar", 3, null);

        Map<PersistenceTestBean, PersistenceTestBean> toUpdate =
            new HashMap<PersistenceTestBean, PersistenceTestBean>();

        toUpdate.put(beanA, updated1);
        toUpdate.put(beanB, updated2);
        toUpdate.put(beanC, updated3);

        persistence.update(toUpdate);

        PersistenceTestBean wildcard = new PersistenceTestBean(null, null, null);
        List<PersistenceTestBean> results = persistence.query(wildcard);
        assertThat(results.size(), is(3));
        assertThat(results.contains(updated1), is(true));
        assertThat(results.contains(updated2), is(true));
        assertThat(results.contains(updated3), is(true));
    }

    @Test
    public void testDelete_shouldDeleteElement() throws PersistenceException {
        persistence.delete(beanA);
        List<PersistenceTestBean> results = persistence.query(new PersistenceTestBean("A", 1, null));
        assertThat(results.isEmpty(), is(true));
    }

    @Test(expected = PersistenceException.class)
    public void testDeleteElementNotPresent_shouldFail() throws PersistenceException {
        PersistenceTestBean test = new PersistenceTestBean("Test", 1, null);
        persistence.delete(test);
    }

    @Test
    public void testDeleteMultipleHits_shouldWork() throws PersistenceException {
        PersistenceTestBean example = new PersistenceTestBean(null, 1, null);
        List<PersistenceTestBean> results = persistence.query(example);
        assertThat(results.size(), is(2));

        persistence.delete(example);
        results = persistence.query(example);
        assertThat(results.isEmpty(), is(true));
    }

    @Test
    public void testMultiDelete_shouldWork() throws PersistenceException {
        PersistenceTestBean wildcard = new PersistenceTestBean(null, null, null);
        List<PersistenceTestBean> results = persistence.query(wildcard);
        assertThat(results.size(), is(3));

        PersistenceTestBean aAndB = new PersistenceTestBean(null, 1, null);
        PersistenceTestBean c = new PersistenceTestBean("C", 3, null);
        List<PersistenceTestBean> toDelete = Arrays.asList(new PersistenceTestBean[]{ aAndB, c });
        persistence.delete(toDelete);

        results = persistence.query(wildcard);
        assertThat(results.isEmpty(), is(true));
    }

}
