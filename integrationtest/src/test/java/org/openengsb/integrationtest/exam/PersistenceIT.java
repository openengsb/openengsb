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

package org.openengsb.integrationtest.exam;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.persistence.PersistenceManager;
import org.openengsb.core.persistence.PersistenceService;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class PersistenceIT extends AbstractExamTestHelper {

    private PersistenceService persistence;
    private PersistenceTestObject element;
    private PersistenceTestObject wildcard;

    @Before
    public void setUp() throws Exception {
        PersistenceManager manager = retrieveService(getBundleContext(), PersistenceManager.class);
        persistence = manager.getPersistenceForBundle(getBundleContext().getBundle());

        element = new PersistenceTestObject("42", 42);
        persistence.create(element);

        wildcard = new PersistenceTestObject(null, null);
    }

    @After
    public void tearDown() throws Exception {
        if (persistence.query(wildcard).isEmpty()) {
            return;
        }
        persistence.delete(wildcard);
    }

    @Test
    public void testCreateAndQuery() throws Exception {
        PersistenceTestObject test = new PersistenceTestObject("test", 1);
        persistence.create(test);
        List<PersistenceTestObject> result = persistence.query(new PersistenceTestObject("test", null));
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(test));
    }

    @Test
    public void testUpdateAndQuery() throws Exception {
        element.setString("foo");

        persistence.update(persistence.query(wildcard).get(0), element);

        List<PersistenceTestObject> result = persistence.query(wildcard);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getString(), is("foo"));
    }

    @Test
    public void testDelete() throws Exception {
        persistence.delete(element);
        List<PersistenceTestObject> result = persistence.query(wildcard);
        assertThat(result.isEmpty(), is(true));
    }

}
