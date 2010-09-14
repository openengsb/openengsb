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

package org.openengsb.report;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.MethodCallHelper;
import org.openengsb.core.model.Event;
import org.openengsb.drools.ReportDomain;
import org.openengsb.drools.model.Report;
import org.openengsb.report.datastore.EventStore;
import org.openengsb.report.datastore.StorageKey;

public class ReportDomainImpl implements ReportDomain {

    private Log log = LogFactory.getLog(getClass());

    private Map<String, StorageKey> toCollect = new HashMap<String, StorageKey>();

    private EventStorageRegistry registry;

    private EventStore eventStore;

    private ReportEndpoint endpoint;

    private MessageProperties msgProperties;

    private ContextHelper contextHelper;

    @Override
    public String collectData(String idType, String id) {
        log.info("Start collection report data for idType: '" + idType + "' and id: '" + id + "'.");
        String reportId = UUID.randomUUID().toString();
        StorageKey storageKey = new StorageKey(reportId, idType, id);
        toCollect.put(reportId, storageKey);
        registry.storeEventsFor(storageKey);
        return reportId;
    }

    @Override
    public Report generateReport(String reportId) {
        log.info("Generate report for reportId: '" + reportId + "'.");
        StorageKey storageKey = toCollect.remove(reportId);
        if (storageKey == null) {
            throw new IllegalArgumentException("No report for the given report id.");
        }
        registry.stopStoringEventsFor(storageKey);
        List<Event> events = eventStore.getEvents(storageKey);
        eventStore.clearEvents(storageKey);
        return generateReport(events);
    }

    @Override
    public Report generateReport(List<Event> events) {
        QName toolConnector = getToolConnectorQName();
        Method method;
        method = getGenerateReportMethod();
        return (Report) MethodCallHelper.sendMethodCall(endpoint, toolConnector, method, new Object[] { events },
                msgProperties);
    }

    private Method getGenerateReportMethod() {
        try {
            return getClass().getMethod("generateReport", List.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private QName getToolConnectorQName() {
        String defaultName = contextHelper.getValue("report/default");
        String serviceName = contextHelper.getValue("report/" + defaultName + "/servicename");
        String namespace = contextHelper.getValue("report/" + defaultName + "/namespace");
        return new QName(namespace, serviceName);
    }

    public void setRegistry(EventStorageRegistry registry) {
        this.registry = registry;
    }

    public void setEventStore(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void setContextHelper(ContextHelper contextHelper) {
        this.contextHelper = contextHelper;
    }

    public void setMessageProperties(MessageProperties msgProperties) {
        this.msgProperties = msgProperties;
    }

    public void setEndpoint(ReportEndpoint endpoint) {
        this.endpoint = endpoint;
    }

}
