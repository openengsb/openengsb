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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.edbi.api.UnavailableTypeInformationException;
import org.openengsb.core.edbi.models.TestModel;

/**
 * JdbcIndexTest
 */
public class JdbcIndexTest {

    @Test
    public void getModelClass_withSetClass_returnsClass() throws Exception {
        JdbcIndex<TestModel> model = new JdbcIndex<>();
        model.setModelClass(TestModel.class);

        assertEquals(TestModel.class, model.getModelClass());
    }

    @Test
    public void getModelClass_withUnsetClass_loadsAndReturnsClass() throws Exception {
        JdbcIndex<TestModel> index = new JdbcIndex<>();
        index.setName(TestModel.class.getCanonicalName());

        assertEquals(TestModel.class, index.getModelClass());
    }

    @Test
    public void getModelClass_withSetClassLoader_loadsAndReturnsClass() throws Exception {
        JdbcIndex<?> index = new JdbcIndex<>();
        final Class<?> clazz = TestModel.class;
        String className = clazz.getCanonicalName();

        ClassLoader cl = mock(ClassLoader.class);

        index.setClassLoader(cl);
        index.setName(className);

        when(cl.loadClass(className)).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return clazz;
            }
        });

        assertEquals(TestModel.class, index.getModelClass());

        verify(cl, times(1)).loadClass(className);
    }

    @Test(expected = UnavailableTypeInformationException.class)
    public void getModelClass_withSetClassLoader_thatCanNotLoadClass_throwsException() throws Exception {
        JdbcIndex<?> model = new JdbcIndex<>();

        Class<?> clazz = TestModel.class;
        String className = clazz.getCanonicalName();

        ClassLoader cl = mock(ClassLoader.class);

        model.setClassLoader(cl);
        model.setName(className);

        when(cl.loadClass(className)).thenThrow(new ClassNotFoundException());

        model.getModelClass();
    }
}
