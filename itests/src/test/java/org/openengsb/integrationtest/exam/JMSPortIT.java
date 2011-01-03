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

package org.openengsb.integrationtest.exam;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.OutgoingPort;
import org.openengsb.core.common.internal.CallRouterImpl;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

@RunWith(JUnit4TestRunner.class)
public class JMSPortIT extends AbstractExamTestHelper {

    @Test
    public void JMSPort_shouldBeExportedWithCorrectId() {
        OutgoingPort serviceWithId =
            OsgiServiceUtils.getServiceWithId(getBundleContext(), OutgoingPort.class, "jms-json");
        System.out.println("ServiceID:" + serviceWithId);
        assertNotNull(serviceWithId);
    }

    @Test
    public void instantiateCallRouter_shouldCallPort() throws URISyntaxException {
        BundleContext bundleContext2 = this.getBundleContext();
        OutgoingPort mock2 = mock(OutgoingPort.class);
        Dictionary<String, String> hashMap = new Hashtable<String, String>();
        hashMap.put("id", "jms-json");
        bundleContext2.registerService(OutgoingPort.class.getName(), mock2, hashMap);
        CallRouterImpl router = new CallRouterImpl();
        router.setBundleContext(bundleContext2);
        URI destination = new URI("123", "456", "789");
        MethodCall call = new MethodCall();
        router.call("jms-json", destination, call);
        verify(mock2).send(destination, call);
    }
}
