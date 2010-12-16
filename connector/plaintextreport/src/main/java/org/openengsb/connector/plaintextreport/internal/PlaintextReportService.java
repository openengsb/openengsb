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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.openengsb.core.common.AliveState;
import org.openengsb.core.common.Event;
import org.openengsb.domain.report.NoSuchReportException;
import org.openengsb.domain.report.common.AbstractReportDomain;
import org.openengsb.domain.report.model.Report;
import org.openengsb.domain.report.model.ReportPart;

public class PlaintextReportService extends AbstractReportDomain {

    private static final String CONTENT_TYPE = "text/plain";

    private Set<String> activeReportDataCollections = new HashSet<String>();

    private ReportPartStore partStore = new InMemoryReportPartStore();

    public PlaintextReportService(String id) {
        super(id);
    }

    @Override
    public Report generateReport(String reportId, String category, String reportName) throws NoSuchReportException {
        checkId(reportId);
        Report report = doGenerateReport(reportName, reportId);
        activeReportDataCollections.remove(reportId);
        partStore.clearParts(reportId);
        storeReport(category, report);
        return report;
    }

    @Override
    public Report getDraft(String reportId, String draftName) throws NoSuchReportException {
        checkId(reportId);
        return doGenerateReport(draftName, reportId);
    }

    @Override
    public String collectData() {
        String reportId = UUID.randomUUID().toString();
        activeReportDataCollections.add(reportId);
        return reportId;
    }

    @Override
    public void addReportPart(String reportId, ReportPart reportPart) throws NoSuchReportException {
        checkContentType(reportPart);
        checkId(reportId);
        partStore.storePart(reportId, reportPart);
    }

    @Override
    public void processEvent(String reportId, Event event) throws NoSuchReportException {
        checkId(reportId);
        StringReportPart reportPart = new StringReportPart(generatePartName(event), CONTENT_TYPE, getContent(event));
        partStore.storePart(reportId, reportPart);
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    private void checkContentType(ReportPart reportPart) {
        String contentType = reportPart.getContentType();
        if (!contentType.equals(CONTENT_TYPE)) {
            throw new IllegalArgumentException(
                "Plaintext report service does not support report parts with content type '" + contentType
                        + "'. It only supports content type '" + CONTENT_TYPE + "'.");
        }
    }

    private void checkId(String reportId) throws NoSuchReportException {
        if (activeReportDataCollections.contains(reportId)) {
            return;
        }
        throw new NoSuchReportException("Currently no report is generated for reportId: " + reportId);
    }

    private Report doGenerateReport(String reportName, String reportId) {
        List<ReportPart> parts = partStore.getParts(reportId);
        Report report = new Report(reportName);
        report.setParts(parts);
        return report;
    }

    private String getContent(Event event) {
        StringBuilder content = new StringBuilder();
        appendAll(content, "Event class: ", event.getClass().getName(), "\n");
        appendAll(content, "Event Fields: \n");
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(event.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor p : propertyDescriptors) {
                try {
                    String name = p.getName();
                    Object value = p.getReadMethod().invoke(event);
                    appendAll(content, "  ", name, ": ", value, "\n");
                } catch (IllegalAccessException e) {
                    appendAll(content, e);
                } catch (InvocationTargetException e) {
                    appendAll(content, e);
                }
            }
        } catch (IntrospectionException e) {
            appendAll(content, e);
        }
        return content.toString();
    }

    private void appendAll(StringBuilder builder, Object... objects) {
        for (Object o : objects) {
            builder.append(o);
        }
    }

    private String generatePartName(Event e) {
        return e.getClass().getName() + " - " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
    }

}
