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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.domains.report.NoSuchReportException;
import org.openengsb.domains.report.ReportDomain;
import org.openengsb.domains.report.common.FileSystemReportStoreFactory;
import org.openengsb.domains.report.model.Report;
import org.openengsb.domains.report.plaintext.internal.PlaintextReportFactory;
import org.openengsb.domains.report.plaintext.internal.PlaintextReportService;
import org.openengsb.domains.report.plaintext.internal.StringReportPart;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class ReportIT extends AbstractExamTestHelper {
    private ReportDomain reportService;

    @Before
    public void setUp() throws Exception {
        reportService = retrieveService(getBundleContext(), ReportDomain.class);

        ContextCurrentService contextService = retrieveService(getBundleContext(), ContextCurrentService.class);
        contextService.createContext("42");
        contextService.setThreadLocalContext("42");
        contextService.putValue("domains/ReportDomain/defaultConnector/id", "plaintextConnector");

        PlaintextReportFactory factory = new PlaintextReportFactory(new FileSystemReportStoreFactory());
        PlaintextReportService reporter =
            factory.createServiceInstance("plaintextConnector", new HashMap<String, String>());

        String[] clazzes = new String[]{Domain.class.getName(), ReportDomain.class.getName()};
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("id", "plaintextConnector");

        getBundleContext().registerService(clazzes, reporter, properties);
    }

    @Test
    public void testCreateAndRetrieve() throws NoSuchReportException {
        String reportId = reportService.collectData();
        reportService.addReportPart(reportId, new StringReportPart("part1", "text/plain", "foo"));
        reportService.processEvent(reportId, new Event("42"));
        reportService.generateReport(reportId, "bar", "buz");

        Report report = reportService.getAllReports("bar").get(0);

        assertThat(report.getName(), is("buz"));
        assertThat(report.getParts().size(), is(2));
        assertThat(report.getParts().get(0).getPartName(), is("part1"));
        assertThat(report.getParts().get(0).getContentType(), is("text/plain"));
        assertThat(report.getParts().get(0).getContent(), is("foo".getBytes()));

        reportService.removeCategory("bar");
        assertThat(reportService.getAllReports("bar").isEmpty(), is(true));
    }
}
