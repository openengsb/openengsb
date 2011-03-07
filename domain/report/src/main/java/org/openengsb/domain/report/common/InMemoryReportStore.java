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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.domain.report.model.Report;

public class InMemoryReportStore implements ReportStore {

    private Map<String, Map<String, Report>> reports = new HashMap<String, Map<String, Report>>();

    @Override
    public List<Report> getAllReports(String category) {
        Map<String, Report> categoryReports = reports.get(category);
        if (categoryReports == null) {
            return new ArrayList<Report>();
        }
        return new ArrayList<Report>(categoryReports.values());
    }

    @Override
    public void storeReport(String category, Report report) {
        Map<String, Report> categoryReports = getOrCreateCategory(category);
        categoryReports.put(report.getName(), report);
    }

    @Override
    public void removeReport(String category, Report report) {
        Map<String, Report> categoryReports = reports.get(category);
        if (categoryReports == null) {
            return;
        }
        categoryReports.remove(report.getName());
    }

    @Override
    public List<String> getAllCategories() {
        return new ArrayList<String>(reports.keySet());
    }

    @Override
    public void removeCategory(String category) {
        reports.remove(category);
    }

    @Override
    public void createCategory(String category) {
        reports.put(category, new HashMap<String, Report>());
    }

    private Map<String, Report> getOrCreateCategory(String category) {
        Map<String, Report> map = reports.get(category);
        if (map == null) {
            map = new HashMap<String, Report>();
            reports.put(category, map);
        }
        return map;
    }

}
