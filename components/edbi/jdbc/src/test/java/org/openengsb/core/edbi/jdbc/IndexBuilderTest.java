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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.edbi.api.IndexField;
import org.openengsb.core.edbi.jdbc.names.ClassNameIndexTranslator;
import org.openengsb.core.edbi.models.SubTestModel;
import org.openengsb.core.edbi.models.TestModel;

/**
 * IndexBuilderTest
 */
public class IndexBuilderTest {

    IndexBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new IndexBuilder(new ClassNameIndexTranslator());
    }

    @After
    public void tearDown() throws Exception {
        builder = null;
    }

    @Test
    public void buildIndex_works() throws Exception {
        JdbcIndex<TestModel> index = builder.buildIndex(TestModel.class);

        assertEquals(TestModel.class.getCanonicalName(), index.getName());
        assertEquals(3, index.getFields().size());

        IndexField[] fields = index.getFields().toArray(new IndexField[3]);

        assertEquals("subModel", fields[0].getName());
        assertEquals(SubTestModel.class, fields[0].getType());

        assertEquals("testInteger", fields[1].getName());
        assertEquals(Integer.class, fields[1].getType());

        assertEquals("testId", fields[2].getName());
        assertEquals(String.class, fields[2].getType());
    }
}
