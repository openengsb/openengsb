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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

public class PersistenceInternalExistXmlDB implements PersistenceInternal {

    private static final Log log = LogFactory.getLog(PersistenceInternalExistXmlDB.class);

    private static final String DB_URI = "xmldb:exist:///db";

    private final Collection rootCollection;
    private CollectionManagementService collectionMgtService;

    public PersistenceInternalExistXmlDB() {
        try {
            initEmbeddedExist();

            log.debug("database registered. Now retrieving references to collections and services");

            rootCollection = DatabaseManager.getCollection(DB_URI, "admin", "");
            collectionMgtService = (CollectionManagementService) rootCollection.getService(
                    "CollectionManagementService", "1.0");

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("unable to start embedded eXist-db", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("unable to start embedded eXist-db", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("unable to start embedded eXist-db", e);
        } catch (XMLDBException e) {
            throw new RuntimeException("unable to start embedded eXist-db", e);
        }
    }

    private void initEmbeddedExist() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            XMLDBException {
        log.debug("starting embedded eXist-database");
        Class<?> cl = Class.forName("org.exist.xmldb.DatabaseImpl");
        Database database = (Database) cl.newInstance();
        database.setProperty("create-database", "true");
        DatabaseManager.registerDatabase(database);
    }

    private XPathQueryService getXPathQueryService(Collection col) throws XMLDBException {
        XPathQueryService result = (XPathQueryService) col.getService("XPathQueryService", "1.0");
        result.setProperty("indent", "yes");
        return result;
    }

    private Collection getOrCreateCollection(String name) throws XMLDBException {
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        Collection col = DatabaseManager.getCollection(DB_URI + name, "admin", "");
        if (col == null) {
            log.info("collection for " + name + " not found, creating new");
            col = collectionMgtService.createCollection(name);
        }
        return col;
    }

    @Override
    public void create(List<PersistenceObject> elements) throws PersistenceException {
        try {
            doCreate(elements);
        } catch (XMLDBException e) {
            throw new PersistenceException(e);
        }
    }

    private void doCreate(List<PersistenceObject> elements) throws XMLDBException {
        for (PersistenceObject o : elements) {
            log.debug("retrieve collection for classname " + o.getClassName());
            Collection col = getOrCreateCollection(o.getClassName());

            String xml = o.getXml();
            String id = "" + xml.hashCode();
            log.debug("generated id from xml-string: " + id);

            XMLResource resource = (XMLResource) col.createResource(id, XMLResource.RESOURCE_TYPE);
            resource.setContent(xml);
            log.debug("store resource to collection");
            col.storeResource(resource);
        }
    }

    @Override
    public void delete(List<PersistenceObject> examples) throws PersistenceException {
        try {
            for (PersistenceObject o : examples) {
                Collection col = getOrCreateCollection(o.getClassName());
                ResourceSet result = queryExistDB(col, o.getXml());
                for (ResourceIterator it = result.getIterator(); it.hasMoreResources();) {
                    Resource r = it.nextResource();
                    col.removeResource(r);
                }
            }
        } catch (XMLDBException e) {
            throw new PersistenceException(e);
        }
    }

    private String makeCondition(String varName, String value) {
        return String.format("bean/fields[fieldName=\"%s\"]/value//*[.=\"%s\"] != ''", varName, value);
    }

    @Override
    public List<PersistenceObject> query(List<PersistenceObject> example) throws PersistenceException {
        try {
            return doQuery(example);
        } catch (XMLDBException e) {
            throw new PersistenceException(e);
        }
    }

    private List<PersistenceObject> doQuery(List<PersistenceObject> example) throws XMLDBException {
        List<PersistenceObject> result = new ArrayList<PersistenceObject>();
        for (PersistenceObject o : example) {
            Collection coll = getOrCreateCollection(o.getClassName());
            ResourceSet queryResult = queryExistDB(coll, o.getXml());
            for (ResourceIterator it = queryResult.getIterator(); it.hasMoreResources();) {
                Resource r = it.nextResource();
                PersistenceObject resultObject = new PersistenceObject((String) r.getContent(), o.getClassName());
                result.add(resultObject);
            }
        }
        return result;
    }

    private ResourceSet queryExistDB(Collection coll, String sampleXml) throws XMLDBException {
        String queryString = makeQuery(sampleXml);
        XPathQueryService service = getXPathQueryService(coll);
        return service.query(queryString);
    }

    private String makeQuery(String xml) {
        Map<String, String> fields = getFields(xml);
        StringBuffer query = new StringBuffer();
        query.append("/XMLMappable[");
        boolean first = true;
        for (Entry<String, String> e : fields.entrySet()) {
            if (!first) {
                query.append(" and ");
            }
            query.append(makeCondition(e.getKey(), e.getValue()));
            first = false;
        }
        query.append("]");
        return query.toString();
    }

    private Map<String, String> getFields(String xml) {
        Map<String, String> result = new HashMap<String, String>();
        Document doc;
        try {
            doc = (Document) transformToDOM(xml);
        } catch (TransformerException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return result;
        }
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        try {
            XPathExpression fieldsExpr = xpath.compile("//fields"); // "//book[author='Neal Stephenson']/title/text()");
            NodeList nodes = (NodeList) fieldsExpr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node field = nodes.item(i);
                xpath = factory.newXPath();
                XPathExpression nameExpr = xpath.compile(".//fieldName/text()");
                Node nameText = (Node) nameExpr.evaluate(field, XPathConstants.NODE);
                String name = nameText.getNodeValue();
                xpath = factory.newXPath();
                XPathExpression valueExpr = xpath.compile(".//value/primitive/*/text()");
                Node valueText = (Node) valueExpr.evaluate(field, XPathConstants.NODE);
                if (valueText != null) {
                    String value = valueText.getNodeValue();
                    log.debug("found field: " + name + " = " + value);
                    result.put(name, value);
                }
            }
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return result;
        }
        return result;
    }

    @Override
    public void update(PersistenceObject oldElement, PersistenceObject newElement) throws PersistenceException {
        try {
            Collection coll = getOrCreateCollection(oldElement.getClassName());
            ResourceSet result = queryExistDB(coll, oldElement.getXml());
            for (ResourceIterator it = result.getIterator(); it.hasMoreResources();) {
                Resource res = it.nextResource();
                // assume they have the same classname
                res.setContent(newElement.getXml());
                coll.storeResource(res);
            }
        } catch (XMLDBException e) {
            throw new PersistenceException(e);
        }
    }

    public void update(Map<PersistenceObject, PersistenceObject> elements) throws PersistenceException {
        for (Entry<PersistenceObject, PersistenceObject> entry : elements.entrySet()) {
            update(entry.getKey(), entry.getValue());
        }
    }

    public Node transformToDOM(String xml) throws TransformerException {
        Transformer t = getTransformer();
        Source source = new StreamSource(new StringReader(xml));
        DOMResult result = new DOMResult();
        t.transform(source, result);
        return result.getNode();
    }

    private Transformer getTransformer() {
        Transformer t = null;
        try {
            t = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return t;
    }

    protected void reset() throws XMLDBException {
        collectionMgtService.removeCollection("/db");
        log.info("db reset");
    }

    protected void printResourceList(String collname) throws XMLDBException {
        Collection coll = getOrCreateCollection(collname);
        String[] allResources = coll.listResources();
        for (String s : allResources) {
            log.debug("found Resource " + s);
            log.debug("content: " + coll.getResource(s).getContent());
        }
    }

    private void printNode(Node s) {
        try {
            Transformer t = getTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter sw = new StringWriter();
            t.transform(new DOMSource(s), new StreamResult(sw));
            log.debug(sw);
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
