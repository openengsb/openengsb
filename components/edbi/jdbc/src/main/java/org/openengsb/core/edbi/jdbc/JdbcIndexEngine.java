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
package org.openengsb.core.edbi.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.edbi.api.ClassNameTranslator;
import org.openengsb.core.edbi.api.EDBIndexException;
import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.edbi.api.IndexEngine;
import org.openengsb.core.edbi.api.IndexExistsException;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.IndexNotFoundException;
import org.openengsb.core.edbi.jdbc.api.SchemaMapper;
import org.openengsb.core.edbi.jdbc.driver.h2.SchemaCreateCommand;
import org.openengsb.core.edbi.jdbc.names.ClassNameIndexTranslator;
import org.openengsb.core.edbi.jdbc.operation.DeleteOperation;
import org.openengsb.core.edbi.jdbc.operation.InsertOperation;
import org.openengsb.core.edbi.jdbc.operation.UpdateOperation;
import org.openengsb.core.edbi.jdbc.sql.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

/**
 * IndexEngine implementation that uses JDBC as underlying persistence method. Manages Index objects and their
 * respective tables and data using a SchemaMapper.
 */
public class JdbcIndexEngine extends JdbcService implements IndexEngine {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcIndexEngine.class);

    private Map<String, JdbcIndex<?>> registry;

    private ClassNameTranslator translator;
    private SchemaMapper schemaMapper;

    public JdbcIndexEngine(DataSource dataSource, SchemaMapper schemaMapper) {
        super(dataSource);

        this.translator = new ClassNameIndexTranslator();
        this.registry = new HashMap<>();
        this.schemaMapper = schemaMapper;
    }

    @Override
    public <T> Index<T> createIndex(Class<T> model) throws IndexExistsException {
        LOG.info("Creating Index for Class {}", model);

        if (indexExists(model)) {
            throw new IndexExistsException("Index for model " + model.getSimpleName() + " already exists");
        }

        // build index skeleton
        JdbcIndex<T> index = new IndexBuilder(translator).buildIndex(model);

        // create schema (history and head tables in underlying db) for index and map tables
        schemaMapper.create(index);

        // remove any fields that might not have valid type information
        removeUnmappedFields(index);

        // store index meta data
        persist(index);

        registry.put(index.getName(), index);

        return index;
    }

    @Override
    public boolean indexExists(Class<?> model) {
        return indexExists(translator.translate(model));
    }

    @Override
    public boolean indexExists(String name) {
        return registry.containsKey(name) || existsInDb(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> JdbcIndex<T> getIndex(Class<T> model) throws IndexNotFoundException {
        if (!indexExists(model)) {
            throw new IndexNotFoundException("Index for model " + model.getSimpleName() + " does not exist");
        }
        String name = translator.translate(model);

        if (registry.containsKey(name)) {
            JdbcIndex cached = registry.get(name);
            if (!cached.hasTypeInformation()) {
                cached.setModelClass(model);
            }
            return cached;
        }

        JdbcIndex<T> index = load(model);
        registry.put(name, index);
        return index;
    }

    @Override
    public JdbcIndex<?> getIndex(String name) throws IndexNotFoundException {
        if (!indexExists(name)) {
            throw new IndexNotFoundException("Index " + name + " does not exist");
        }
        if (registry.containsKey(name)) {
            return registry.get(name);
        }

        JdbcIndex<?> index = load(name);

        registry.put(name, index);

        return index;
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        throw new UnsupportedOperationException("Custom ClassLoaders not yet supported");
    }

    @Override
    public void commit(IndexCommit commit) throws EDBIndexException {
        // TODO: transactional!
        LOG.info("Committing, id: {}", commit.getCommitId());

        Set<Class<?>> modelClasses = commit.getModelClasses();

        if (modelClasses == null) {
            throw new IllegalArgumentException("Commit has no model class information");
        }

        LOG.debug("Checking if index exists for classes {}", modelClasses);
        for (Class<?> modelClass : modelClasses) {
            if (!indexExists(modelClass)) {
                createIndex(modelClass);
            }
        }

        LOG.debug("Executing operations");
        for (Class<?> modelClass : modelClasses) {
            JdbcIndex<?> index = getIndex(modelClass);

            List<OpenEngSBModel> inserts = commit.getInserts().get(modelClass);
            if (!isEmpty(inserts)) {
                schemaMapper.execute(new InsertOperation(commit, index, inserts));
            }

            List<OpenEngSBModel> updates = commit.getUpdates().get(modelClass);
            if (!isEmpty(updates)) {
                schemaMapper.execute(new UpdateOperation(commit, index, updates));
            }

            List<OpenEngSBModel> deletes = commit.getDeletes().get(modelClass);
            if (!isEmpty(deletes)) {
                schemaMapper.execute(new DeleteOperation(commit, index, deletes));
            }
        }
    }

    /**
     * Creates the necessary relations to save Index and IndexField instances.
     */
    public void install() {
        new SchemaCreateCommand(getDataSource()).execute(); // TODO: sql independence
    }

    protected synchronized boolean existsInDb(String name) {
        return count("INDEX_INFORMATION", "NAME = ?", name) > 0;
    }

    /**
     * Remove fields from the index that have no mapped type information.
     * 
     * @param index the index to be pruned
     */
    protected void removeUnmappedFields(JdbcIndex<?> index) {
        Iterator<IndexField<?>> iterator = index.getFields().iterator();

        while (iterator.hasNext()) {
            IndexField<?> field = iterator.next();

            if (field.getMappedType() == null) {
                LOG.info("Removing {} from index {} - no mapped type information", field.getName(), index.getName());
                iterator.remove();
            }
        }

    }

    protected synchronized void persist(JdbcIndex<?> index) throws IndexExistsException {
        LOG.info("Persisting Index {}", index.getName());

        if (existsInDb(index.getName())) {
            throw new IndexExistsException("Index " + index.getName() + " already exists");
        }

        String sql = "INSERT INTO `INDEX_INFORMATION` VALUES (?, ?, ?, ?)";
        Object[] args = new Object[]{
            index.getName(),
            index.getModelClass().getCanonicalName(),
            index.getHeadTableName(),
            index.getHistoryTableName()
        };

        jdbc().update(sql, args);
        persistFields(index);
    }

    protected void persistFields(final JdbcIndex<?> index) {
        String sql = "INSERT INTO `INDEX_FIELD_INFORMATION` VALUES (?, ?, ?, ?, ?, ?, ?)";
        Collection<IndexField<?>> fields = index.getFields();

        jdbc().batchUpdate(sql, fields, fields.size(), new ParameterizedPreparedStatementSetter<IndexField<?>>() {
            @Override
            public void setValues(PreparedStatement ps, IndexField<?> field) throws SQLException {
                ps.setObject(1, index.getName());
                ps.setObject(2, field.getName());
                ps.setObject(3, field.getType().getCanonicalName());
                ps.setObject(4, field.getMappedName());

                DataType type = (DataType) field.getMappedType();
                ps.setObject(5, type.getType());
                ps.setObject(6, type.getName());
                ps.setObject(7, type.getScale());
            }
        });
    }

    protected JdbcIndex<?> load(final String name) {
        return load(name, null);
    }

    protected <T> JdbcIndex<T> load(final Class<T> modelClass) {
        return load(translator.translate(modelClass), modelClass);
    }

    protected <T> JdbcIndex<T> load(final String name, final Class<T> modelClass) {
        LOG.info("Loading Index {} (with class {})", name, modelClass);

        String sql = "SELECT TABLE_HEAD, TABLE_HISTORY FROM INDEX_INFORMATION WHERE NAME = ?";

        try {
            return jdbc().queryForObject(sql, new RowMapper<JdbcIndex<T>>() {
                @Override
                public JdbcIndex<T> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JdbcIndex<T> index = new JdbcIndex<>();

                    index.setName(name);
                    index.setModelClass(modelClass);

                    index.setHeadTableName(rs.getString("TABLE_HEAD"));
                    index.setHistoryTableName(rs.getString("TABLE_HISTORY"));

                    index.setFields(loadFields(index));

                    return index;
                }
            }, name);
        } catch (EmptyResultDataAccessException e) {
            throw new IndexNotFoundException("Index " + name + " was not found", e);
        }
    }

    protected List<JdbcIndexField<?>> loadFields(final JdbcIndex<?> index) {
        String sql = "SELECT * FROM INDEX_FIELD_INFORMATION WHERE INDEX_NAME = ?";

        try {
            return jdbc().query(sql, new RowMapper<JdbcIndexField<?>>() {
                @Override
                public JdbcIndexField<?> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    JdbcIndexField<?> field = new JdbcIndexField<>(index);

                    field.setName(rs.getString("NAME"));
                    field.setTypeName(rs.getString("TYPE"));
                    field.setMappedName(rs.getString("MAPPED_NAME"));
                    field.setMappedType(mapDataType(rs));

                    return field;
                }

                private DataType mapDataType(ResultSet rs) throws SQLException {
                    int type = rs.getInt("MAPPED_TYPE");
                    String name = rs.getString("MAPPED_TYPE_NAME");
                    int scale = rs.getInt("MAPPED_TYPE_SCALE");

                    return new DataType(type, name, scale);
                }

            }, index.getName());
        } catch (EmptyResultDataAccessException e) {
            LOG.warn("Could not find any fields for index {}", index.getName());
            return Collections.emptyList();
        }
    }

    private boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
