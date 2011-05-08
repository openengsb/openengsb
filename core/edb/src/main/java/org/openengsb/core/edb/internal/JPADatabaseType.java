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

package org.openengsb.core.edb.internal;

import java.util.Properties;

import javax.transaction.NotSupportedException;

/**
 * An enumeration used to define which databases are supported for now by the JPA database connection. Can also return
 * the necessary properties for the database type.
 */
public enum JPADatabaseType {

    H2;

    /**
     * Returns the properties which are necessary for the database connection through JPA.
     */
    public Properties getPropertiesForDatabaseType() throws NotSupportedException {
        switch (this) {
            case H2:
                Properties props = new Properties();
                props.setProperty("openjpa.ConnectionDriverName", "org.h2.Driver");
                props.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
                props.setProperty("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.H2Dictionary");
                props.setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
                return props;
            default:
                throw new NotSupportedException("not implemented for this enum value");
        }
    }

    /**
     * Returns the defined prefix for the connection to the database (example: for H2 -> "jdbc:h2:")
     */
    public String getConnectionPrefix() throws NotSupportedException {
        switch (this) {
            case H2:
                return "jdbc:h2:";
            default:
                throw new NotSupportedException("not implemented for this enum value");
        }
    }
}
