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

package org.openengsb.domains.report.plaintext.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.domains.report.IdType;
import org.openengsb.domains.report.NoSuchReportException;
import org.openengsb.domains.report.common.ReportStore;
import org.openengsb.domains.report.model.Report;
import org.openengsb.domains.report.model.SimpleReportPart;

public class PlaintextReportServiceTest {

    private PlaintextReportService reportService;
    private ReportStore store;

    @Before
    public void setUp() {
        reportService = new PlaintextReportService("test");
        store = Mockito.mock(ReportStore.class);
        reportService.setStore(store);
    }

    @Test(expected = NoSuchReportException.class)
    public void generateReportUnknownId_shouldFail() throws NoSuchReportException {
        reportService.generateReport("foo", "bar", "buz");
    }

    @Test
    public void generateReport_shouldWork() throws NoSuchReportException {
        String reportId = reportService.collectData(IdType.CONTEXT_ID, "foo");
        Report report = reportService.generateReport(reportId, "foo", "bar");
        assertThat(report.getName(), is("bar"));
        Mockito.verify(store).storeReport("foo", report);
    }

    @Test(expected = NoSuchReportException.class)
    public void generateReportTwice_shouldFail() throws NoSuchReportException {
        String reportId = reportService.collectData(IdType.CONTEXT_ID, "foo");
        reportService.generateReport(reportId, "foo", "bar");
        reportService.generateReport(reportId, "foo", "bar");
    }

    @Test
    public void getDraft_shouldWork() throws NoSuchReportException {
        String reportId = reportService.collectData(IdType.CONTEXT_ID, "foo");
        Report report = reportService.getDraft(reportId, "bar");
        assertThat(report.getName(), is("bar"));
        Mockito.verify(store, Mockito.times(0)).storeReport(Mockito.anyString(), Mockito.any(Report.class));
    }

    @Test
    public void generateDraftTwice_shouldWork() throws NoSuchReportException {
        String reportId = reportService.collectData(IdType.CONTEXT_ID, "foo");
        reportService.getDraft(reportId, "bar");
        reportService.getDraft(reportId, "bar");
    }

    @Test
    public void addReportPart_shouldWork() throws NoSuchReportException {
        String reportId = reportService.collectData(IdType.CONTEXT_ID, "foo");
        reportService.addReportPart(reportId, new SimpleReportPart("bar", "text/plain", null));
        Report report = reportService.generateReport(reportId, "buz", "42");
        assertThat(report.getParts().size(), is(1));
        assertThat(report.getParts().get(0).getPartName(), is("bar"));
    }

    @Test(expected = NoSuchReportException.class)
    public void addReportPartWrongReportId_shouldFail() throws NoSuchReportException {
        reportService.addReportPart("wrongReportId", new SimpleReportPart("bar", "text/plain", null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addReportPartWrongContentType_shouldFail() throws NoSuchReportException {
        String reportId = reportService.collectData(IdType.CONTEXT_ID, "foo");
        reportService.addReportPart(reportId, new SimpleReportPart("bar", "wrongType", null));
    }
}
