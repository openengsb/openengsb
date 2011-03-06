package org.openengsb.connector.jira.internal;

import static junit.framework.Assert.assertNotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.domain.issue.models.Issue;


public class JiraServiceUT {
    private static Log log = LogFactory.getLog(JiraServiceUT.class);
    // Login details
    static final String LOGIN_NAME = "soaptester";
    static final String LOGIN_PASSWORD = "soaptester";

    // Constants for issue creation
    static final String PROJECT_KEY = "TST";

    // Constant for get filter
    private static JiraService jiraClient;
    private static JiraSOAPSession jiraSoapSession;

    /**
     * testing server provided by jira
     */
    private static String baseUrl = "http://jira.atlassian.com/rpc/soap/jirasoapservice-v2";
    private static String issueId;


    @BeforeClass
    public static void setUpClass() throws Exception {
        jiraSoapSession = new JiraSOAPSession(baseUrl);
        jiraClient = new JiraService("id", jiraSoapSession, PROJECT_KEY);
        jiraClient.setJiraPassword(LOGIN_PASSWORD);
        jiraClient.setJiraUser(LOGIN_NAME);
        testCreateIssue();
    }

    public static void testCreateIssue() {
//        log.debug("test to create an issue");
//        Issue engsbIssue = createIssue();
//        issueId = jiraClient.createIssue(engsbIssue);
//        assertNotNull(issueId);
    }

    @Test
    public void testAddComment() {
//        log.debug("test to add a command to an issue");
//        jiraClient.addComment(issueId, "comment");

    }

    @Test
    public void testMoveAllIssuesFromOneReleaseToAnotherRelease() {
        jiraClient.moveIssuesFromReleaseToRelease("13203", "11410");
    }

    @Ignore("user has no rights to close a release")
    @Test
    public void closeRelease() {
        jiraClient.closeRelease("Version 2.0");
    }

    @Test
    public void testGenerateReleaseReport() {
        assertNotNull(jiraClient.generateReleaseReport("Version 2.0"));
    }

    private static Issue createIssue() {
        Issue issue = new Issue();
        issue.setSummary("summary");
        issue.setDescription("description");
        issue.setReporter("reporter");
        issue.setOwner("");
        issue.setPriority(Issue.Priority.NONE);
        issue.setStatus(Issue.Status.NEW);
        issue.setDueVersion("versionID1");
        issue.setType(Issue.Type.BUG);
        return issue;
    }
}
