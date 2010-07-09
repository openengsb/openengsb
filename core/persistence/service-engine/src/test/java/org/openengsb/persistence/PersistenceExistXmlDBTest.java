package org.openengsb.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;

public class PersistenceExistXmlDBTest {

    private PersistenceInternal persistence;
    private static final String DBURI = "xmldb:exist:///db";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        persistence = new PersistenceInternalExistXmlDB();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreate1() throws Exception {
        String xml = "<user><name>gladiator-2003</name></user>";
        String className = this.getClass().getName();
        PersistenceObject o = new PersistenceObject(xml, className);
        List<PersistenceObject> l = new ArrayList<PersistenceObject>();
        l.add(o);
        persistence.create(l);
        Collection collection = DatabaseManager.getCollection(DBURI + "/" + className, "admin", "");
        assertNotNull(collection);
        String[] result = collection.listResources();
        Assert.assertTrue(result.length > 0);
        for (String s : result) {
//            collection.removeResource(collection.getResource(s));
        }
    }

    @Test
    @Ignore("nyi")
    public void testQuery() throws Exception {
        Collection col = DatabaseManager.getCollection(DBURI + "/" + this.getClass().getName(), "admin", "");
        XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");
        service.setProperty("indent", "yes");
        String query = "/user[name=\"gladiator-2003\"]";
        ResourceSet result = service.query(query);
        assertTrue(result.getSize() > 0);
        // result.getIterator().nextResource().
        query = "/user[name=\"gladiator-2042\"]";
        result = service.query(query);
        assertTrue(result.getSize() == 0);
    }

    private static Document toXml(String xml) throws TransformerFactoryConfigurationError, TransformerException {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        StreamSource source = new StreamSource(new StringReader(xml));
        DOMResult result = new DOMResult();
        t.transform(source, result);
        return (Document) result.getNode();
    }

}
