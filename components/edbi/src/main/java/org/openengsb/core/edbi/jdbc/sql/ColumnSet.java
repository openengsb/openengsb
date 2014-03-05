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
package org.openengsb.core.edbi.jdbc.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * ColumnSet
 */
public class ColumnSet implements Iterable<Column> {

    private Map<String, Column> columns;

    public ColumnSet() {
        columns = new LinkedHashMap<>();
    }

    public ColumnSet(int capacity) {
        columns = new LinkedHashMap<>(capacity);
    }

    public ColumnSet(List<Column> columns) {
        this(columns.size());
        addAll(columns);
    }

    public List<String> getColumnNames() {
        return new ArrayList<>(columns.keySet());
    }

    public Column get(String column) {
        return columns.get(column);
    }

    public int getType(String column) {
        throw new UnsupportedOperationException();
    }

    public String getTypeName(String column) {
        Column c = get(column);

        return (c == null) ? null : c.getType().getName();
    }

    public boolean contains(String column) {
        return columns.containsKey(column);
    }

    public boolean contains(Column column) {
        return columns.containsValue(column);
    }

    public int size() {
        return columns.size();
    }

    public ColumnSet add(Column column) {
        String name = column.getName();

        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Column name may not be null or empty");
        }

        columns.put(name, column);
        return this;
    }

    public ColumnSet addAll(Collection<Column> columns) {
        for (Column column : columns) {
            add(column);
        }
        return this;
    }

    @Override
    public Iterator<Column> iterator() {
        return columns.values().iterator();
    }
}
