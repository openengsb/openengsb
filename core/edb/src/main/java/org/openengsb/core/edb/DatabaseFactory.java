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

package org.openengsb.core.edb;

import org.openengsb.core.edb.internal.JPADatabase;

/**
 * The Database Factory will be used to create a Database object connected to the desired database.
 */
public class DatabaseFactory {
    public static final String DEFAULT_HOST = "localhost";

    protected DatabaseFactory() {
    }

    /**
     * Currently creates the default database using createDefaultDatabase
     * 
     * @return A Database object connected to the default database.
     */
    public static Database create() {
        return createDefaultDatabase();
    }

    /**
     * Currently creates the default database connecting to a local server with mongo.
     * 
     * @return A Database object connected to the default database.
     */
    public static Database createDefaultDatabase() {
        return new JPADatabase();
    }

    public static Database createJPA() {
        return new JPADatabase();
    }
}
