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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Types;

import org.junit.Test;

public class DataTypeTest {

    @Test
    public void equals_onSameInstance_returnsTrue() throws Exception {
        DataType varchar = new DataType(Types.VARCHAR, "VARCHAR", 255);
        assertTrue(varchar.equals(varchar));
    }

    @Test
    public void equals_withDifferentScale_returnsFalse() throws Exception {
        DataType varchar = new DataType(Types.VARCHAR, "VARCHAR", 255);
        DataType other = new DataType(Types.VARCHAR, "VARCHAR", 50);

        assertFalse(varchar.equals(other));
    }

    @Test
    public void equals_withUnknownScale_returnsTrue() throws Exception {
        DataType varchar = new DataType(Types.VARCHAR, "VARCHAR");
        DataType other = new DataType(Types.VARCHAR, "VARCHAR");

        assertTrue(varchar.equals(other));
    }

    @Test
    public void equals_withUnsetTypeName_returnsTrue() throws Exception {
        DataType varchar = new DataType(Types.VARCHAR);
        DataType other = new DataType(Types.VARCHAR);

        assertTrue(varchar.equals(other));
    }

    @Test
    public void equals_withUnknownType_returnsTrue() throws Exception {
        DataType varchar = new DataType("VARCHAR");
        DataType other = new DataType("VARCHAR");

        assertTrue(varchar.equals(other));
    }

    @Test
    public void equals_withUnknownType_disparateTypeName_returnsFalse() throws Exception {
        DataType varchar = new DataType("VARCHAR");
        DataType other = new DataType("INTEGER");

        assertFalse(varchar.equals(other));
    }

    @Test
    public void equals_withDisparateType_returnsFalse() throws Exception {
        DataType varchar = new DataType(Types.VARCHAR);
        DataType other = new DataType(Types.INTEGER);

        assertFalse(varchar.equals(other));
    }
}
