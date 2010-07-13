package org.openengsb.persistence;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;

public class PersistenceInternalExistXmlDB implements PersistenceInternal {

    private static final Log log = LogFactory.getLog(PersistenceInternalExistXmlDB.class);

    private static final String DB_URI = "xmldb:exist://";

    public PersistenceInternalExistXmlDB() {
        try {
            log.debug("starting embedded eXist-database");
            Class<?> cl = Class.forName("org.exist.xmldb.DatabaseImpl");
            Database database = (Database) cl.newInstance();
            database.setProperty("create-database", "true");
            DatabaseManager.registerDatabase(database);
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

    private Collection getOrCreateCollection(String name) throws XMLDBException {
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        Collection col = DatabaseManager.getCollection(DB_URI + name, "admin", "");
        if (col == null) {
            // collection does not exist: get root collection and create
            // for simplicity, we assume that the new collection is a
            // direct child of the root collection, e.g. /db/test.
            // the example will fail otherwise.
            Collection root = DatabaseManager.getCollection(DB_URI + "/db");
            CollectionManagementService mgtService = (CollectionManagementService) root.getService(
                    "CollectionManagementService", "1.0");
            col = mgtService.createCollection(name);
        }
        return col;
    }

    @Override
    public void create(List<PersistenceObject> elements) {
        for (PersistenceObject o : elements) {
            try {
                Collection col = getOrCreateCollection(o.getClassName());
                String xml = o.getXml();
                String id = "" + xml.hashCode();

                XMLResource resource = (XMLResource) col.createResource(id, XMLResource.RESOURCE_TYPE);
                resource.setContent(xml);
                // resource.setContentAsDOM(document);
                col.storeResource(resource);
            } catch (XMLDBException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void delete(List<PersistenceObject> examples) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<PersistenceObject> query(List<PersistenceObject> example) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update(PersistenceObject oldElement, PersistenceObject newElement) {
        // TODO Auto-generated method stub
    }

    public void update(Map<Object, Object> elements) {

    }

}
