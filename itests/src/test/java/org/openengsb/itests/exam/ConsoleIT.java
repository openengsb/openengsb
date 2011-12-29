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

import static org.junit.Assert.assertTrue;

import java.io.PrintStream;
import java.util.List;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.util.OutputStreamFormater;
import org.openengsb.domain.auditing.AuditingDomain;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.openengsb.itests.util.OutputStreamHelper;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.ProbeBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

@RunWith(JUnit4TestRunner.class)
// This one will run each test in it's own container (slower speed)
// @ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class ConsoleIT extends AbstractPreConfiguredExamTestHelper {

    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
        probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*;status=provisional");
        return probe;
    }

    @Test
    public void testToExecuteOpenEngSBDomainInfoCommand() throws Exception {
        CommandProcessor cp = getOsgiService(CommandProcessor.class);

        OutputStreamHelper outputStreamHelper = new OutputStreamHelper();
        PrintStream out = new PrintStream(outputStreamHelper);
        CommandSession cs = cp.createSession(System.in, out, System.err);

        Bundle b = getInstalledBundle("org.openengsb.framework.console");
        b.start();
        cs.execute("openengsb:domains");
        cs.close();

        List<String> result = outputStreamHelper.getResult();
        assertTrue(contains(result, "AuditingDomain", "Domain to auditing tools in the OpenEngSB system."));
        assertTrue(contains(result, "Example Domain",
            "This domain is provided as an example for all developers. It should not be used in production."));
    }

    @Test
    public void testToExecuteOpenEngSBServiceListCommand() throws Exception {
        CommandProcessor cp = getOsgiService(CommandProcessor.class);

        OutputStreamHelper outputStreamHelper = new OutputStreamHelper();
        PrintStream out = new PrintStream(outputStreamHelper);
        CommandSession cs = cp.createSession(System.in, out, System.err);

        Bundle b = getInstalledBundle("org.openengsb.framework.console");
        b.start();
        
        waitForDefaultConnectors();
        
        cs.execute("openengsb:service list");
        cs.close();

        List<String> result = outputStreamHelper.getResult();
        for (String string : result) {
            System.out.println(string);
        }
        assertTrue(contains(result, "AuditingDomain", "Domain to auditing tools in the OpenEngSB system."));
        assertTrue(contains(result, "auditing+memoryauditing+auditing-root", "ONLINE"));
        assertTrue(contains(result, "BinaryTransformationDomain",
            "Domain for connectors which want to provide a BinaryTransformationProviderFactory."));
        assertTrue(contains(result, "Example Domain",
            "This domain is provided as an example for all developers. It should not be used in production."));
    }

	private void waitForDefaultConnectors() {
		getOsgiService(AuditingDomain.class);
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
