package org.openengsb.logging;

import java.util.UUID;

import org.exist.storage.DBBroker;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;

/**
 * Add a document to the database.
 * 
 * Call with java -jar start.jar org.exist.examples.xmldb.Put collection docName
 * 
 */
public class Put {

    public final static String URI = "xmldb:exist://localhost:8093/exist/xmlrpc";

    protected static void usage() {
        System.out.println("usage: org.exist.examples.xmldb.Put collection docName");
        System.exit(0);
    }

    public static void main(String args[]) throws Exception {
        // initialize driver
        String driver = "org.exist.xmldb.DatabaseImpl";
        Class cl = Class.forName(driver);
        Database database = (Database) cl.newInstance();
        DatabaseManager.registerDatabase(database);

        // try to get collection
        Collection col = DatabaseManager.getCollection(URI + "/db/andi");
        if (col == null) {
            // collection does not exist: get root collection and create.
            // for simplicity, we assume that the new collection is a
            // direct child of the root collection, e.g. /db/test.
            // the example will fail otherwise.
            Collection root = DatabaseManager.getCollection(URI + DBBroker.ROOT_COLLECTION);
            CollectionManagementService mgtService = (CollectionManagementService) root.getService(
                    "CollectionManagementService", "1.0");
            col = mgtService.createCollection("/db/andi".substring((DBBroker.ROOT_COLLECTION + "/").length()));
        }
        // create new XMLResource
        String fileName = UUID.randomUUID().toString();
        XMLResource document = (XMLResource) col.createResource(fileName, "XMLResource");
        document.setContent("<test>" + fileName + "</test>");
        System.out.println("---------------------");
        System.out.println("---------------------");
        System.out.print("storing document " + document.getId() + "...");
        col.storeResource(document);
        System.out.println("ok!");
        System.out.println("---------------------");
        System.out.println("---------------------");
    }
}
