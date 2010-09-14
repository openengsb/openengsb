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

package org.openengsb.util.exist;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;

public class ExistDatabaseConnection implements DatabaseConnection {

    private Log log = LogFactory.getLog(getClass());
    private String connectionUri;
    private boolean connectionAvaiable;
    private HashMap<String, Collection> collections = new HashMap<String, Collection>();

    public void setConnectionUri(String connectionUri) {
        this.connectionUri = connectionUri;
    }

    @Override
    public void storeContentNodeToDatabase(String structure, String name, String content) {
        connectionToDatabase();
        Collection nodeAccess = createOrReuseNodeStructure(structure);
        storeContentToNodeAccess(name, content, nodeAccess);
    }

    private void connectionToDatabase() {
        if (!this.connectionAvaiable) {
            this.log.debug("Since no connection exists try to initialize one for " + this.connectionUri);
            initializeConnection();
        } else {
            this.log.debug("Connection found for " + this.connectionUri);
        }
    }

    private void initializeConnection() {
        try {
            Class<?> cl = Class.forName("org.exist.xmldb.DatabaseImpl");
            Database database = (Database) cl.newInstance();
            DatabaseManager.registerDatabase(database);
            this.connectionAvaiable = true;
        } catch (Exception e) {
            throw new DatabaseException("No connection to log database could be established", e);
        }
    }

    private Collection createOrReuseNodeStructure(String structure) {
        if (this.collections.containsKey(structure)) {
            return this.collections.get(structure);
        }
        String[] splittedStructure = structure.split("\\/");
        for (int i = 1; i < splittedStructure.length; i++) {
            String partLookup = "";
            for (int j = 1; j <= i; j++) {
                partLookup += "/";
                partLookup += splittedStructure[j];
            }
            lookupOrCreateStructureNode(partLookup);
        }
        return this.collections.get(structure);
    }

    private void lookupOrCreateStructureNode(String structure) {
        if (this.collections.containsKey(structure)) {
            this.log.debug("Found access node in internal cache " + structure);
            return;
        }
        Collection databaseAccessNode;
        try {
            this.log.debug("Looking for existing accessNode " + structure);
            databaseAccessNode = DatabaseManager.getCollection(this.connectionUri + structure);
            if (databaseAccessNode == null) {
                databaseAccessNode = createDatabaseAccessNode(structure);
            } else {
                this.log.debug("Found access node on server " + structure);
            }
            this.collections.put(structure, databaseAccessNode);
        } catch (XMLDBException e) {
            throw new DatabaseException("Collection could not be retrieved");
        }
    }

    private Collection createDatabaseAccessNode(String structure) throws XMLDBException {
        Collection databaseAccessNode;
        String[] splittedStructure = structure.split("\\/");
        String struct = "";
        String head = "";
        for (int i = 1; i < splittedStructure.length; i++) {
            if (i == splittedStructure.length - 1) {
                head = splittedStructure[i];
            } else {
                struct += "/";
                struct += splittedStructure[i];
            }
        }
        Collection root = this.collections.get(struct);
        CollectionManagementService mgtService = (CollectionManagementService) root.getService(
                "CollectionManagementService", "1.0");
        this.log.debug("Creating access node " + structure);
        databaseAccessNode = mgtService.createCollection(head);
        return databaseAccessNode;
    }

    private void storeContentToNodeAccess(String name, String content, Collection nodeAccess) {
        try {
            XMLResource document = (XMLResource) nodeAccess.createResource(name, "XMLResource");
            document.setContent(content);
            this.log.debug("Storeing to " + nodeAccess.getName() + "/" + name + " " + content);
            nodeAccess.storeResource(document);
        } catch (XMLDBException e) {
            throw new DatabaseException("Cant store node to database", e);
        }
    }

}
