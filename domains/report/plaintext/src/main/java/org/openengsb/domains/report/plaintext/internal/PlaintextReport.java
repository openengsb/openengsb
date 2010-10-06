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

import org.openengsb.core.common.Event;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.domains.report.IdType;
import org.openengsb.domains.report.NoSuchReportException;
import org.openengsb.domains.report.common.AbstractReportDomain;
import org.openengsb.domains.report.model.Report;
import org.openengsb.domains.report.model.ReportPart;

public class PlaintextReport extends AbstractReportDomain {

    @Override
    public Report generateReport(String reportId, String category, String reportName) throws NoSuchReportException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Report getDraft(String reportId, String draftName) throws NoSuchReportException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String collectData(IdType idType, String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addReportPart(String reportId, ReportPart reportPart) throws NoSuchReportException {
        // TODO Auto-generated method stub

    }

    @Override
    public void processEvent(Event e) {
        // TODO Auto-generated method stub

    }

    @Override
    public AliveState getAliveState() {
        // TODO Auto-generated method stub
        return null;
    }

}
