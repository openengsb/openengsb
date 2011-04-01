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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.TaskboxService;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

@RunWith(JUnit4TestRunner.class)
public class TaskboxUiIT extends AbstractExamTestHelper {

    private WebClient webClient;
    private TaskboxService taskboxService;
    private WorkflowService workflowService;
    private RuleManager ruleManager;
    private final String loginPageEntryUrl =
        "http://localhost:" + WEBUI_PORT + "/openengsb/TaskOverview/?context=it-taskbox";

    @Before
    public void setUp() throws Exception {
        super.beforeClass();

        webClient = new WebClient();

        ContextCurrentService contextService = getOsgiService(ContextCurrentService.class);
        if (!contextService.getAvailableContexts().contains("it-taskbox")) {
            contextService.createContext("it-taskbox");
        }
        contextService.setThreadLocalContext("it-taskbox");
        ruleManager = getOsgiService(RuleManager.class);
        workflowService = getOsgiService(WorkflowService.class);
        taskboxService = getOsgiService(TaskboxService.class, 30000);
    }

    @After
    public void tearDown() throws Exception {
        webClient.closeAllWindows();
        FileUtils.deleteDirectory(new File(getWorkingDirectory()));
    }

    @Test
    public void testIfTaskOverviewInteractionWorks() throws Exception {
        addWorkflow("HIDemoWorkflow");

        HtmlPage page = webClient.getPage(loginPageEntryUrl);
        HtmlForm form = page.getForms().get(0);
        HtmlSubmitInput loginButton = form.getInputByValue("Login");
        form.getInputByName("username").setValueAttribute("admin");
        form.getInputByName("password").setValueAttribute("password");
        HtmlPage taskOverviewPage = loginButton.click();

        assertTrue(taskOverviewPage.asText().contains("No Records Found"));
        assertTrue(taskboxService.getOpenTasks().size() == 0);

        workflowService.startFlow("HIDemoWorkflow");
        workflowService.startFlow("HIDemoWorkflow");

        assertTrue(taskboxService.getOpenTasks().size() == 2);

        taskOverviewPage = taskOverviewPage.getAnchorByText("Task-Overview").click();

        HtmlTable table = taskOverviewPage.getFirstByXPath("//table");
        assertTrue(table != null);
        assertEquals(4, table.getRowCount());
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
        HtmlForm detailForm = taskOverviewPage.getForms().get(2);

        HtmlSubmitInput finishButton = detailForm.getInputByName("submitButton");
        detailForm.getInputByName("taskname").setValueAttribute("taskname");
        detailForm.getTextAreaByName("taskdescription").setText("taskdescription");
        taskOverviewPage = finishButton.click();
        Thread.sleep(2000); // 2 seconds

        assertEquals(2, taskboxService.getOpenTasks().size());

        taskOverviewPage = webClient.getPage(loginPageEntryUrl);
        table = taskOverviewPage.getFirstByXPath("//table");
        taskOneRow = table.getRow(2);
        assertTrue(taskOneRow.asText().contains("step2"));
        assertTrue(taskOneRow.asText().contains("taskdescription"));
        taskTwoRow = table.getRow(3);
        assertTrue(taskTwoRow.asText().contains("step1"));

        taskOverviewPage = taskOneRow.getCell(0).getHtmlElementsByTagName("a").get(0).click();
        detailForm = taskOverviewPage.getForms().get(2);
        assertEquals("taskname", detailForm.getInputByName("taskname").getValueAttribute());
        assertEquals("taskdescription", detailForm.getTextAreaByName("taskdescription").getText());
        finishButton = detailForm.getInputByName("submitButton");
        taskOverviewPage = finishButton.click();
        Thread.sleep(2000); // 2 seconds

        taskOverviewPage = webClient.getPage(loginPageEntryUrl);
        table = taskOverviewPage.getFirstByXPath("//table");
        assertTrue(table != null);
        assertEquals(3, table.getRowCount());
        taskOneRow = table.getRow(2);
        assertTrue(taskOneRow.asText().contains("step1"));
        assertEquals(rowTwoText, taskOneRow.asText());
    }

    private void addWorkflow(String workflow) throws IOException, RuleBaseException {
        if (ruleManager.get(new RuleBaseElementId(RuleBaseElementType.Process, workflow)) == null) {
            InputStream is =
                getClass().getClassLoader().getResourceAsStream("rulebase/org/openengsb/" + workflow + ".rf");
            String testWorkflow = IOUtils.toString(is);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Process, workflow);
            ruleManager.add(id, testWorkflow);
            IOUtils.closeQuietly(is);
        }
    }

}
