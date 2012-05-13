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

package org.openengsb.core.ekb.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.openengsb.core.api.ekb.ModelDescription;
import org.openengsb.core.api.ekb.transformation.TransformationDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * The EKB model graph is used as base for the finding of transformation descriptions. It is using a graph database as
 * storage and is able to perform graph based algorithm on the transformation graph.
 */
public class EKBModelGraph {
    private static final Logger LOGGER = LoggerFactory.getLogger(EKBModelGraph.class);
    private static final String ACTIVE_FIELD = "isActive";
    private static final String FILENAME = "filename";
    private static EKBModelGraph instance;
    private OGraphDatabase graph;
    private Map<String, TransformationDescription> descriptions;
    private AtomicLong counter;

    public static EKBModelGraph getInstance() {
        if (instance == null) {
            instance = new EKBModelGraph();
        }
        return instance;
    }

    private EKBModelGraph() {
        graph = new OGraphDatabase("memory:ekbgraphdb").create();
        graph.createVertexType("Models");
        descriptions = new HashMap<String, TransformationDescription>();
        counter = new AtomicLong(0L);
    }

    /**
     * Adds a model to the graph database and defines it as an active model.
     */
    public void addModel(ModelDescription model) {
        ODocument node = graph.createVertex("Models");
        node.field(OGraphDatabase.LABEL, model.toString());
        node.field(ACTIVE_FIELD, "true");
        node.save();
        LOGGER.debug("Added model {} to the graph database", model);
    }

    /**
     * Removes a model from the graph database by defining it as an inactive model.
     */
    public void removeModel(ModelDescription model) {
        ODocument node = getModel(model.toString());
        if (node == null) {
            LOGGER.warn("Couldn't remove model {} since it wasn't present in the graph database", model);
            return;
        }
        node.field(ACTIVE_FIELD, "false");
        node.save();
        LOGGER.debug("Removed model {} from the graph database", model);
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
     * Adds a transformation description to the graph database. It also adds the models which are the source and the
     * target of the description, if they are not yet added, as inactive models.
     */
    public void addTransformation(TransformationDescription description) {
        checkTransformationDescriptionId(description);
        ODocument source = getOrCreateModel(description.getSourceModel().toString());
        ODocument target = getOrCreateModel(description.getTargetModel().toString());
        ODocument edge = graph.createEdge(source, target);
        edge.field(OGraphDatabase.LABEL, description.getId());
        if (description.getFileName() != null) {
            edge.field(FILENAME, description.getFileName());
        }
        edge.save();
        descriptions.put(description.getId(), description);
        LOGGER.debug("Added transformation description {} to the graph database", description);
    }

    /**
     * Removes a transformation description from the graph database. If the given description has no defined id, then
     * all transformation descriptions in the given edge are deleted, which have ids which were automatically added.
     */
    public void removeTransformation(TransformationDescription description) {
        String source = description.getSourceModel().toString();
        String target = description.getTargetModel().toString();
        for (ODocument edge : getEdgesBetweenModels(source, target)) {
            String id = (String) edge.field(OGraphDatabase.LABEL);
            if (description.getId() == null && isInternalId(id)) {
                edge.delete();
                descriptions.remove(id);
                LOGGER.debug("Removed transformation description {} to the graph database", id);
            } else if (id.equals(description.getId())) {
                edge.delete();
                descriptions.remove(id);
                LOGGER.debug("Removed transformation description {} to the graph database", id);
                break;
            }
        }
    }

    /**
     * Returns all transformation descriptions which were added by the file with the given filename.
     */
    public List<TransformationDescription> getTransformationsPerFileName(String filename) {
        String query = String.format("select from E where %s = ?", FILENAME);
        List<ODocument> edges = graph.query(new OSQLSynchQuery<ODocument>(query), filename);
        List<TransformationDescription> result = new ArrayList<TransformationDescription>();
        for (ODocument edge : edges) {
            result.add(descriptions.get((String) edge.field(FILENAME)));
        }
        return result;
    }

    /**
     * Returns a possible transformation path, beginning at the source model type and ending with the target model type
     * where all given transformation description ids appear in the path.
     */
    public List<TransformationDescription> getTransformationPath(ModelDescription source, ModelDescription target,
            List<String> ids) {
        ODocument edge;
        if (ids == null || ids.isEmpty()) {
            edge = getEdgeBetweenModels(source.toString(), target.toString(), null);
        } else {
            edge = getEdgeBetweenModels(source.toString(), target.toString(), ids.get(0));
        }
        List<TransformationDescription> result = new ArrayList<TransformationDescription>();
        if (edge != null) {
            result.add(descriptions.get((String) edge.field(OGraphDatabase.LABEL)));
            return result;
        }
        throw new IllegalArgumentException("no transformation description found");
    }

    /**
     * Returns true if there is a transformation path, beginning at the source model type and ending with the target
     * model type where all given transformation description ids appear in the path. Returns false if not.
     */
    public Boolean isTransformationPossible(ModelDescription source, ModelDescription target, List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return getEdgeBetweenModels(source.toString(), target.toString(), null) != null;
        } else {
            return getEdgeBetweenModels(source.toString(), target.toString(), ids.get(0)) != null;
        }
    }

    /**
     * Returns the first edge which is found between the source model and the target model. If an id is given, then the
     * edge with the given id is returned, if this edge exists.
     */
    private ODocument getEdgeBetweenModels(String source, String target, String id) {
        ODocument from = getModel(source);
        ODocument to = getModel(target);
        String query = "select from E where out = ? AND in = ?";
        Object[] args;
        if (id != null) {
            query = String.format("%s AND %s = ?", query, OGraphDatabase.LABEL);
            args = new Object[]{ from, to, id };
        } else {
            args = new Object[]{ from, to };
        }
        List<ODocument> result = graph.query(new OSQLSynchQuery<ODocument>(query), args);
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
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
    @SuppressWarnings("unused")
    private List<ODocument> getNeighborsOfModel(String model) {
        ODocument from = getModel(model);
        List<ODocument> edges = graph.query(new OSQLSynchQuery<ODocument>("select from E where out = ?"), from);
        List<ODocument> result = new ArrayList<ODocument>();
        for (ODocument edge : edges) {
            ODocument vertex = graph.getInVertex(edge);
            if (!result.contains(vertex)) {
                result.add(vertex);
            }
        }
        return result;
    }

    /**
     * Returns the model with the given name, or creates one if it isn't existing until then and returns the new one.
     */
    private ODocument getOrCreateModel(String model) {
        ODocument node = getModel(model);
        if (node == null) {
            node = graph.createVertex("Models");
            node.field(OGraphDatabase.LABEL, model.toString());
            node.field(ACTIVE_FIELD, "false");
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
}
