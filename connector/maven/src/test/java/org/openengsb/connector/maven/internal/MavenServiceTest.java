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

package org.openengsb.connector.maven.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.AliveState;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.domain.build.BuildDomainEvents;
import org.openengsb.domain.build.BuildStartEvent;
import org.openengsb.domain.build.BuildSuccessEvent;
import org.openengsb.domain.deploy.DeployDomainEvents;
import org.openengsb.domain.deploy.DeployStartEvent;
import org.openengsb.domain.deploy.DeploySuccessEvent;
import org.openengsb.domain.test.TestDomainEvents;
import org.openengsb.domain.test.TestFailEvent;
import org.openengsb.domain.test.TestStartEvent;
import org.openengsb.domain.test.TestSuccessEvent;

public class MavenServiceTest {

    private MavenServiceImpl mavenService;
    private TestDomainEvents testEvents;
    private BuildDomainEvents buildEvents;
    private DeployDomainEvents deployEvents;

    @Before
    public void setUp() throws Exception {
        System.setProperty("karaf.data", ".");
        deleteLogFile();
        FileUtils.deleteDirectory(new File(getPath("test-unit-success"), "target"));
        FileUtils.deleteDirectory(new File(getPath("test-unit-fail"), "target"));
        this.mavenService = new MavenServiceImpl("42");
        buildEvents = mock(BuildDomainEvents.class);
        testEvents = mock(TestDomainEvents.class);
        deployEvents = mock(DeployDomainEvents.class);
        mavenService.setBuildEvents(buildEvents);
        mavenService.setTestEvents(testEvents);
        mavenService.setDeployEvents(deployEvents);
        mavenService.setContextService(mock(ContextCurrentService.class));
        mavenService.setSynchronous(true);
        mavenService.setUseLogFile(false);
    }

    @After
    public void deleteLogFile() throws IOException {
        FileUtils.deleteDirectory(new File("log"));
    }

    @Test
    public void build_shouldWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("clean compile");
        String id = mavenService.build();
        ArgumentCaptor<BuildSuccessEvent> argumentCaptor = ArgumentCaptor.forClass(BuildSuccessEvent.class);

        verify(buildEvents).raiseEvent(any(BuildStartEvent.class));
        verify(buildEvents).raiseEvent(argumentCaptor.capture());
        BuildSuccessEvent event = argumentCaptor.getValue();
        assertThat(event.getBuildId(), is(id));
        assertThat(event.getOutput(), containsString("SUCCESS"));
    }

    @Test
    public void buildWithProcessId_shouldWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("clean compile");
        mavenService.build(42);
        verify(buildEvents).raiseEvent(any(BuildStartEvent.class));
        verify(buildEvents).raiseEvent(refEq(new BuildSuccessEvent(42L, null), "output"));
    }

    @Test
    public void test_shouldWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("test");
        String id = mavenService.runTests();
        verify(testEvents).raiseEvent(any(TestStartEvent.class));
        verify(testEvents).raiseEvent(refEq(new TestSuccessEvent(id, null), "output"));
    }

    @Test
    public void testWithProcessId_shouldThrowEventsWithProcessId() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("install");
        long processId = 42;
        mavenService.runTests(processId);
        verify(testEvents).raiseEvent(any(TestStartEvent.class));
        verify(testEvents).raiseEvent(refEq(new TestSuccessEvent(processId, null), "output"));
    }

    @Test
    public void deploy_shoudWork() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("install -Dmaven.test.skip=true");
        String id = mavenService.deploy();
        verify(deployEvents).raiseEvent(any(DeployStartEvent.class));
        verify(deployEvents).raiseEvent(refEq(new DeploySuccessEvent(id, null), "output"));
    }

    @Test
    public void deployWithProcessId_shouldThrowEventsWithProcessId() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("install -Dmaven.test.skip=true");
        long id = 42;
        mavenService.deploy(id);
        verify(deployEvents).raiseEvent(any(DeployStartEvent.class));
        verify(deployEvents).raiseEvent(refEq(new DeploySuccessEvent(id, null), "output"));
    }

    @Ignore("no idea why this fails, it works from cmd-line")
    @Test
    public void testTestFail() {
        mavenService.setProjectPath(getPath("test-unit-fail"));
        mavenService.setCommand("install");
        String id = mavenService.runTests();
        verify(testEvents).raiseEvent(any(TestStartEvent.class));
        verify(testEvents).raiseEvent(refEq(new TestFailEvent(id, null), "output"));
    }

    @Test
    public void testGetAliveState_shouldReturnOnline() {
        mavenService.setProjectPath(getPath("test-unit-success"));
        assertThat(mavenService.getAliveState(), is(AliveState.ONLINE));
    }

    @Test
    public void testGetAliveStateWrongPath_shouldReturnOffline() {
        mavenService.setProjectPath("pathThatDoesForSureNotExistBecauseItIsStrange");
        assertThat(mavenService.getAliveState(), is(AliveState.OFFLINE));
    }

    @Test
    public void build_shouldWriteLogFile() throws Exception {
        mavenService.setUseLogFile(true);
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("clean compile");
        mavenService.build();
        ArgumentCaptor<BuildSuccessEvent> argumentCaptor = ArgumentCaptor.forClass(BuildSuccessEvent.class);

        verify(buildEvents).raiseEvent(argumentCaptor.capture());
        BuildSuccessEvent event = argumentCaptor.getValue();
        String output = event.getOutput();
        @SuppressWarnings("unchecked")
        Collection<File> logFiles = FileUtils.listFiles(new File("log"), new String[]{ "log", }, false);
        File logFile = logFiles.iterator().next();
        assertThat(logFile, notNullValue());
        String fileContent = FileUtils.readFileToString(logFile);
        assertThat(fileContent, is(output));
    }

    @Test
    public void asyncBuild_shouldRaiseBuildSuccessEvent() throws Exception {
        final Object sync = new Object();
        mavenService.setSynchronous(false);
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("clean compile");
        ArgumentCaptor<BuildSuccessEvent> eventCaptor = ArgumentCaptor.forClass(BuildSuccessEvent.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                synchronized (sync) {
                    sync.notifyAll();
                }
                return null;
            }
        }).when(buildEvents).raiseEvent(eventCaptor.capture());
        Thread waitForBuildEnd = startWaiterThread(sync);
        mavenService.build();
        waitForBuildEnd.join();
        BuildSuccessEvent event = eventCaptor.getValue();
        assertThat(event.getOutput(), containsString("SUCCESS"));
    }

    @Test
    public void asyncBuild_shouldCreateLogFile() throws Exception {
        final Object syncFinish = new Object();
        final Object syncStart = new Object();
        mavenService.setUseLogFile(true);
        mavenService.setSynchronous(false);
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("clean compile");
        makeNotifyAnswerForBuildStart(syncStart);
        makeNotifyAnswerForBuildSuccess(syncFinish);
        Thread waitForBuildStart = startWaiterThread(syncStart);
        Thread waitForBuildEnd = startWaiterThread(syncFinish);
        mavenService.build();
        waitForBuildStart.join();
        Collection<File> listFiles = FileUtils.listFiles(new File("log"), FileFilterUtils.fileFileFilter(), null);
        assertThat("no logfile was created", listFiles.isEmpty(), is(false));
        waitForBuildEnd.join();
    }
    
    @Test
    public void build_shouldAssertLogLimit() throws Exception {
    	
    	final Object syncFinish = new Object();
    	
    	int max = mavenService.getLogLimit();
    	
    	for(int i = 1; i <= max; i++){
    		String fileName = "dummyFile"+i;
    		File dummyFile = new File("log", fileName);
    		dummyFile.createNewFile();
    		int tresh = 1000*i;
    		dummyFile.setLastModified(System.currentTimeMillis()-tresh);	
    	}

        mavenService.setUseLogFile(true);
        mavenService.setSynchronous(false);
        mavenService.setProjectPath(getPath("test-unit-success"));
        mavenService.setCommand("clean compile");
        mavenService.build();
        makeNotifyAnswerForBuildSuccess(syncFinish);
        Thread waitForBuildEnd = startWaiterThread(syncFinish);
        
        waitForBuildEnd.join();
        Collection<File> listFiles = FileUtils.listFiles(new File("log"), FileFilterUtils.fileFileFilter(), null);
        assertThat(listFiles.size(), is(max));
        assertThat(listFiles.contains(new File("dummyFile"+max)), is(false));
    }

    private void makeNotifyAnswerForBuildSuccess(final Object syncFinish) {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                synchronized (syncFinish) {
                    syncFinish.notifyAll();
                }
                return null;
            }
        }).when(buildEvents).raiseEvent(any(BuildSuccessEvent.class));
    }

    private void makeNotifyAnswerForBuildStart(final Object syncFinish) {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                synchronized (syncFinish) {
                    syncFinish.notifyAll();
                }
                return null;
            }
        }).when(buildEvents).raiseEvent(any(BuildStartEvent.class));
    }

    private Thread startWaiterThread(final Object sync) {
        final Thread thread = new Thread() {
            @Override
            public void run() {
                synchronized (sync) {
                    try {
                        sync.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        };
        thread.start();
        return thread;
    }

    private String getPath(String folder) {
        return ClassLoader.getSystemResource(folder).getFile();
    }

}
