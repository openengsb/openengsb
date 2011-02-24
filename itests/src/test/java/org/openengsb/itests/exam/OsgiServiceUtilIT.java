/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.itests.exam;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class OsgiServiceUtilIT extends AbstractExamTestHelper {

    @Test
    public void testOsgiServiceUtilMethods() throws Exception {
        ServiceManager service = OsgiServiceUtils.getService(getBundleContext(), ServiceManager.class);
        assertThat(service, notNullValue());
        service =
            (ServiceManager) OsgiServiceUtils.getService(getBundleContext(),
                OsgiServiceUtils.makeFilter(ServiceManager.class, "(connector=example)"));
        assertThat(service, notNullValue());

        ServiceManager service2 =
            (ServiceManager) OsgiServiceUtils.getService(getBundleContext(), "(connector=example)");
        assertThat(service2.getInstanceId(), is(service.getInstanceId()));
    }

    @Test
    public void testOsgiServiceProxy() throws Exception {
        ServiceManager proxy = OsgiServiceUtils.getOsgiServiceProxy(getBundleContext(),
            OsgiServiceUtils.makeFilter(ServiceManager.class, "(connector=example)"), ServiceManager.class);
        assertThat(proxy.getInstanceId(), is("org.openengsb.connector.example.LogServiceManager"));
    }
}
