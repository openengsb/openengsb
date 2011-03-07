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

package org.openengsb.domain.report.common;

import java.util.List;

import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.report.model.Report;

public abstract class AbstractReportDomain extends AbstractOpenEngSBService implements ReportDomain {

    private ReportStore store;

    public AbstractReportDomain(String instanceId) {
        super(instanceId);
    }

    @Override
    public List<Report> getAllReports(String category) {
        return store.getAllReports(category);
    }

    @Override
    public void storeReport(String category, Report report) {
        store.storeReport(category, report);
    }

    @Override
    public void removeReport(String category, Report report) {
        store.removeReport(category, report);
    }

    @Override
    public List<String> getAllCategories() {
        return store.getAllCategories();
    }

    @Override
    public void removeCategory(String category) {
        store.removeCategory(category);
    }

    @Override
    public void createCategory(String category) {
        store.createCategory(category);
    }

    public void setStore(ReportStore store) {
        this.store = store;
    }

    public ReportStore getStore() {
        return store;
    }

}
