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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.persistence.PersistenceService;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class PersistenceIT extends AbstractExamTestHelper {

    private PersistenceService persistence;
    private TestObject element;
    private TestObject wildcard;

    @Before
    public void setUp() throws Exception {
        persistence = retrieveService(getBundleContext(), PersistenceService.class);

        element = new TestObject("42", 42);
        persistence.create(element);

        wildcard = new TestObject(null, null);
    }

    @Test
    public void testCreateAndQuery() throws Exception {
        TestObject test = new TestObject("test", 1);
        persistence.create(test);
        List<TestObject> result = persistence.query(new TestObject("test", null));
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(test));
    }

    @Test
    public void testUpdateAndQuery() throws Exception {
        element.string = "foo";

        persistence.update(persistence.query(wildcard).get(0), element);

        List<TestObject> result = persistence.query(wildcard);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getString(), is("foo"));
    }

    @Test
    public void testDelete() throws Exception {
        persistence.delete(element);
        List<TestObject> result = persistence.query(wildcard);
        assertThat(result.isEmpty(), is(true));
    }

    public static class TestObject {
        private String string;

        private Integer integer;

        public TestObject(String string, Integer integer) {
            this.string = string;
            this.integer = integer;
        }

        public Integer getInteger() {
            return integer;
        }

        public String getString() {
            return string;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((integer == null) ? 0 : integer.hashCode());
            result = prime * result + ((string == null) ? 0 : string.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestObject other = (TestObject) obj;
            return safeEquals(this.string, other.string) && safeEquals(this.integer, other.integer);
        }

        private boolean safeEquals(Object a, Object b) {
            if (a == null) {
                return b == null;
            }
            return a.equals(b);
        }

    }
}
