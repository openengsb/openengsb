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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.edbi.api.UnavailableTypeInformationException;
import org.openengsb.core.edbi.models.TestModel;

public class JdbcIndexFieldTest {

    @Test
    public void getType_withDefaultClassLoader_returnsType() throws Exception {
        JdbcIndexField<?> field = new JdbcIndexField<>();

        field.setTypeName("org.openengsb.core.edbi.models.TestModel");

        assertEquals(TestModel.class, field.getType());
    }

    @Test(expected = UnavailableTypeInformationException.class)
    public void getType_withDefaultClassLoader_withUnknownType_throwsException() throws Exception {
        JdbcIndexField<?> field = new JdbcIndexField<>();

        field.setTypeName("org.openengsb.core.edbi.models.TestModelThatDoesNotExist");
        field.getType();
    }

    @Test
    public void getType_withCustomClassLoaderThatKnowsClass_returnsType() throws Exception {
        ClassLoader loader = mock(ClassLoader.class);
        final Class<?> clazz = TestModel.class;

        when(loader.loadClass("org.openengsb.core.edbi.models.TestModel")).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return clazz;
            }
        });

        JdbcIndexField<?> field = new JdbcIndexField<>();
        field.setTypeName("org.openengsb.core.edbi.models.TestModel");
        field.setClassLoader(loader);

        assertEquals(clazz, field.getType());
    }

}
