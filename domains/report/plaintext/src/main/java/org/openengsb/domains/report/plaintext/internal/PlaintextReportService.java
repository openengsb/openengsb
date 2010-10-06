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

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.openengsb.core.common.Event;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.domains.report.IdType;
import org.openengsb.domains.report.NoSuchReportException;
import org.openengsb.domains.report.common.AbstractReportDomain;
import org.openengsb.domains.report.model.Report;
import org.openengsb.domains.report.model.ReportPart;

public class PlaintextReportService extends AbstractReportDomain {

    private static final String CONTENT_TYPE = "text/plain";

    private ReportStorageRegistry registry;

    private ReportPartStore partStore;

    private String id;

    public PlaintextReportService(String id) {
        this.id = id;
    }

    @Override
    public Report generateReport(String reportId, String category, String reportName) throws NoSuchReportException {
        StorageKey storageKey = registry.getKeyFor(reportId);
        Report report = doGenerateReport(reportName, storageKey);
        registry.stopStoringDataFor(storageKey);
        partStore.clearParts(storageKey);
        storeReport(category, report);
        return report;
    }

    private Report doGenerateReport(String reportName, StorageKey storageKey) {
        List<ReportPart> parts = partStore.getParts(storageKey);
        Report report = new Report(reportName);
        report.setParts(parts);
        return report;
    }

    @Override
    public Report getDraft(String reportId, String draftName) throws NoSuchReportException {
        return doGenerateReport(draftName, registry.getKeyFor(reportId));
    }

    @Override
    public String collectData(IdType idType, String id) {
        String reportId = UUID.randomUUID().toString();
        StorageKey storageKey = new StorageKey(reportId, idType, id);
        registry.storeDataFor(storageKey);
        return reportId;
    }

    @Override
    public void addReportPart(String reportId, ReportPart reportPart) throws NoSuchReportException {
        checkContentType(reportPart);
        StorageKey storageKey = registry.getKeyFor(reportId);
        partStore.storePart(storageKey, reportPart);
    }

    private void checkContentType(ReportPart reportPart) {
        String contentType = reportPart.getContentType();
        if (!contentType.equals(CONTENT_TYPE)) {
            throw new IllegalArgumentException(
                    "Plaintext report service does not support report parts with content type '" + contentType
                            + "'. It only supports content type '" + CONTENT_TYPE + "'.");
        }
    }

    @Override
    public void processEvent(Event event) {
        StringReportPart reportPart = new StringReportPart(generatePartName(event), CONTENT_TYPE, getContent(event));
        for (IdType type : IdType.values()) {
            String id = getId(type, event);
            if (id != null) {
                storeForAllKeys(reportPart, registry.getStorageKeysFor(type, id));
            }
        }
    }

    private void storeForAllKeys(StringReportPart reportPart, Set<StorageKey> storageKeys) {
        for (StorageKey key : storageKeys) {
            partStore.storePart(key, reportPart);
        }
    }

    private String getId(IdType type, Event event) {
        switch (type) {
            case CONTEXT_ID:
                return event.getContextId();
            case CORRELATION_ID:
            case WORKFLOW_ID:
            case WORKFLOW_INSTANCE_ID:
                return null;
        }
        return null;
    }

    private String getContent(Event event) {
        StringBuilder content = new StringBuilder();
        appendAll(content, "Event class: ", event.getClass().getName(), "\n");
        appendAll(content, "Event context: ", event.getContextId(), "\n");
        appendAll(content, "Event Fields: \n");
        for (Field field : event.getClass().getDeclaredFields()) {
            appendAll(content, field.getName(), ": ", getFieldValue(field, event), "\n");
        }
        return content.toString();
    }

    private String getFieldValue(Field field, Event event) {
        Object value = null;
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            value = field.get(event);
            field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            value = "[WARNING - can not access field value]";
        }
        return String.valueOf(value);
    }

    private void appendAll(StringBuilder builder, Object... objects) {
        for (Object o : objects) {
            builder.append(o);
        }
    }

    private String generatePartName(Event e) {
        return e.getClass().getName() + " - " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    public void setRegistry(ReportStorageRegistry registry) {
        this.registry = registry;
    }

    public void setPartStore(ReportPartStore partStore) {
        this.partStore = partStore;
    }

    public String getId() {
        return id;
    }

}
