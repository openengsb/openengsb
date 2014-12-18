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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.util.OutputStreamFormater;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.openengsb.itests.util.OutputStreamHelper;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@RunWith(PaxExam.class)
public class ConsoleIT extends AbstractPreConfiguredExamTestHelper {
    private static final String FRAMEWORK_VERSION = "framework-version";
    private static final String FRAMEWORK_ELEMENT_COUNT = "framework-element-count";
    private static final String DROOLS_VERSION = "drools-version";
    private static final String KARAF_VERSION = "karaf-version";
    private static final String OSGI_VERSION = "osgi-version";

    private OutputStreamHelper outputStreamHelper;
    private CommandSession cs;
    private CommandProcessor cp;
    private String testServiceId;

    @BeforeClass
    public static void initialize() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
        probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*;status=provisional");
        return probe;
    }

    @Before
    public void setUp() throws Exception {
        if (cp == null) {
            Bundle b = getInstalledBundle("org.openengsb.framework.console");
            b.start();
            cp = getOsgiService(CommandProcessor.class);
        }
        outputStreamHelper = new OutputStreamHelper();
        PrintStream out = new PrintStream(outputStreamHelper);
        cs = cp.createSession(System.in, out, System.err);
    }

    @After
    public void tearDown() throws Exception {
        outputStreamHelper.close();
        cs.close();
    }

    @Test
    public void testToExecuteOpenEngSBInfoCommand_shouldPrintOpenEngSBInformation() throws Exception {
        Map<String, String> info = loadDataForInfoCommand();
        cs.execute("openengsb:info");
        List<String> result = outputStreamHelper.getResult();
        assertTrue(contains(result, "OpenEngSB Framework Version", info.get(FRAMEWORK_VERSION)));
        assertTrue(contains(result, "OpenEngSB Framework Bundles", info.get(FRAMEWORK_ELEMENT_COUNT)));
        assertTrue(contains(result, "Karaf Version", info.get(KARAF_VERSION)));
        assertTrue(contains(result, "OSGi Framework", info.get(OSGI_VERSION)));
        assertTrue(contains(result, "Drools version", info.get(DROOLS_VERSION)));
    }

    @Test
    public void testToExecuteOpenEngSBDomainInfoCommand_shouldPrintInfoAboutDomain() throws Exception {
        waitForOsgiBundle("org.openengsb.domain.example");
        cs.execute("openengsb:domains");
        List<String> result = outputStreamHelper.getResult();
        assertTrue(contains(result, "AuditingDomain", "Domain to auditing tools in the OpenEngSB system."));
        assertTrue(contains(result, "Example Domain",
            "This domain is provided as an example for all developers. It should not be used in production."));
    }

    @Test
    public void testToExecuteOpenEngSBServiceListCommand_shouldListServices() throws Exception {
        cs.execute("openengsb:service list");
        List<String> result = outputStreamHelper.getResult();
        assertTrue(contains(result, "root-authenticator", "ONLINE"));
        assertTrue(contains(result, "auditing-root", "ONLINE"));
        assertTrue(contains(result, "root-authorizer", "ONLINE"));
    }

    @Test
    public void testListCommand_shouldShowPreviouslyAddedService() throws Exception {
        String id = addTestService();
        cs.execute("openengsb:service list");
        List<String> result = outputStreamHelper.getResult();
        assertTrue(contains(result, id, "ONLINE"));
    }

    @Test
    public void testDeleteCommand_serviceShouldNotBeAvailableAfterwards() throws Exception {
        String id = addTestService();
        cs.execute("openengsb:service -f true delete " + id);
        cs.execute("openengsb:service list");
        List<String> result = outputStreamHelper.getResult();
        assertTrue(result.contains(String.format("Service: %s successfully deleted", id)));
        assertFalse(contains(result, id, "ONLINE"));
    }

    @Test
    public void testToExecuteOpenEngSBServiceCreateCommand_shouldCreateService() throws Exception {
        String serviceId = "testID";
        String executeCommand = String.format("openengsb:service -f true create AuditingDomain type:memoryauditing "
                + "service.pid:%s attr:something", serviceId);
        cs.execute(executeCommand);
        cs.execute("openengsb:service list");

        List<String> result = outputStreamHelper.getResult();
        assertTrue(result.contains("Connector successfully created"));
        assertTrue(contains(result, serviceId, "ONLINE"));
    }

    private String addTestService() {
        if (testServiceId != null) {
            return testServiceId;
        }
        ConnectorManager connectorManager = getOsgiService(ConnectorManager.class);
        ConnectorDescription connectorDescription = new ConnectorDescription("authentication", "composite-connector");
        Map<String, String> attributes =
            Maps.newHashMap(ImmutableMap.of("compositeStrategy", "authentication.provider", "queryString",
                "(foo=bar)"));
        connectorDescription.setAttributes(attributes);
        testServiceId = connectorManager.create(connectorDescription);
        return testServiceId;
    }

    private Map<String, String> loadDataForInfoCommand() {
        Map<String, String> data = new HashMap<String, String>();
        Integer count = 0;
        for (Bundle b : getBundleContext().getBundles()) {
            if (b.getSymbolicName().startsWith("org.openengsb.framework")) {
                if (!data.containsKey(FRAMEWORK_VERSION)) {
                    data.put(FRAMEWORK_VERSION, b.getVersion().toString());
                }
                count++;
            } else if (b.getSymbolicName().startsWith("org.drools")) {
                if (!data.containsKey(DROOLS_VERSION)) {
                    data.put(DROOLS_VERSION, b.getVersion().toString());
                }
            }
        }
        data.put(FRAMEWORK_ELEMENT_COUNT, count.toString());
        data.put(OSGI_VERSION, getBundleContext().getBundle(0).getSymbolicName()
                + " - " + getBundleContext().getBundle(0).getVersion());
        data.put(KARAF_VERSION, System.getProperty("karaf.version"));
        return data;
    }

    private boolean contains(List<String> list, String value, String value2) {
        for (String s : list) {
            String s1 = OutputStreamFormater.formatValues(value, value2);
            if (s.contains(s1)) {
                return true;
            }
        }
        return false;
    }
}
