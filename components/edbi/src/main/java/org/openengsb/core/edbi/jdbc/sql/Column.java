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

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Represents a column of a Table. It consists of a name (title) and a data type name. As both are represented only by
 * String, they should satisfy valid SQL names depending on the underlying SQL implementation they are used for.
 */
public class Column implements TableElement {

    /**
     * A column option.
     */
    public static enum Option {
        NULL, NOT_NULL, AUTO_INCREMENT
    }

    /**
     * The name of the column
     */
    private String name;

    /**
     * The data type of the column
     */
    private DataType type;

    /**
     * The {@link org.openengsb.core.edbi.jdbc.sql.Column.Option} flags for this column
     */
    private EnumSet<Option> options;

    /**
     * Creates a new Column instance with the given name and type name.
     * 
     * @param name the name of the column
     * @param type the data type of the column
     */
    public Column(String name, DataType type) {
        this(name, type, EnumSet.noneOf(Option.class));
    }

    /**
     * Creates a new Column instance with the given name, type name and options.
     * 
     * @param name the name of the column
     * @param type the data type of the column
     * @param options column options
     */
    public Column(String name, DataType type, Option... options) {
        this(name, type, EnumSet.copyOf(Arrays.asList(options)));
    }

    /**
     * Creates a new Column instance with the given name, type and options.
     * 
     * @param name the name of the column
     * @param type the data type of the column
     * @param options column options
     */
    public Column(String name, DataType type, EnumSet<Option> options) {
        this.name = name;
        this.type = type;
        this.options = options;
    }

    /**
     * Sets the given option for this column. Equal to {@code set(Option, true)}.
     * 
     * @param option the option to set
     * @return this for chaining
     */
    public Column set(Option option) {
        return set(option, true);
    }

    /**
     * Unsets the given option for this column. Equal to {@code set(Option, false)}.
     * 
     * @param option the option to unset
     * @return this for chaining
     */
    public Column unset(Option option) {
        return set(option, false);
    }

    /**
     * Sets or unsets the given option for this column according to the set flag given.
     * 
     * @param option the option to flag
     * @param set whether to set or unset the option
     * @return this for chaining
     */
    public Column set(Option option, boolean set) {
        if (set) {
            options.add(option);
        } else {
            options.remove(option);
        }
        return this;
    }

    /**
     * Sets the designated name of the column.
     * 
     * @param name the name of the column
     * @return this for chaining
     */
    public Column setName(String name) {
        this.name = name;
        return this;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public boolean hasOption(Option option) {
        return options.contains(option);
    }

    public EnumSet<Option> getOptions() {
        return options.clone();
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    @Override
    public void accept(TableElementVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("[%s : %s %s]", name, type, options);
    }

    @Override
    public int hashCode() {
        if (name == null) {
            return super.hashCode();
        } else {
            return name.hashCode();
        }
    }

}
