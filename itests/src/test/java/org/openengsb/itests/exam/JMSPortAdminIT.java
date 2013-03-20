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

package org.openengsb.itests.exam;

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.OptionUtils.combine;

import org.apache.karaf.tooling.exam.options.configs.FeaturesCfg;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.util.DefaultOsgiUtilsService;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.itests.remoteclient.ExampleConnector;
import org.openengsb.itests.remoteclient.SecureSampleConnector;
import org.openengsb.itests.util.AbstractRemoteTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4TestRunner.class)
public class JMSPortAdminIT extends AbstractRemoteTestHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(JMSPortAdminIT.class);
    private DefaultOsgiUtilsService utilsService;
    private String openwirePort;

    @Configuration
    public Option[] additionalConfiguration() throws Exception {
        return combine(baseConfiguration(),
            editConfigurationFileExtend(FeaturesCfg.BOOT, ",openengsb-ports-jms,openengsb-connector-example"),
            new VMOption("-Dorg.openengsb.jms.noencrypt=true"),
            new VMOption("-Dorg.openengsb.security.noverify=true"));
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        openwirePort = getOpenwirePort();
        additionalJMSSetUp(LOGGER);
        utilsService = getOsgiUtils();
    }

    @Test
    public void testStartAndStopRemoteConnector_shouldRegisterAndUnregisterProxy() throws Exception {
        authenticateAsAdmin();

        // make sure jms is up and running
        utilsService.getServiceWithId(OutgoingPort.class, "jms-json", 60000);

        SecureSampleConnector remoteConnector = new SecureSampleConnector(openwirePort);
        remoteConnector.start(new ExampleConnector(), new ConnectorDescription("example", "external-connector-proxy"));
        ExampleDomain osgiService = getOsgiService(ExampleDomain.class, "(service.pid=example-remote)", 31000);

        assertThat(getBundleContext().getServiceReferences(ExampleDomain.class.getName(),
            "(service.pid=example-remote)"), not(nullValue()));
        assertThat(osgiService, not(nullValue()));

        remoteConnector.getInvocationHistory().clear();
        osgiService.doSomethingWithMessage("test");
        assertThat(remoteConnector.getInvocationHistory().isEmpty(), is(false));

        remoteConnector.stop();
        
        boolean unregistered = false;
        
        for (int i = 0; i < 5; i++) {
            Thread.sleep(1000);
            if (remoteConnectorUnregistered()) {
                unregistered = true;
                break;
            }
        }
        assertThat(unregistered, is(true));
    }
    
    private boolean remoteConnectorUnregistered() throws Exception {
        return getBundleContext().getServiceReferences(ExampleDomain.class.getName(), 
            "(id=example-remote)") == null;
    }
}
