/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public abstract class PersistenceInternalTest extends PersistenceTest {

    public PersistenceInternalTest(Class<?> objectClass, Object o1, Object sample1, Object udpated1) {
        super(objectClass, o1, sample1, udpated1);
    }

    @Test
    public void testCreateAndQueryAll() throws Exception {
        PersistenceObject s1 = makePersistenceObject(o1);
        persistence.create(s1);
        Object example = objectClass.newInstance();
        Collection<Object> resultObjects = doQuery(example);
        assertTrue(resultObjects.contains(o1));
    }

    @Test
    public void testCreateAndQuery() throws Exception {
        PersistenceObject s1 = makePersistenceObject(o1);
        persistence.create(s1);
        Object example = sample1;
        Collection<Object> resultObjects = doQuery(example);
        assertTrue(resultObjects.contains(o1));
    }

    @Test
    public void testUpdate() throws Exception {
        PersistenceObject s1 = makePersistenceObject(o1);
        persistence.create(s1);
        PersistenceObject s1u = makePersistenceObject(updated1);
        persistence.update(s1, s1u);
        Collection<Object> result = doQuery(updated1);
        assertTrue(result.contains(updated1));
    }

    @Test
    public void testDelete() throws Exception {
        PersistenceObject s1 = makePersistenceObject(o1);
        persistence.create(s1);
        persistence.delete(s1);
        Collection<Object> result = doQuery(objectClass.newInstance());
        assertFalse(result.contains(o1));
    }

    protected Collection<Object> doQuery(Object example) throws JAXBException, PersistenceException,
            ClassNotFoundException {
        PersistenceObject sample = makePersistenceObject(example);
        List<PersistenceObject> resultList = persistence.query(sample);
        assertNotNull(resultList);
        Collection<Object> resultObjects = convertResult(resultList);
        return resultObjects;
    }

}
