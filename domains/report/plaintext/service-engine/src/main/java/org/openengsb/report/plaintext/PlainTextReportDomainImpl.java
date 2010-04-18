/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.report.plaintext;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.MethodCallHelper;
import org.openengsb.core.model.Event;
import org.openengsb.drools.ReportDomain;
import org.openengsb.drools.model.Attachment;
import org.openengsb.drools.model.Report;

public class PlainTextReportDomainImpl implements ReportDomain {

    private ContextHelper contextHelper;

    private PlaintextReportEndpoint endpoint;

    private MessageProperties msgProperties;

    private File reportDirectory;

    public PlainTextReportDomainImpl(ContextHelper contextHelper, PlaintextReportEndpoint endpoint,
            MessageProperties msgProperties, File reportDirectory) {
        this.contextHelper = contextHelper;
        this.endpoint = endpoint;
        this.msgProperties = msgProperties;
        this.reportDirectory = reportDirectory;
    }

    @Override
    public String collectData(String idType, String id) {
        Method m = getMethod("collectData", String.class, String.class);
        QName domainService = getDomainQName();
        return (String) MethodCallHelper.sendMethodCall(endpoint, domainService, m, new Object[] { idType, id },
                msgProperties);
    }

    private QName getDomainQName() {
        String serviceName = contextHelper.getValue("report/servicename");
        String namespace = contextHelper.getValue("report/namespace");
        return new QName(namespace, serviceName);
    }

    @Override
    public Report generateReport(String reportId) {
        Method m = getMethod("generateReport", String.class);
        QName domainService = getDomainQName();
        return (Report) MethodCallHelper.sendMethodCall(endpoint, domainService, m, new Object[] { reportId },
                msgProperties);
    }

    @Override
    public Report generateReport(List<Event> events) {
        reportDirectory.mkdirs();
        File reportFile = new File(reportDirectory, getReportName());
        writeToFile(events, reportFile);
        List<Attachment> attachments = new ArrayList<Attachment>();
        byte[] data = getBytes(events, attachments);
        Report report = new Report(data, "text/plain", getReportName());
        report.setAttachments(attachments);
        return report;
    }

    private byte[] getBytes(List<Event> events, List<Attachment> attachments) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        try {
            writeEvents(writer, events, attachments);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    private void writeEvents(Writer writer, List<Event> events, List<Attachment> attachments) throws IOException {
        writer.append("\n\n-----------------------\n");
        writer.append(new Date().toString());
        writer.append("\n");
        for (Event e : events) {
            writeEventIntro(writer, e);
            for (String key : e.getKeys()) {
                writeKeyValuePair(writer, attachments, e, key);
            }
            writeEventTail(writer);
        }
        writer.append("\n");
    }

    private void writeEventIntro(Writer writer, Event e) throws IOException {
        writer.append("---------event---------\n");
        writer.append("name: ");
        writer.append(e.getName());
        writer.append(" / domain: ");
        writer.append(e.getDomain());
        writer.append(" / toolconnector: ");
        writer.append(String.valueOf(e.getToolConnector()));
        writer.append("\n");
    }

    private void writeKeyValuePair(Writer writer, List<Attachment> attachments, Event e, String key) throws IOException {
        if (e.getValue(key) instanceof byte[]) {
            addAttachment(key, (byte[]) e.getValue(key), (String) e.getValue(key + ".type"), attachments);
        } else {
            writer.append(key);
            writer.append(": ");
            writer.append(String.valueOf(e.getValue(key)));
            writer.append("\n");
        }
    }

    private void writeEventTail(Writer writer) throws IOException {
        writer.append("\n");
        writer.append("\n");
    }

    private void addAttachment(String name, byte[] data, String type, List<Attachment> attachments) {
        String actualType = type;
        if (actualType == null) {
            actualType = "text/plain";
        }
        attachments.add(new Attachment(data, actualType, name));
    }

    private void writeToFile(List<Event> events, File reportFile) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(reportFile, true));
            List<Attachment> attachments = new ArrayList<Attachment>();
            writeEvents(writer, events, attachments);
            writeAttachmentsToFile(reportFile, attachments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private void writeAttachmentsToFile(File reportFile, List<Attachment> attachments) throws IOException {
        for (Attachment attachment : attachments) {
            FileUtils
                    .writeByteArrayToFile(new File(reportFile.getParent(), attachment.getName()), attachment.getData());
        }
    }

    private String getReportName() {
        Date current = new Date();
        DateFormat format = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        return format.format(current) + ".txt";
    }

    private Method getMethod(String name, Class<?>... parameterTypes) {
        try {
            return getClass().getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
