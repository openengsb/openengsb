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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
        File categoryFile = new File(rootDirectory, getCategoryFileName(category));
        if (!categoryFile.exists()) {
            return result;
        }
        for (File reportFile : categoryFile.listFiles()) {
            if (!reportFile.getName().endsWith(".meta")) {
                result.add(loadReport(reportFile));
            }
        }
        return result;
    }

    @Override
    public void storeReport(String category, Report report) {
        try {
            createCategory(category);
            File categoryFile = new File(rootDirectory, getCategoryFileName(category));
            File reportFile = createReportDirectory(report, categoryFile);
            List<ReportPart> parts = report.getParts();
            for (int i = 0; i < parts.size(); i++) {
                storeReportPart(reportFile, i, parts.get(i));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeReport(String category, Report report) {
        File categoryFile = new File(rootDirectory, getCategoryFileName(category));
        if (!categoryFile.exists()) {
            return;
        }
        File reportFile = new File(categoryFile, getReportFileName(report));
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
            if (file.isDirectory() && Arrays.asList(file.list()).contains("category.meta")) {
                result.add(getCategoryName(file));
            }
        }
        return result;
    }

    @Override
    public void removeCategory(String category) {
        try {
            File categoryFile = new File(rootDirectory, getCategoryFileName(category));
            if (categoryFile.exists()) {
                FileUtils.deleteDirectory(categoryFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createCategory(String category) {
        File categoryFile = new File(rootDirectory, getCategoryFileName(category));
        if (categoryFile.exists()) {
            return;
        }
        categoryFile.mkdirs();
        writeCategoryMetadata(category, categoryFile);
    }

    private Report loadReport(File reportFile) {
        try {
            File metaFile = new File(reportFile, "report.meta");
            Properties reportProps = readPropertiesFromFile(metaFile);
            String name = reportProps.getProperty("reportName");
            Report report = new Report(name);
            for (File partFile : getAndSortPartFiles(reportFile)) {
                report.addPart(loadPart(partFile));
            }
            return report;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<File> getAndSortPartFiles(File reportFile) {
        File[] files = reportFile.listFiles();
        List<File> result = new ArrayList<File>(files.length);
        for (File file : files) {
            if (file.getName().endsWith(".meta")) {
                continue;
            }
            result.add(file);
        }
        Collections.sort(result, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                int index1 = Integer.parseInt(name1.split("[.]")[0]);
                int index2 = Integer.parseInt(name2.split("[.]")[0]);
                return index1 < index2 ? -1 : (index1 == index2 ? 0 : 1);
            }
        });
        return result;
    }

    private ReportPart loadPart(File partFile) throws IOException {
        String partName = partFile.getName();
        int partIndex = Integer.parseInt(partName.split("[.]")[0]);
        Properties metadata = readMetadata(new File(partFile.getParentFile(), partIndex + ".meta"));
        byte[] content = FileUtils.readFileToByteArray(partFile);
        return new SimpleReportPart(metadata.getProperty("partName"), metadata.getProperty("contentType"), content);
    }

    private Properties readMetadata(File file) throws IOException {
        if (!file.exists()) {
            return getDefaultProperties();
        }
        return readPropertiesFromFile(file);
    }

    private Properties readPropertiesFromFile(File file) throws IOException {
        FileReader reader = null;
        try {
            Properties properties = new Properties();
            reader = new FileReader(file);
            properties.load(reader);
            return properties;
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private Properties getDefaultProperties() {
        Properties properties = new Properties();
        properties.put("partName", "unknown");
        properties.put("contentType", "text/plain");
        return properties;
    }

    private File createReportDirectory(Report report, File categoryFile) throws IOException {
        String name = getReportFileName(report);
        File reportFile = new File(categoryFile, name);
        testAndDelete(reportFile);
        reportFile.mkdirs();
        File meta = new File(reportFile, "report.meta");
        Properties properties = new Properties();
        properties.setProperty("reportName", report.getName());
        writeProperties(meta, properties);
        return reportFile;
    }

    private void testAndDelete(File reportFile) throws IOException {
        if (reportFile.exists()) {
            FileUtils.deleteDirectory(reportFile);
        }
    }

    private void storeReportPart(File reportFile, int partIndex, ReportPart part) throws IOException {
        File partFile = new File(reportFile, partIndex + getFileEnding(part.getContentType()));
        writeMetadata(reportFile, partIndex, part);
        FileUtils.touch(partFile);
        if (part.getContent() != null) {
            FileUtils.writeByteArrayToFile(partFile, part.getContent());
        }
    }

    private void writeMetadata(File reportFile, int partIndex, ReportPart part) throws IOException {
        File partMetaFile = new File(reportFile, partIndex + ".meta");
        Properties properties = new Properties();
        properties.put("partName", part.getPartName());
        properties.put("contentType", part.getContentType());
        writeProperties(partMetaFile, properties);
    }

    private void writeProperties(File file, Properties properties) throws IOException {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
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

    private String getReportFileName(Report report) {
        return String.valueOf(report.getName().hashCode());
    }

    private String getCategoryFileName(String category) {
        return String.valueOf(category.hashCode());
    }

    private String getCategoryName(File categoryFile) {
        try {
            File meta = new File(categoryFile, "category.meta");
            Properties properties = readPropertiesFromFile(meta);
            return properties.getProperty("categoryName");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeCategoryMetadata(String category, File categoryFile) {
        try {
            File meta = new File(categoryFile, "category.meta");
            Properties properties = new Properties();
            properties.setProperty("categoryName", category);
            writeProperties(meta, properties);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
