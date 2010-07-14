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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Node;

@RunWith(Parameterized.class)
public abstract class PersistenceInternalTest {

    private static Log log = LogFactory.getLog(PersistenceInternalTest.class);

    protected PersistenceInternal persistence;
    protected static UniversalJaxbSerializer serializer;

    protected Class<?> objectClass;
    protected Object o1;
    protected Object sample1;
    protected Object updated1;

    public PersistenceInternalTest(Class<?> objectClass, Object o1, Object sample1, Object udpated1) {
        this.objectClass = objectClass;
        this.o1 = o1;
        this.sample1 = sample1;
        this.updated1 = udpated1;
    }

    @Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> result = new LinkedList<Object[]>();
        result.add(new Object[] { SimpleTestBean.class, new SimpleTestBean(42, "bla"), new SimpleTestBean(42, null),
                new SimpleTestBean(42, "blah"), });

        ComplexTestBean o1 = new ComplexTestBean();
        o1.simple = new SimpleTestBean(42, "42");
        ComplexTestBean sample1 = new ComplexTestBean();
        sample1.simple = new SimpleTestBean(42, "42");
        ComplexTestBean updated1 = new ComplexTestBean();
        updated1.simple = new SimpleTestBean(42, "21");

        result.add(new Object[] { ComplexTestBean.class, o1, sample1, updated1, });

        ComplexTestBean o2 = new ComplexTestBean();
        o2.simple = new SimpleTestBean(42, "42");
        List<String> testList = new ArrayList<String>();
        testList.add("item1");
        testList.add("item2");
        o2.testList = testList;

        ComplexTestBean sample2 = new ComplexTestBean();
        sample2.simple = new SimpleTestBean(42, "42");
        ComplexTestBean updated2 = new ComplexTestBean();
        updated2.simple = new SimpleTestBean(42, "42");
        Map<Long, String> testMap = new HashMap<Long, String>();
        testMap.put(1L, "value1");
        testMap.put(2L, "value");
        updated2.testMap = testMap;

        result.add(new Object[] { ComplexTestBean.class, o2, sample2, updated2, });

        return result;
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        serializer = new UniversalJaxbSerializer();

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        persistence = getPersistenceImpl();
    }

    @After
    public void tearDown() throws Exception {
    }

    protected abstract PersistenceInternal getPersistenceImpl() throws Exception;

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

    protected Collection<Object> convertResult(Collection<PersistenceObject> resultList) throws JAXBException,
            ClassNotFoundException {
        Collection<Object> result = new HashSet<Object>();
        for (PersistenceObject po : resultList) {
            Object o = parsePersistenceObject(po);
            result.add(o);
        }
        return result;
    }

    protected PersistenceObject makePersistenceObject(Object o) throws JAXBException {
        String doc = serializer.serialize(o);
        log.debug(doc);
        String className = o.getClass().getName();
        return new PersistenceObject(doc, className);
    }

    protected Object parsePersistenceObject(PersistenceObject po) throws JAXBException, ClassNotFoundException {
        Class<?> objectClass = Class.forName(po.getClassName());
        return serializer.deserialize(objectClass, po.getXml());
    }

    protected void printNode(Node s) throws TransformerException {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringWriter sw = new StringWriter();
        t.transform(new DOMSource(s), new StreamResult(sw));
        log.debug(sw);
    }
}
