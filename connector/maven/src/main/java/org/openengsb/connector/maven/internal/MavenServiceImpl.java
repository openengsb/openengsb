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

package org.openengsb.connector.maven.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.domain.build.BuildDomainEvents;
import org.openengsb.domain.build.BuildFailEvent;
import org.openengsb.domain.build.BuildStartEvent;
import org.openengsb.domain.build.BuildSuccessEvent;
import org.openengsb.domain.deploy.DeployDomainEvents;
import org.openengsb.domain.deploy.DeployFailEvent;
import org.openengsb.domain.deploy.DeployStartEvent;
import org.openengsb.domain.deploy.DeploySuccessEvent;
import org.openengsb.domain.test.TestDomainEvents;
import org.openengsb.domain.test.TestFailEvent;
import org.openengsb.domain.test.TestStartEvent;
import org.openengsb.domain.test.TestSuccessEvent;

public class MavenServiceImpl extends AbstractOpenEngSBService implements MavenDomain {

    private String mvnVersion = readMvnVersionFromPropFile();
    private String mvnCommand = new File(System.getProperty("karaf.data")).getAbsolutePath() + "/apache-maven-"
            + mvnVersion + "/bin/mvn" + addSystemEnding();
    private static final int MAX_LOG_FILES = 5;

    private Log log = LogFactory.getLog(this.getClass());

    private String projectPath;

    private BuildDomainEvents buildEvents;
    private TestDomainEvents testEvents;
    private DeployDomainEvents deployEvents;

    private Executor executor = Executors.newSingleThreadExecutor();
    private ExecutorService outputReaderPool = Executors.newCachedThreadPool();

    private boolean synchronous = false;

    private boolean useLogFile = true;

    private ContextCurrentService contextService;

    private String command;
    private File logDir;
    
    public MavenServiceImpl(String id) {
        super(id);
        String karafData = System.getProperty("karaf.data");
        logDir = new File(karafData, "log");
        if (!logDir.exists()) {
            logDir.mkdir();
        } else if (!logDir.isDirectory()) {
            throw new IllegalStateException("cannot access log-directory");
        }
        
        if (!isMavenInstalled()) {
            try {
                installMaven();
            } catch (Exception e) {
                log.error(e);
            }
        }
        new File(mvnCommand).setExecutable(true);
    }

    public String readMvnVersionFromPropFile() {        
        Properties prop = new Properties();
        String version = null;
        try {
            prop.load(ClassLoader.getSystemResourceAsStream("config.properties"));
            version = prop.getProperty("mvnVersion");
        } catch (IOException e) {
        }
        return version;
    }

    private List<String> getListOfMirrors() {
        Properties prop = new Properties();
        List<String> mirrorList = new ArrayList<String>();
        try {
            prop.load(ClassLoader.getSystemResourceAsStream("config.properties"));

            int i = 1;
            while (prop.getProperty("mirror" + i) != null) {
                mirrorList.add(prop.getProperty("mirror" + i));
                i++;
            }
        } catch (IOException e) {
        }
        return mirrorList;
    }

    public Boolean isMavenInstalled() {
        if (new File(System.getProperty("karaf.data") + "/apache-maven-" + readMvnVersionFromPropFile()).exists()) {
            return true;
        }
        return false;

    }

    public boolean download(String url, String downloadPath) {
        InputStream in = null;
        try {
            in = new URL(url).openStream();
            FileUtils.writeByteArrayToFile(new File(downloadPath), IOUtils.toByteArray(in));
            return true;
        } catch (IOException e) {
            log.error(e);
            return false;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public void installMaven() throws Exception {
        File tmp = File.createTempFile("mvn_setup", "zip");
        List<?> mirrors = getListOfMirrors();
        for (int i = 0; i < mirrors.size(); i++) {
            if (download(String.valueOf(mirrors.get(i)) + "apache-maven-" + readMvnVersionFromPropFile() + "-bin.zip",
                    tmp.getAbsolutePath())) {
                break;
            }

            if (i == mirrors.size() - 1) {
                log.error("No valid mirror found!");
            }
        }
        if (mirrors.size() != 0) {
            unzipFile(tmp.getAbsolutePath(), System.getProperty("karaf.data"));
        }
    }

    public void unzipFile(String archivePath, String targetPath) throws Exception {
        try {
            File archiveFile = new File(archivePath);
            File targetFile = new File(targetPath);
            ZipFile zipFile = new ZipFile(archiveFile);
            Enumeration<?> e = zipFile.getEntries();
            while (e.hasMoreElements()) {
                ZipArchiveEntry zipEntry = (ZipArchiveEntry) e.nextElement();
                File file = new File(targetFile, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    FileUtils.forceMkdir(file);
                } else {
                    InputStream is = zipFile.getInputStream(zipEntry);
                    FileOutputStream os = FileUtils.openOutputStream(file);
                    try {
                        IOUtils.copy(is, os);
                    } finally {
                        os.close();
                        is.close();
                    }
                }
            }
        } catch (ZipException e) {
            throw new Exception("Unzipping failed");
        } catch (IOException e) {
            throw new Exception();
        }
    }

    
    private static String addSystemEnding() {
        if (System.getProperty("os.name").contains("Windows")) {
            return ".bat";
        }
        return "";
    }

    void setProjectPath(String projectPath) {
        this.projectPath = projectPath.replaceAll("%20", " ");
    }

    @Override
    public AliveState getAliveState() {
        if (validate()) {
            return AliveState.ONLINE;
        } else {
            return AliveState.OFFLINE;
        }
    }

    @Override
    public String runTests() {
        final String id = createId();
        final String contextId = contextService.getThreadLocalContext();
        Runnable runTests = new Runnable() {

            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                testEvents.raiseEvent(new TestStartEvent(id));
                if (result.isSuccess()) {
                    testEvents.raiseEvent(new TestSuccessEvent(id, result.getOutput()));
                } else {
                    testEvents.raiseEvent(new TestFailEvent(id, result.getOutput()));
                }
            }
        };
        execute(runTests);
        return id;
    }

    @Override
    public void runTests(final long processId) {
        final String contextId = contextService.getThreadLocalContext();
        Runnable runTests = new Runnable() {
            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                testEvents.raiseEvent(new TestStartEvent(processId));
                if (result.isSuccess()) {
                    testEvents.raiseEvent(new TestSuccessEvent(processId, result.getOutput()));
                } else {
                    testEvents.raiseEvent(new TestFailEvent(processId, result.getOutput()));
                }
            }
        };
        execute(runTests);
    }

    @Override
    public String build() {
        final String id = createId();
        final String contextId = contextService.getThreadLocalContext();
        Runnable doBuild = new Runnable() {
            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                buildEvents.raiseEvent(new BuildStartEvent(id));
                if (result.isSuccess()) {
                    buildEvents.raiseEvent(new BuildSuccessEvent(id, result.getOutput()));
                } else {
                    buildEvents.raiseEvent(new BuildFailEvent(id, result.getOutput()));
                }
            }
        };
        execute(doBuild);
        return id;
    }

    @Override
    public void build(final long processId) {
        final String contextId = contextService.getThreadLocalContext();
        Runnable doBuild = new Runnable() {
            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                BuildStartEvent buildStartEvent = new BuildStartEvent();
                buildStartEvent.setProcessId(processId);
                buildEvents.raiseEvent(buildStartEvent);
                if (result.isSuccess()) {
                    buildEvents.raiseEvent(new BuildSuccessEvent(processId, result.getOutput()));
                } else {
                    buildEvents.raiseEvent(new BuildFailEvent(processId, result.getOutput()));
                }
            }
        };
        execute(doBuild);

    }

    private void execute(Runnable runnable) {
        if (synchronous) {
            runnable.run();
        } else {
            executor.execute(runnable);
        }
    }

    @Override
    public String deploy() {
        final String id = createId();
        final String contextId = contextService.getThreadLocalContext();
        Runnable doDeploy = new Runnable() {

            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                deployEvents.raiseEvent(new DeployStartEvent(id));
                if (result.isSuccess()) {
                    deployEvents.raiseEvent(new DeploySuccessEvent(id, result.getOutput()));
                } else {
                    deployEvents.raiseEvent(new DeployFailEvent(id, result.getOutput()));
                }
            }
        };
        execute(doDeploy);
        return id;
    }

    @Override
    public void deploy(final long processId) {
        final String contextId = contextService.getThreadLocalContext();
        Runnable doDeploy = new Runnable() {
            @Override
            public void run() {
                contextService.setThreadLocalContext(contextId);
                MavenResult result = excuteCommand(command);
                deployEvents.raiseEvent(new DeployStartEvent(processId));
                if (result.isSuccess()) {
                    deployEvents.raiseEvent(new DeploySuccessEvent(processId, result.getOutput()));
                } else {
                    deployEvents.raiseEvent(new DeployFailEvent(processId, result.getOutput()));
                }

            }
        };
        execute(doDeploy);
    }

    private String createId() {
        return UUID.randomUUID().toString();
    }

    public Boolean validate() {
        return excuteCommand("validate").isSuccess();
    }

    private synchronized MavenResult excuteCommand(String goal) {
        File dir = new File(projectPath);

        List<String> command = new ArrayList<String>();
        command.add(mvnCommand);
        command.addAll(Arrays.asList(goal.trim().split(" ")));

        try {
            return runMaven(dir, command);
        } catch (IOException e) {
            log.error(e);
            return new MavenResult(false, e.getMessage());
        } catch (InterruptedException e) {
            log.error(e);
            return new MavenResult(false, e.getMessage());
        }
    }

    private MavenResult runMaven(File dir, List<String> command) throws IOException, InterruptedException {
        log.info("running '" + command + "' in directory '" + dir.getPath() + "'");
        Process process = configureProcess(dir, command);
        Future<String> outputFuture = configureProcessOutputReader(process);
        Future<String> errorFuture = configureProcessErrorReader(process);
        boolean processResultCode = process.waitFor() == 0;
        String outputResult = readResultFromFuture(outputFuture);
        String errorResult = readResultFromFuture(errorFuture);
        if (!errorResult.isEmpty()) {
            log.warn("Maven connector error stream output: " + errorResult);
        }
        log.info("maven exited with status " + processResultCode);
        return new MavenResult(processResultCode, outputResult);
    }

    private Process configureProcess(File dir, List<String> command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.directory(dir).start();
        return process;
    }

    private Future<String> configureProcessErrorReader(Process process) {
        ProcessOutputReader error = new ProcessOutputReader(process.getErrorStream());
        return outputReaderPool.submit(error);
    }

    private Future<String> configureProcessOutputReader(Process process) throws IOException {
        ProcessOutputReader output;
        if (useLogFile) {
            File logFile = getNewLogFile();
            output = new ProcessOutputReader(process.getInputStream(), logFile);
        } else {
            output = new ProcessOutputReader(process.getInputStream());
        }
        return outputReaderPool.submit(output);
    }

    private String readResultFromFuture(Future<String> future) throws InterruptedException {
        String result;
        try {
            result = future.get();
        } catch (ExecutionException e) {
            log.error(e.getCause());
            result = ExceptionUtils.getFullStackTrace(e);
        }
        return result;
    }

    private File getNewLogFile() throws IOException {
        if (logDir.list().length + 1 > MAX_LOG_FILES) {
            assertLogLimit();
        }
        String dateString = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        String fileName = String.format("maven.%s.log", dateString);
        File logFile = new File(logDir, fileName);
        logFile.createNewFile();
        return logFile;
    }

    private boolean assertLogLimit() {
        File[] logFiles = logDir.listFiles();
        Arrays.sort(logFiles, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        return logFiles[0].delete();
    }

    public void setBuildEvents(BuildDomainEvents buildEvents) {
        this.buildEvents = buildEvents;
    }

    public void setTestEvents(TestDomainEvents testEvents) {
        this.testEvents = testEvents;
    }

    public void setDeployEvents(DeployDomainEvents deployEvents) {
        this.deployEvents = deployEvents;
    }

    public void setContextService(ContextCurrentService contextService) {
        this.contextService = contextService;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setUseLogFile(boolean useLogFile) {
        this.useLogFile = useLogFile;
    }

    public int getLogLimit() {
        return MAX_LOG_FILES;
    }

    public String getMvnVersion() {
        return mvnVersion;
    }

    public void setMvnVersion(String mvnVersion) throws Exception {        
        installMaven();
        this.mvnVersion = mvnVersion;
        mvnCommand = new File(System.getProperty("karaf.data")).getAbsolutePath() + "/apache-maven-" + mvnVersion
                + "/bin/mvn" + addSystemEnding();
    }

    private class MavenResult {
        private String output;

        private boolean success;

        public MavenResult(boolean success, String output) {
            this.success = success;
            this.output = output;
        }

        public String getOutput() {
            return output;
        }

        public boolean isSuccess() {
            return success;
        }
    }

}
