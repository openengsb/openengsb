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
package org.openengsb.core.edbi.jdbc.driver;

import javax.sql.DataSource;

import org.openengsb.core.edbi.jdbc.api.TypeMap;

/**
 * DBMS Driver for the EDBI. Provides everything database specific to bootstrap a JdbcIndexEngine.
 */
public interface Driver {

    /**
     * Returns the data source for this dbms.
     * 
     * @return the data source used
     */
    DataSource getDataSource();

    /**
     * Returns the type map of the dbms.
     * 
     * @return the type map
     */
    TypeMap getTypeMap();
}
