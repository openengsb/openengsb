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

/**
 * An SQL data type.
 */
public class DataType implements Cloneable {

    public static final int UNKNOWN = Integer.MIN_VALUE;

    public static final int DEFAULT_SCALE = 0;

    /**
     * SQL type constant of {@code java.sql.Types}
     */
    private int type = UNKNOWN;

    /**
     * The database-specific name of the type.
     */
    private String name;

    /**
     * The scale of the type, if any.
     */
    private int scale;

    public DataType(int type) {
        this(type, null);
    }

    public DataType(int type, int scale) {
        this(type, null, scale);
    }

    public DataType(String name) {
        this(UNKNOWN, name);
    }

    public DataType(String name, int scale) {
        this(UNKNOWN, name, scale);
    }

    public DataType(int type, String name) {
        this(type, name, DEFAULT_SCALE);
    }

    public DataType(int type, String name, int scale) {
        this.type = type;
        this.name = name;
        this.scale = scale;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataType dataType = (DataType) o;

        if (scale != dataType.scale) {
            return false;
        } else if (type != dataType.type) {
            return false;
        } else if (name == null) {
            if (dataType.name != null) {
                return false;
            }
        } else if (!name.equals(dataType.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + scale;
        return result;
    }

    @Override
    protected DataType clone() throws CloneNotSupportedException {
        return new DataType(type, name, scale);
    }
}
