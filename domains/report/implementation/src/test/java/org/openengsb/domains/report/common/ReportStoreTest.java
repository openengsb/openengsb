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

package org.openengsb.domains.report.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.domains.report.model.Report;
import org.openengsb.domains.report.model.SimpleReportPart;

public abstract class ReportStoreTest {

    private ReportStore reportDomain;

    public abstract ReportStore getReportStore();

    @Before
    public void setUp() {
        this.reportDomain = getReportStore();
        reportDomain.createCategory("42");
        reportDomain.storeReport("42", new Report("test"));
        reportDomain.storeReport("42", new Report("test1"));
        reportDomain.storeReport("42", new Report("test2"));
    }

    @Test
    public void createCategory_shouldWork() {
        assertThat(reportDomain.getAllCategories().contains("testCategory"), is(false));
        reportDomain.createCategory("testCategory");
        assertThat(reportDomain.getAllCategories().contains("testCategory"), is(true));
    }

    @Test
    public void createCategoryTwice_shouldPerformNoOpOnSecondCreation() {
        assertThat(reportDomain.getAllCategories().contains("testCategory"), is(false));
        reportDomain.createCategory("testCategory");
        List<String> categories = reportDomain.getAllCategories();
        reportDomain.createCategory("testCategory");
        assertThat(reportDomain.getAllCategories(), is(categories));
    }

    @Test
    public void removeCategory_shouldWork() {
        assertThat(reportDomain.getAllCategories().contains("42"), is(true));
        this.reportDomain.removeCategory("42");
        assertThat(reportDomain.getAllCategories().contains("42"), is(false));
    }

    @Test
    public void removeCategoryTwice_shouldPerformNoOpOnSecondRemoval() {
        this.reportDomain.removeCategory("42");
        assertThat(reportDomain.getAllCategories().contains("42"), is(false));
        this.reportDomain.removeCategory("42");
    }

    @Test
    public void getAllCategoriesWithoutCategories_shouldReturnEmptyList() {
        reportDomain.removeCategory("42");
        assertThat(reportDomain.getAllCategories().isEmpty(), is(true));
    }

    @Test
    public void getAllCategoriesWithCategories_shouldReturnAllCategories() {
        this.reportDomain.createCategory("testCategory");
        this.reportDomain.createCategory("testCategory2");
        this.reportDomain.createCategory("testCategory3");
        List<String> categories = reportDomain.getAllCategories();
        assertThat(categories.size(), is(4));
        assertThat(categories.contains("42"), is(true));
        assertThat(categories.contains("testCategory"), is(true));
        assertThat(categories.contains("testCategory2"), is(true));
        assertThat(categories.contains("testCategory3"), is(true));
    }

    @Test
    public void storeReportNewCategory_shouldCreateCategoryAndStoreReport() {
        Report report = new Report("testReport");
        this.reportDomain.storeReport("testCategory", report);
        assertThat(reportDomain.getAllReports("testCategory").get(0).getName(), is("testReport"));
    }

    @Test
    public void storeReportExistingCategory_shouldStoreReport() {
        this.reportDomain.createCategory("testCategory");
        Report report = new Report("testReport");
        this.reportDomain.storeReport("testCategory", report);
        assertThat(reportDomain.getAllReports("testCategory").get(0).getName(), is("testReport"));
    }

    @Test
    public void storeReportTwice_shouldOverwriteReport() {
        Report report = new Report("testReport");
        this.reportDomain.storeReport("testCategory", report);
        report = new Report("testReport");
        report.addPart(new SimpleReportPart("somePart", null, null));
        this.reportDomain.storeReport("testCategory", report);
        assertThat(reportDomain.getAllReports("testCategory").get(0).getParts().size(), is(1));
    }

    @Test
    public void removeReport_shouldWork() {
        Report report = new Report("testReport");
        this.reportDomain.storeReport("testCategory", report);
        this.reportDomain.removeReport("testCategory", report);
        assertThat(reportDomain.getAllReports("testCategory").isEmpty(), is(true));

    }

    @Test
    public void removeReportTwice_shouldPerformNoOPOnSecondRemoval() {
        Report report = new Report("testReport");
        this.reportDomain.storeReport("testCategory", report);
        this.reportDomain.removeReport("testCategory", report);
        assertThat(reportDomain.getAllReports("testCategory").isEmpty(), is(true));
        this.reportDomain.removeReport("testCategory", report);
    }

    @Test
    public void getAllReportsCategoryDoesNotExist_shouldReturnEmptyList() {
        List<Report> allReports = this.reportDomain.getAllReports("foo");
        assertThat(allReports.isEmpty(), is(true));
    }

    @Test
    public void getAllReportsEmptyCategory_shouldReturnEmptyList() {
        reportDomain.createCategory("foo");
        List<Report> allReports = reportDomain.getAllReports("foo");
        assertThat(allReports.isEmpty(), is(true));
    }

    @Test
    public void getAllReports_shouldReturnAllReports() {
        this.reportDomain.storeReport("testCategory", new Report("testReport"));
        this.reportDomain.storeReport("testCategory", new Report("testReport1"));
        this.reportDomain.storeReport("testCategory", new Report("testReport2"));
        List<Report> allReports = reportDomain.getAllReports("testCategory");
        assertThat(allReports.size(), is(3));
    }

}
