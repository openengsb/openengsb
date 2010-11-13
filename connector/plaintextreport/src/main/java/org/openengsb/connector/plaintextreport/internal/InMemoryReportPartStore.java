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

package org.openengsb.connector.plaintextreport.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.domain.report.model.ReportPart;

public class InMemoryReportPartStore implements ReportPartStore {

    private Map<String, List<ReportPart>> reportPartMap = new HashMap<String, List<ReportPart>>();

    @Override
    public List<ReportPart> getParts(String key) {
        List<ReportPart> list = reportPartMap.get(key);
        if (list == null) {
            return Collections.emptyList();
        }
        return new ArrayList<ReportPart>(list);
    }

    @Override
    public void storePart(String key, ReportPart part) {
        List<ReportPart> list = reportPartMap.get(key);
        if (list == null) {
            list = new ArrayList<ReportPart>();
            reportPartMap.put(key, list);
        }
        list.add(part);
    }

    @Override
    public ReportPart getLastPart(String key) {
        List<ReportPart> parts = getParts(key);
        if (parts.isEmpty()) {
            return null;
        }
        return parts.get(parts.size() - 1);
    }

    @Override
    public void clearParts(String key) {
        reportPartMap.remove(key);
    }

}
