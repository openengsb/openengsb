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

package org.openengsb.core.api.edb;

import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;

/**
 * An enumeration used to define which databases are supported for now by the EDB database connection. 
 */
public enum EDBDatabaseType {

    H2;
    
    /**
     * Returns the properties which are necessary for the database connection through JPA.
     */
    public Properties getPropertiesForDatabaseType() throws NotImplementedException {
        switch (this) {
            case H2:
                Properties props = new Properties();
                props.setProperty("openjpa.ConnectionDriverName", "org.h2.Driver");
                props.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
                props.setProperty("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.H2Dictionary");
                props.setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
                return props;
            default:
                throw new NotImplementedException("not implemented for this enum value");
        }
    }

    /**
     * Returns the defined prefix for the connection to the database (example: for H2 -> "jdbc:h2:")
     */
    public String getConnectionPrefix() throws NotImplementedException {
        switch (this) {
            case H2:
                return "jdbc:h2:";
            default:
                throw new NotImplementedException("not implemented for this enum value");
        }
    }
}
