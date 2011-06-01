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

package org.openengsb.core.api;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openengsb.core.api.model.BeanDescription;

public class BeanDescriptionTest {

    @Test
    public void testWrapSimpleBean_shouldContainStringData() throws Exception {
        SimpleTestBean bean = new SimpleTestBean("42", 42L);
        BeanDescription beanDescription = BeanDescription.fromObject(bean);
        assertThat(beanDescription.getData().get("stringValue"), is("42"));
        assertThat(beanDescription.getData().get("longValue"), is("42"));
    }

    @Test
    public void testWrapAndUnWrapSimpleBean_shouldBeEqualObject() throws Exception {
        SimpleTestBean bean = new SimpleTestBean("42", 42L);
        BeanDescription beanDescription = BeanDescription.fromObject(bean);
        SimpleTestBean bean2 = beanDescription.toObject(SimpleTestBean.class);

        assertThat(bean2.longValue, is(42L));
        assertThat(bean2.stringValue, is("42"));
    }

}
