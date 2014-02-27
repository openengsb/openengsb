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

import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edbi.jdbc.api.SchemaMapper;
import org.openengsb.core.edbi.jdbc.api.TableEngine;
import org.openengsb.core.edbi.jdbc.sql.Table;

/**
 * Facades {@link org.openengsb.core.edbi.jdbc.HeadTableEngine} and
 * {@link org.openengsb.core.edbi.jdbc.HistoryTableEngine}.
 */
public class DefaultSchemaMapper implements SchemaMapper {

    private TableEngine headTableEngine;
    private TableEngine historyTableEngine;

    public DefaultSchemaMapper(TableEngine headTableEngine, TableEngine historyTableEngine) {
        this.headTableEngine = headTableEngine;
        this.historyTableEngine = historyTableEngine;
    }

    @Override
    public boolean exists(JdbcIndex<?> index) {
        boolean headExists = headTableEngine.exists(index);
        boolean histExists = historyTableEngine.exists(index);

        if (headExists ^ histExists) {
            throw new IllegalStateException("Index is inconsitent. Head: " + headExists + ", history: " + histExists);
        }

        return headExists;
    }

    @Override
    public void create(JdbcIndex<?> index) {
        // TODO: handle inconsistency
        if (exists(index)) {
            return;
        }

        Table headTable = headTableEngine.create(index);
        Table histTable = historyTableEngine.create(index);

        index.setHeadTableName(headTable.getName());
        index.setHistoryTableName(histTable.getName());
    }

    @Override
    public void drop(JdbcIndex<?> index) {
        if (!exists(index)) {
            return;
        }

        // TODO
        // headTableEngine.drop(index);
        // historyTableEngine.drop(index);
    }

    @Override
    public void merge(EDBCommit commit) {
        // TODO
    }

}
