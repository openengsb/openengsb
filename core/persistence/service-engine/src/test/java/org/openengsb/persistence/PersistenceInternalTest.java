package org.openengsb.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;
import java.util.List;

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
import org.w3c.dom.Node;

public abstract class PersistenceInternalTest {

    private static Log log = LogFactory.getLog(PersistenceInternalTest.class);

    protected PersistenceInternal persistence;
    protected static UniversalJaxbSerializer serializer;

    protected SimpleTestBean simple1;

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
        simple1 = new SimpleTestBean(42, "bleh");
        simple1.moreContent = "moaaar";
    }

    @After
    public void tearDown() throws Exception {
    }

    protected abstract PersistenceInternal getPersistenceImpl() throws Exception;

    @Test
    public void testCreateAndQueryAll() throws Exception {
        PersistenceObject s1 = makePersistenceObject(simple1);
        persistence.create(s1);
        SimpleTestBean example = new SimpleTestBean();
        PersistenceObject sample = makePersistenceObject(example);
        List<PersistenceObject> resultList = persistence.query(sample);
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
    }

    @Test
    public void testCreateAndQuery() throws Exception {
        PersistenceObject s1 = makePersistenceObject(simple1);
        persistence.create(s1);
        SimpleTestBean example = new SimpleTestBean(42, null);
        PersistenceObject sample = makePersistenceObject(example);
        List<PersistenceObject> resultList = persistence.query(sample);
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        PersistenceObject resultPo = resultList.get(0);
        SimpleTestBean result = (SimpleTestBean) parsePersistenceObject(resultPo);
        assertEquals(simple1, result);
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
