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

package org.openengsb.itests.htmlunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.workflow.api.RuleBaseException;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.core.workflow.api.WorkflowService;
import org.openengsb.core.workflow.api.model.RuleBaseElementId;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;
import org.openengsb.itests.htmlunit.testpanel.TestTaskPanel;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.openengsb.ui.common.taskbox.WebTaskboxService;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.ProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.Constants;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class TaskboxUiIT extends AbstractPreConfiguredExamTestHelper {

    private static final String CONTEXT = "it-taskbox";
    private static final String WORKFLOW = "HIDemoWorkflow";
    private String pageEntryUrl;
    private static final int MAX_RETRY = 5;
    private static final Integer MAX_SLEEP_TIME_IN_SECONDS = 30;

    private WebClient webClient;
    private WebTaskboxService taskboxService;
    private WorkflowService workflowService;
    private RuleManager ruleManager;

    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
        probe.setHeader(Constants.EXPORT_PACKAGE, "*,org.openengsb.itests.htmlunit.testpanel");
        return probe;
    }

    @BeforeClass
    public static void initialize() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Before
    public void setUp() throws Exception {
        String httpPort = getConfigProperty("org.ops4j.pax.web", "org.osgi.service.http.port");
        pageEntryUrl = "http://localhost:" + httpPort + "/openengsb/tasks/?context=" + CONTEXT;
        webClient = new WebClient();
        ContextCurrentService contextService = getOsgiService(ContextCurrentService.class);
        if (!contextService.getAvailableContexts().contains(CONTEXT)) {
            contextService.createContext(CONTEXT);
        }
        ContextHolder.get().setCurrentContextId(CONTEXT);
        ruleManager = getOsgiService(RuleManager.class);
        workflowService = getOsgiService(WorkflowService.class);
        taskboxService = getOsgiService(WebTaskboxService.class);

        waitForSiteToBeAvailable(pageEntryUrl, MAX_SLEEP_TIME_IN_SECONDS);
        authenticateAsAdmin();
        addWorkflow();
        loginAsAdmin();
    }

    @After
    public void tearDown() throws Exception {
        webClient.closeAllWindows();
    }

    @Test
    public void testIfTaskOverviewInteractionWorks_shouldWork() throws Exception {
        HtmlPage taskOverviewPage = webClient.getPage(pageEntryUrl);
        assertTrue("Page does not contain: No Records Found", taskOverviewPage.asText().contains("No Records Found"));
        assertEquals("The taskbox is not empty", 0, taskboxService.getOpenTasks().size());

        workflowService.startFlow(WORKFLOW);
        workflowService.startFlow(WORKFLOW);
        assertEquals("The taskbox does not contain the new tasks", 2, taskboxService.getOpenTasks().size());

        taskOverviewPage = taskOverviewPage.getAnchorByText("Task-Overview").click();
        waitForTextOnPage(taskOverviewPage, new ElementCondition() {
            @Override
            public boolean isPresent(HtmlPage page) {
                return page.getFirstByXPath("//table") != null;
            }
        });
        HtmlTable table = taskOverviewPage.getFirstByXPath("//table");
        assertNotNull("Table on Overviewpage not found", table);
        assertEquals("Not all tasks found on page", 4, table.getRowCount());
        HtmlTableRow headerRow = table.getRow(0);
        assertTrue(headerRow.asText().contains("TaskId"));
        HtmlTableRow actionsRow = table.getRow(1);
        assertTrue(actionsRow.asText().contains("filter clear"));
        HtmlTableRow taskOneRow = table.getRow(2);
        assertTrue(taskOneRow.asText().contains("step1"));
        assertEquals("even", taskOneRow.getAttribute("class"));
        HtmlTableRow taskTwoRow = table.getRow(3);
        assertTrue(taskTwoRow.asText().contains("step1"));
        assertEquals("odd", taskTwoRow.getAttribute("class"));
        String rowTwoText = taskTwoRow.asText();

        taskOverviewPage = taskOneRow.getCell(0).getHtmlElementsByTagName("a").get(0).click();
        waitForTextOnPage(taskOverviewPage, new ElementCondition() {
            @Override
            public boolean isPresent(HtmlPage page) {
                return page.getForms().size() >= 3;
            }
        });
        HtmlForm detailForm = taskOverviewPage.getForms().get(2);
        HtmlSubmitInput finishButton = (HtmlSubmitInput) detailForm.getByXPath("input[@type=\"submit\"]").get(0);
        detailForm.getInputByName("taskname").setValueAttribute("taskname");
        detailForm.getTextAreaByName("taskdescription").setText("taskdescription");
        taskOverviewPage = finishButton.click();

        boolean isRight = false;
        for (int i = 0; i < MAX_RETRY && !isRight; i++) {
            try {
                taskOverviewPage = webClient.getPage(pageEntryUrl);
                table = taskOverviewPage.getFirstByXPath("//table");
                taskOneRow = table.getRow(2);
                taskTwoRow = table.getRow(3);
                isRight =
                    taskOneRow.asText().contains("step2") && taskOneRow.asText().contains("taskdescription")
                            && taskTwoRow.asText().contains("step1") && table.getRowCount() == 4;
                if (!isRight) {
                    Thread.sleep(3000);
                }
            } catch (Exception ex) {

            }
        }
        if (!isRight) {
            fail("Could not process click event in time!");
        }

        assertEquals("The taskbox should contain 2 tasks", 2, taskboxService.getOpenTasks().size());
        taskOneRow.getCell(0).getHtmlElementsByTagName("a").get(0).click();
        taskOneRow.getCell(0).getHtmlElementsByTagName("a").get(0).click();
        waitForTextOnPage(taskOverviewPage, new ElementCondition() {
            @Override
            public boolean isPresent(HtmlPage page) {
                return page.getForms().size() >= 3;
            }
        });
        detailForm = taskOverviewPage.getForms().get(2);
        assertEquals("The taskname column is missing", "taskname", detailForm.getInputByName("taskname")
            .getValueAttribute());
        assertEquals("The taskdescription column is missing", "taskdescription",
            detailForm.getTextAreaByName("taskdescription").getText());
        finishButton = (HtmlSubmitInput) detailForm.getByXPath("input[@type=\"submit\"]").get(0);
        taskOverviewPage = finishButton.click();

        isRight = false;
        for (int i = 0; i < MAX_RETRY && !isRight; i++) {
            try {
                taskOverviewPage = webClient.getPage(pageEntryUrl);
                table = taskOverviewPage.getFirstByXPath("//table");
                taskOneRow = table.getRow(2);
                isRight =
                    taskOneRow.asText().contains("step1") && table.getRowCount() == 3;
                if (!isRight) {
                    Thread.sleep(3000);
                }
            } catch (Exception ex) {

            }
        }
        if (!isRight) {
            fail("Could not process click event in time!");
        }

        assertEquals("The second row should not have changed", rowTwoText, taskOneRow.asText());
        assertEquals("One task should be remaining", 1, taskboxService.getOpenTasks().size());
    }

    @Test
    public void testIfTaskPanelGetsReplaced_shouldWork() throws Exception {
        taskboxService.registerTaskPanel("step1", TestTaskPanel.class);
        workflowService.startFlow(WORKFLOW);

        HtmlPage taskOverviewPage = webClient.getPage(pageEntryUrl);
        taskOverviewPage = taskOverviewPage.getAnchorByText("Task-Overview").click();
        HtmlTable table = taskOverviewPage.getFirstByXPath("//table");
        assertNotNull("Table on Overviewpage not found", table);

        HtmlTableRow taskOneRow = table.getRow(2);
        taskOverviewPage = taskOneRow.getCell(0).getHtmlElementsByTagName("a").get(0).click();

        waitForTextOnPage(taskOverviewPage, new ElementCondition() {
            @Override
            public boolean isPresent(HtmlPage page) {
                return page.asText().contains("I am a test message!");
            }
        });
        assertTrue("Testpanel was not found!", taskOverviewPage.asText().contains("I am a test message!"));

    }

    private void addWorkflow() throws IOException, RuleBaseException {
        if (ruleManager.get(new RuleBaseElementId(RuleBaseElementType.Process, WORKFLOW)) == null) {
            InputStream is =
                getClass().getClassLoader().getResourceAsStream("rulebase/org/openengsb/" + WORKFLOW + ".rf");
            String testWorkflow = IOUtils.toString(is);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Process, WORKFLOW);
            ruleManager.add(id, testWorkflow);
            IOUtils.closeQuietly(is);
        }
    }

    private void loginAsAdmin() throws FailingHttpStatusCodeException, IOException {
        HtmlPage page = webClient.getPage(pageEntryUrl);
        HtmlForm form = page.getForms().get(0);
        HtmlSubmitInput loginButton = form.getInputByValue("Login");
        form.getInputByName("username").setValueAttribute("admin");
        form.getInputByName("password").setValueAttribute("password");
        loginButton.click();
    }

    private void waitForTextOnPage(HtmlPage page, ElementCondition condition) throws InterruptedException {
        for (int i = 0; i < MAX_RETRY; i++) {
            if (condition.isPresent(page)) {
                return;
            }
            Thread.sleep(3000);
        }
        fail("was waiting for element " + condition + " to appear on the page, but it did not.");
    }

}
