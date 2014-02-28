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
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edbi.api.ClassNameTranslator;
import org.openengsb.core.edbi.api.EDBIndexException;
import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexEngine;
import org.openengsb.core.edbi.api.IndexExistsException;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.api.IndexNotFoundException;
import org.openengsb.core.edbi.jdbc.api.SchemaMapper;
import org.openengsb.core.edbi.jdbc.names.ClassNameIndexTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

/**
 * JdbcIndexEngine
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
        if (indexExists(model)) {
            throw new IndexExistsException("Index for model " + model.getSimpleName() + " already exists");
        }

        // build index skeleton
        JdbcIndex<T> index = new IndexBuilder(translator).buildIndex(model);

        // create schema (history and head tables in underlying db) for index and map tables
        schemaMapper.create(index);

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
    public <T> Index<T> getIndex(Class<T> model) throws IndexNotFoundException {
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
    public Index<?> getIndex(String name) throws IndexNotFoundException {
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
    public void merge(EDBCommit commit) throws EDBIndexException {
        // each model in the EDB commit should already have an index mapped to it
        // resolve commit and build an EDBICommit, pass it to the schema mapper
        // schemaMapper.merge(commit);
    }

    protected synchronized boolean existsInDb(String name) {
        return count("INDEX_INFORMATION", "NAME = ?", name) > 0;
    }

    protected synchronized void persist(JdbcIndex<?> index) throws IndexExistsException {
        if (existsInDb(index.getName())) {
            throw new IndexExistsException("Index " + index.getName() + " already exists");
        }

        String sql = "INSERT INTO `INDEX_INFORMATION` VALUES (?, ?, ?, ?)";
        Object[] args = new Object[]{
            index.getName(),
            index.getClass().getCanonicalName(),
            index.getHeadTableName(),
            index.getHistoryTableName()
        };

        jdbc().update(sql, args);
        persistFields(index);
    }

    protected void persistFields(final JdbcIndex<?> index) {
        String sql = "INSERT INTO `INDEX_FIELD_INFORMATION` VALUES (?, ?, ?, ?, ?)";
        Collection<IndexField<?>> fields = index.getFields();

        jdbc().batchUpdate(sql, fields, fields.size(), new ParameterizedPreparedStatementSetter<IndexField<?>>() {
            @Override
            public void setValues(PreparedStatement ps, IndexField<?> field) throws SQLException {
                ps.setObject(1, index.getName());
                ps.setObject(2, field.getName());
                ps.setObject(3, field.getType().getCanonicalName());
                ps.setObject(4, field.getMappedName());
                ps.setObject(5, field.getMappedType());
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
                    field.setMappedType(rs.getString("MAPPED_TYPE"));

                    return field;
                }
            }, index.getName());
        } catch (EmptyResultDataAccessException e) {
            LOG.warn("Could not find any fields for index " + index.getName());
            return Collections.emptyList();
        }
    }
}
