/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.ekb.graph.orient.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.ekb.api.transformation.TransformationDescription;

import com.google.common.base.Joiner;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * The OrientModelGraphUtils class contains utility methods used in the OrientModelGraph class.
 */
public final class OrientModelGraphUtils {
    public static final String ACTIVE_FIELD = "isActive";
    public static final String FILENAME = "filename";
    
    private OrientModelGraphUtils() {        
    }

    /**
     * Gets the value for the id field of a graph object.
     */
    public static String getIdFieldValue(ODocument document) {
        return getFieldValue(document, OGraphDatabase.LABEL);
    }

    /**
     * Sets the value for the id field of a graph object.
     */
    public static void setIdFieldValue(ODocument document, String value) {
        setFieldValue(document, OGraphDatabase.LABEL, value);
    }

    /**
     * Gets the value for the active field of a graph object.
     */
    public static Boolean getActiveFieldValue(ODocument document) {
        return Boolean.parseBoolean(getFieldValue(document, ACTIVE_FIELD));
    }

    /**
     * Sets the value for the active field of a graph object.
     */
    public static void setActiveFieldValue(ODocument document, Boolean active) {
        setFieldValue(document, ACTIVE_FIELD, active.toString());
    }

    /**
     * Sets the value for the filename field of a graph object.
     */
    public static void setFilenameFieldValue(ODocument document, String value) {
        setFieldValue(document, FILENAME, value);
    }

    /**
     * Gets the value for the given field of a graph object.
     */
    public static String getFieldValue(ODocument document, String fieldname) {
        return (String) document.field(fieldname);
    }

    /**
     * Sets the value for the given field of an graph object.
     */
    public static void setFieldValue(ODocument document, String fieldname, String value) {
        document.field(fieldname, value);
    }

    /**
     * Adds to the given map the property connection information which result from the given transformation description.
     */
    public static void fillEdgeWithPropertyConnections(ODocument edge, TransformationDescription description) {
        Map<String, String> connections = convertPropertyConnectionsToSimpleForm(description.getPropertyConnections());
        for (Map.Entry<String, String> entry : connections.entrySet()) {
            edge.field(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Converts a complex property connection map (key:String, value:Set of Strings) to a simple property connection
     * map, so that the connections can easily be saved to the edges in the model graph.
     */
    public static Map<String, String> convertPropertyConnectionsToSimpleForm(Map<String, Set<String>> connections) {
        Map<String, String> result = new HashMap<String, String>();
        if (connections == null) {
            return result;
        }
        for (Map.Entry<String, Set<String>> entry : connections.entrySet()) {
            result.put(entry.getKey(), Joiner.on(',').join(entry.getValue()));
        }
        return result;
    }
}
