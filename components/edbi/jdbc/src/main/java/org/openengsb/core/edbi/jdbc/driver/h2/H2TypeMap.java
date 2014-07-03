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
package org.openengsb.core.edbi.jdbc.driver.h2;

import static java.sql.Types.BIGINT;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.DATE;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.LONGNVARCHAR;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIME;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.VARCHAR;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;

import org.openengsb.core.edbi.jdbc.driver.AbstractTypeMap;

/**
 * TypeMap for the h2 database.
 */
public class H2TypeMap extends AbstractTypeMap {

    @Override
    protected void initMap() {
        put(Integer.class, INTEGER, "INTEGER");
        put(int.class, INTEGER, "INTEGER");
        put(Long.class, BIGINT, "BIGINT");
        put(long.class, BIGINT, "BIGINT");
        put(Boolean.class, BOOLEAN, "BOOLEAN");
        put(boolean.class, BOOLEAN, "BOOLEAN");
        put(Double.class, DOUBLE, "DOUBLE");
        put(double.class, DOUBLE, "DOUBLE");
        put(Float.class, FLOAT, "REAL");
        put(float.class, FLOAT, "REAL");
        put(Short.class, SMALLINT, "SMALLINT");
        put(short.class, SMALLINT, "SMALLINT");

        put(String.class, LONGNVARCHAR, "LONGVARCHAR");

        put(java.util.Date.class, TIMESTAMP, "DATETIME");
        put(UUID.class, VARCHAR, "UUID");

        put(Date.class, DATE, "DATE");
        put(Time.class, TIME, "TIME");
        put(Timestamp.class, TIMESTAMP, "TIMESTAMP");
    }

}
