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

import java.io.PrintStream;
import java.util.List;

import org.apache.felix.gogo.commands.CommandException;
import org.apache.felix.gogo.runtime.CommandNotFoundException;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.util.OutputStreamFormater;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.openengsb.itests.util.OutputStreamHelper;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.ProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class BaseConsoleIT extends AbstractPreConfiguredExamTestHelper {

    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
        probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*;status=provisional");
        return probe;
    }

    @Test(expected = CommandNotFoundException.class)
    public void testToExecuteNotInstalledCommand_shouldThrowException() throws Exception {
        CommandProcessor cp = getOsgiService(CommandProcessor.class);
        CommandSession cs = cp.createSession(System.in, System.out, System.err);

        cs.execute("failing-command");
        fail("Exception should be thrown that the command does not exist");
        cs.close();
    }

    @Test(expected = CommandException.class)
    public void testToExecuteACommandWithInvalidArguments_shouldThrowException() throws Exception {
        CommandProcessor cp = getOsgiService(CommandProcessor.class);
        CommandSession cs = cp.createSession(System.in, System.out, System.err);

        Bundle b = getInstalledBundle("org.apache.karaf.shell.log");
        b.start();
        cs.execute("log:display argument1");
        b.stop();
        fail("Exception should be thrown that the argument is not expected");
        cs.close();
    }

    @Test
    public void testToExecuteOpenEngSBInfoCommand() throws Exception {
        CommandProcessor cp = getOsgiService(CommandProcessor.class);

        OutputStreamHelper outputStreamHelper = new OutputStreamHelper();
        PrintStream out = new PrintStream(outputStreamHelper);
        CommandSession cs = cp.createSession(System.in, out, System.err);

        Bundle b = getInstalledBundle("org.openengsb.framework.console");
        b.start();
        cs.execute("openengsb:info");
        b.stop();
        cs.close();

        List<String> result = outputStreamHelper.getResult();
        assertTrue(contains(result, "OpenEngSB Framework Version", ""));
        assertTrue(contains(result, "Karaf Version", ""));
        assertTrue(contains(result, "OSGi Framework", ""));
        assertTrue(contains(result, "Drools version", ""));
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
        b.stop();
        cs.close();

        List<String> result = outputStreamHelper.getResult();
        assertTrue(contains(result, "AuditingDomain", "Domain to auditing tools in the OpenEngSB system."));
        assertTrue(contains(result, "Example Domain",
            "This domain is provided as an example for all developers. It should not be used in production."));
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


