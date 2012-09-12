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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.ModelGraph;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.index.OIndexes;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * The EKB model graph is an implementation of the model graph. It uses a graph database as storage.
 * 
 * It usually get filled by two components: the TransformationEngineService and the ModelRegistryService. The
 * TransformationEngineService inserts all transformations it get saved as new edges into the graph database. The
 * ModelRegistry notifies the graph whenever new models get available or models get unavailable.
 */
public final class OrientModelGraph implements ModelGraph {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrientModelGraph.class);
    private static final String ACTIVE_FIELD = "isActive";
    private static final String FILENAME = "filename";
    private OGraphDatabase graph;
    private Map<String, TransformationDescription> descriptions;
    private AtomicLong counter;

    public OrientModelGraph() {
        startup();
        descriptions = new HashMap<String, TransformationDescription>();
        counter = new AtomicLong(0L);
    }

    private void startup() {
        ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader orientClassLoader = OIndexes.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(orientClassLoader);
            graph = new OGraphDatabase("memory:ekbgraphdb").create();
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
        Orient.instance().removeShutdownHook();
        graph.createVertexType("Models");
    }

    /**
     * Shuts down the graph database
     */
    public void shutdown() {
        ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader orientClassLoader = OIndexes.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(orientClassLoader);
            graph.close();
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }

    /**
     * Deletes all edges and nodes from the graph database. This method is used in the tests to have at every test the
     * same start situation.
     */
    public void cleanDatabase() {
        for (ODocument edge : graph.browseEdges()) {
            edge.delete();
        }
        for (ODocument node : graph.browseVertices()) {
            node.delete();
        }
    }

    @Override
    public void addModel(ModelDescription model) {
        ODocument node = getModel(model.toString());
        if (node == null) {
            node = graph.createVertex("Models");
            setIdFieldValue(node, model.toString());
        }
        setActiveFieldValue(node, true);
        node.save();
        LOGGER.debug("Added model {} to the graph database", model);
    }

    @Override
    public void removeModel(ModelDescription model) {
        ODocument node = getModel(model.toString());
        if (node == null) {
            LOGGER.warn("Couldn't remove model {} since it wasn't present in the graph database", model);
            return;
        }
        setActiveFieldValue(node, false);
        node.save();
        LOGGER.debug("Removed model {} from the graph database", model);
    }

    @Override
    public void addTransformation(TransformationDescription description) {
        checkTransformationDescriptionId(description);
        ODocument source = getOrCreateModel(description.getSourceModel().toString());
        ODocument target = getOrCreateModel(description.getTargetModel().toString());
        ODocument edge = graph.createEdge(source, target);
        setIdFieldValue(edge, description.getId());
        if (description.getFileName() != null) {
            setFilenameFieldValue(edge, description.getFileName());
        }
        fillEdgeWithPropertyConnections(edge, description);
        edge.save();
        descriptions.put(description.getId(), description);
        LOGGER.debug("Added transformation description {} to the graph database", description);
    }

    /**
     * Adds to the given map the property connection information which result from the given transformation description.
     */
    private void fillEdgeWithPropertyConnections(ODocument edge, TransformationDescription description) {
        Map<String, String> connections = convertPropertyConnectionsToSimpleForm(description.getPropertyConnections());
        for (Map.Entry<String, String> entry : connections.entrySet()) {
            edge.field(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Converts a complex property connection map (key:String, value:Set of Strings) to a simple property connection
     * map, so that the connections can easily be saved to the edges in the model graph.
     */
    private Map<String, String> convertPropertyConnectionsToSimpleForm(Map<String, Set<String>> connections) {
        Map<String, String> result = new HashMap<String, String>();
        if (connections == null) {
            return result;
        }
        for (Map.Entry<String, Set<String>> entry : connections.entrySet()) {
            result.put(entry.getKey(), convertStringSetToString(entry.getValue()));
        }
        return result;
    }

    /**
     * Converts a set of strings to a simple string, with a ',' char as separator char.
     */
    private String convertStringSetToString(Set<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String property : strings) {
            if (builder.length() != 0) {
                builder.append(",");
            }
            builder.append(property);
        }
        return builder.toString();
    }

    @Override
    public void removeTransformation(TransformationDescription description) {
        String source = description.getSourceModel().toString();
        String target = description.getTargetModel().toString();
        for (ODocument edge : getEdgesBetweenModels(source, target)) {
            String id = getIdFieldValue(edge);
            if (description.getId() == null && isInternalId(id)) {
                edge.delete();
                descriptions.remove(id);
                LOGGER.debug("Removed transformation description {} from the graph database", id);
            } else if (id.equals(description.getId())) {
                edge.delete();
                descriptions.remove(id);
                LOGGER.debug("Removed transformation description {} from the graph database", id);
                break;
            }
        }
    }

    @Override
    public List<TransformationDescription> getTransformationsPerFileName(String filename) {
        String query = String.format("select from E where %s = ?", FILENAME);
        List<ODocument> edges = graph.query(new OSQLSynchQuery<ODocument>(query), filename);
        List<TransformationDescription> result = new ArrayList<TransformationDescription>();
        for (ODocument edge : edges) {
            result.add(descriptions.get(getIdFieldValue(edge)));
        }
        return result;
    }

    @Override
    public List<TransformationDescription> getTransformationPath(ModelDescription source, ModelDescription target,
            List<String> ids) {
        if (ids == null) {
            ids = new ArrayList<String>();
        }
        List<ODocument> path = recursivePathSearch(source.toString(), target.toString(), ids, new ODocument[0]);
        List<TransformationDescription> result = new ArrayList<TransformationDescription>();
        if (path != null) {
            for (ODocument edge : path) {
                result.add(descriptions.get(getIdFieldValue(edge)));
            }
            return result;
        }
        throw new IllegalArgumentException("No transformation description found");
    }

    @Override
    public Boolean isTransformationPossible(ModelDescription source, ModelDescription target, List<String> ids) {
        try {
            getTransformationPath(source, target, ids);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Tests if a transformation description has an id and adds an unique id if it hasn't one.
     */
    private void checkTransformationDescriptionId(TransformationDescription description) {
        if (description.getId() == null) {
            description.setId("EKBInternal-" + counter.incrementAndGet());
        }
    }

    /**
     * Returns true if the given id is an automatically generated id, or a user defined one.
     */
    private boolean isInternalId(String id) {
        return id.startsWith("EKBInternal-");
    }

    /**
     * Returns all edges which start at the source model and end in the target model.
     */
    private List<ODocument> getEdgesBetweenModels(String source, String target) {
        ODocument from = getModel(source);
        ODocument to = getModel(target);
        String query = "select from E where out = ? AND in = ?";
        return graph.query(new OSQLSynchQuery<ODocument>(query), from, to);
    }

    /**
     * Returns all neighbors of a model.
     */
    private List<ODocument> getNeighborsOfModel(String model) {
        String query = String.format("select from Models where in.out.%s in [?]", OGraphDatabase.LABEL);
        List<ODocument> neighbors = graph.query(new OSQLSynchQuery<ODocument>(query), model);
        return neighbors;
    }

    /**
     * Returns the model with the given name, or creates one if it isn't existing until then and returns the new one.
     */
    private ODocument getOrCreateModel(String model) {
        ODocument node = getModel(model);
        if (node == null) {
            node = graph.createVertex("Models");
            setIdFieldValue(node, model.toString());
            setActiveFieldValue(node, false);
            node.save();
        }
        return node;
    }

    /**
     * Returns the model with the given name.
     */
    private ODocument getModel(String model) {
        String query = String.format("select from Models where %s = ?", OGraphDatabase.LABEL);
        List<ODocument> from = graph.query(new OSQLSynchQuery<ODocument>(query), model);
        if (from.size() > 0) {
            return from.get(0);
        } else {
            return null;
        }
    }

    /**
     * Recursive path search function. It performs a depth first search with integrated loop check to find a path from
     * the start model to the end model. If the id list is not empty, then the function only returns a path as valid if
     * all transformations defined with the id list are in the path. It also takes care of models which aren't
     * available. Returns null if there is no path found.
     */
    private List<ODocument> recursivePathSearch(String start, String end, List<String> ids, ODocument... steps) {
        List<ODocument> neighbors = getNeighborsOfModel(start);
        for (ODocument neighbor : neighbors) {
            if (alreadyVisited(neighbor, steps) || !getActiveFieldValue(neighbor)) {
                continue;
            }
            ODocument nextStep = getEdgeWithPossibleId(start, getIdFieldValue(neighbor), ids);
            if (nextStep == null) {
                continue;
            }
            if (getIdFieldValue(neighbor).equals(end)) {
                List<ODocument> result = new ArrayList<ODocument>();
                List<String> copyIds = new ArrayList<String>(ids);
                for (ODocument step : steps) {
                    String id = getIdFieldValue(step);
                    if (id != null && copyIds.contains(id)) {
                        copyIds.remove(id);
                    }
                    result.add(step);
                }
                String id = getIdFieldValue(nextStep);
                if (id != null && copyIds.contains(id)) {
                    copyIds.remove(id);
                }
                result.add(nextStep);
                if (copyIds.isEmpty()) {
                    return result;
                }
            }
            ODocument[] path = Arrays.copyOf(steps, steps.length + 1);
            path[path.length - 1] = nextStep;
            List<ODocument> check = recursivePathSearch(getIdFieldValue(neighbor), end, ids, path);
            if (check != null) {
                return check;
            }
        }
        return null;
    }

    /**
     * Returns an edge between the start and the end model. If there is an edge which has an id which is contained in
     * the given id list, then this transformation is returned. If not, then the first found is returned.
     */
    private ODocument getEdgeWithPossibleId(String start, String end, List<String> ids) {
        List<ODocument> edges = getEdgesBetweenModels(start, end);
        for (ODocument edge : edges) {
            if (ids.contains(getIdFieldValue(edge))) {
                return edge;
            }
        }
        return edges.size() != 0 ? edges.get(0) : null;
    }

    /**
     * Checks if a model is already visited in the path search algorithm. Needed for the loop detection.
     */
    private boolean alreadyVisited(ODocument neighbor, ODocument[] steps) {
        for (ODocument step : steps) {
            ODocument out = graph.getOutVertex(step);
            if (out.equals(neighbor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the value for the id field of a graph object.
     */
    private String getIdFieldValue(ODocument document) {
        return getFieldValue(document, OGraphDatabase.LABEL);
    }

    /**
     * Sets the value for the id field of a graph object.
     */
    private void setIdFieldValue(ODocument document, String value) {
        setFieldValue(document, OGraphDatabase.LABEL, value);
    }

    /**
     * Gets the value for the active field of a graph object.
     */
    private Boolean getActiveFieldValue(ODocument document) {
        return Boolean.parseBoolean(getFieldValue(document, ACTIVE_FIELD));
    }

    /**
     * Sets the value for the active field of a graph object.
     */
    private void setActiveFieldValue(ODocument document, Boolean active) {
        setFieldValue(document, ACTIVE_FIELD, active.toString());
    }

    /**
     * Sets the value for the filename field of a graph object.
     */
    private void setFilenameFieldValue(ODocument document, String value) {
        setFieldValue(document, FILENAME, value);
    }

    /**
     * Gets the value for the given field of a graph object.
     */
    private String getFieldValue(ODocument document, String fieldname) {
        return (String) document.field(fieldname);
    }

    /**
     * Sets the value for the given field of an graph object.
     */
    private void setFieldValue(ODocument document, String fieldname, String value) {
        document.field(fieldname, value);
    }

    /**
     * Returns true if the model is currently active, returns false if not.
     */
    public boolean isModelActive(ModelDescription model) {
        ODocument node = getModel(model.toString());
        return getActiveFieldValue(node);
    }
}
