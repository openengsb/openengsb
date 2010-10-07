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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openengsb.domains.report.model.Report;
import org.openengsb.domains.report.model.ReportPart;
import org.openengsb.domains.report.model.SimpleReportPart;

public class FileSystemReportStore implements ReportStore {

    private File rootDirectory;

    public FileSystemReportStore(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs();
        } else if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("Root directory '" + rootDirectory + "' is not a directory.");
        }
    }

    @Override
    public List<Report> getAllReports(String category) {
        List<Report> result = new ArrayList<Report>();
        File categoryFile = new File(rootDirectory, category);
        if (!categoryFile.exists()) {
            return result;
        }
        for (File reportFile : categoryFile.listFiles()) {
            result.add(loadReport(reportFile));
        }
        return result;
    }

    private Report loadReport(File reportFile) {
        Report report = new Report(reportFile.getName());
        for (File partFile : reportFile.listFiles()) {
            if (!partFile.getName().endsWith(".meta")) {
                report.addPart(loadPart(partFile));
            }
        }
        return report;
    }

    private ReportPart loadPart(File partFile) {
        try {
            String partName = partFile.getName();
            int partIndex = Integer.parseInt(partName.split("[.]")[0]);
            Properties metadata = readMetadata(new File(partFile.getParentFile(), partIndex + ".meta"));
            byte[] content = FileUtils.readFileToByteArray(partFile);
            return new SimpleReportPart(metadata.getProperty("partName"), metadata.getProperty("contentType"), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Properties readMetadata(File file) throws IOException {
        Properties properties = new Properties();
        if (!file.exists()) {
            properties.put("partName", "unknown");
            properties.put("contentType", "text/plain");
            return properties;
        }
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            properties.load(reader);
            return properties;
        } finally {
            IOUtils.closeQuietly(reader);
        }

    }

    @Override
    public void storeReport(String category, Report report) {
        try {
            createCategory(category);
            File categoryFile = new File(rootDirectory, category);
            File reportFile = new File(categoryFile, report.getName());
            testAndDelete(reportFile);
            reportFile.mkdirs();
            List<ReportPart> parts = report.getParts();
            for (int i = 0; i < parts.size(); i++) {
                ReportPart part = parts.get(i);
                File partFile = new File(reportFile, i + getFileEnding(part.getContentType()));
                writeMetadata(reportFile, i, part);
                FileUtils.touch(partFile);
                if (part.getContent() != null) {
                    FileUtils.writeByteArrayToFile(partFile, part.getContent());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void testAndDelete(File reportFile) throws IOException {
        if (reportFile.exists()) {
            FileUtils.deleteDirectory(reportFile);
        }
    }

    private void writeMetadata(File reportFile, int i, ReportPart part) throws IOException {
        FileWriter fw = null;
        try {
            File partMetaFile = new File(reportFile, i + ".meta");
            Properties properties = new Properties();
            properties.put("partName", part.getPartName());
            properties.put("contentType", part.getContentType());
            fw = new FileWriter(partMetaFile);
            properties.store(fw, "");
        } finally {
            IOUtils.closeQuietly(fw);
        }
    }

    private String getFileEnding(String contentType) {
        if ("text/plain".equals(contentType)) {
            return ".txt";
        } else if ("text/xml".equals(contentType)) {
            return ".xml";
        }
        return ".data";
    }

    @Override
    public void removeReport(String category, Report report) {
        File categoryFile = new File(rootDirectory, category);
        if (!categoryFile.exists()) {
            return;
        }
        File reportFile = new File(categoryFile, report.getName());
        if (!reportFile.exists()) {
            return;
        }
        try {
            FileUtils.deleteDirectory(reportFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAllCategories() {
        List<String> result = new ArrayList<String>();
        File[] files = rootDirectory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                result.add(file.getName());
            }
        }
        return result;
    }

    @Override
    public void removeCategory(String category) {
        try {
            File categoryFile = new File(rootDirectory, category);
            if (categoryFile.exists()) {
                FileUtils.deleteDirectory(categoryFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createCategory(String category) {
        File categoryFile = new File(rootDirectory, category);
        if (categoryFile.exists()) {
            return;
        }
        categoryFile.mkdirs();
    }

}
