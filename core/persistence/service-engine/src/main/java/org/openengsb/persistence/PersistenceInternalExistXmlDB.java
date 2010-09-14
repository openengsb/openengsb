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

package org.openengsb.persistence;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.persistence.NodeTraverser.Condition;
import org.w3c.dom.Node;
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

    private Collection rootCollection;
    private CollectionManagementService collectionMgtService;

    public PersistenceInternalExistXmlDB() {
        init();
    }

    @Override
    public void create(PersistenceObject bean) throws PersistenceException {
        try {
            doCreate(bean);
        } catch (XMLDBException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void create(List<PersistenceObject> elements) throws PersistenceException {
        log.debug("creating " + elements.size() + " new elements");
        for (PersistenceObject po : elements) {
            create(po);
        }
    }

    @Override
    public List<PersistenceObject> query(PersistenceObject example) throws PersistenceException {
        try {
            return doQuery(example);
        } catch (XMLDBException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<PersistenceObject> query(List<PersistenceObject> example) throws PersistenceException {
        List<PersistenceObject> result = new ArrayList<PersistenceObject>();
        for (PersistenceObject po : example) {
            result.addAll(query(po));
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

    @Override
    public void update(Map<PersistenceObject, PersistenceObject> elements) throws PersistenceException {
        for (Entry<PersistenceObject, PersistenceObject> entry : elements.entrySet()) {
            update(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void delete(PersistenceObject example) throws PersistenceException {
        try {
            Collection col = getOrCreateCollection(example.getClassName());
            ResourceSet result = queryExistDB(col, example.getXml());
            for (ResourceIterator it = result.getIterator(); it.hasMoreResources();) {
                Resource r = it.nextResource();
                col.removeResource(r);
            }
        } catch (XMLDBException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void delete(List<PersistenceObject> examples) throws PersistenceException {
        for (PersistenceObject o : examples) {
            delete(o);
        }
    }

    private void doCreate(PersistenceObject o) throws XMLDBException {
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

    private XPathQueryService getXPathQueryService(Collection col) throws XMLDBException {
        log.debug("retrieving XPathQueryService for " + col.getName());
        XPathQueryService result = (XPathQueryService) col.getService("XPathQueryService", "1.0");
        result.setProperty("indent", "yes");
        log.debug("collections contains the following resources");
        for (String s : col.listResources()) {
            log.debug(s);
        }
        return result;
    }

    private List<PersistenceObject> doQuery(PersistenceObject example) throws XMLDBException, PersistenceException {
        List<PersistenceObject> result = new ArrayList<PersistenceObject>();
        Collection coll = getOrCreateCollection(example.getClassName());
        ResourceSet queryResult = queryExistDB(coll, example.getXml());
        for (ResourceIterator it = queryResult.getIterator(); it.hasMoreResources();) {
            Resource r = it.nextResource();
            PersistenceObject resultObject = new PersistenceObject((String) r.getContent(), example.getClassName());
            result.add(resultObject);
        }
        return result;
    }

    private ResourceSet queryExistDB(Collection coll, String sampleXml) throws XMLDBException, PersistenceException {
        String queryString;
        try {
            queryString = makeQuery(sampleXml);
            // queryString = "/*";
            log.debug("querying database with String: ");
            log.debug(queryString);
        } catch (TransformerException e) {
            throw new PersistenceException(e);
        }
        XPathQueryService service = getXPathQueryService(coll);
        return service.query(queryString);
    }

    private String makeQuery(String xml) throws TransformerException {
        Node doc = transformToDOM(xml);
        Iterable<Condition> conditions = NodeTraverser.getConditions(doc);
        Iterator<Condition> it = conditions.iterator();
        if (!it.hasNext()) {
            return "/*";
        }
        StringBuffer query = new StringBuffer();
        query.append("/*[");

        Condition firstEntry = it.next();
        query.append(makeCondition(firstEntry.key, firstEntry.value));
        it.remove();
        for (Condition entry : conditions) {
            if (!entry.value.trim().isEmpty()) {
                query.append(" and ");
                query.append(makeCondition(entry.key, entry.value));
            }
        }
        query.append("]");
        System.out.println(query);
        return query.toString();
    }

    private static String makeCondition(String key, String value) {
        return key + "[.='" + value + "'] != ''";
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

    private void init() {
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

}
