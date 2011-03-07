package org.openengsb.connector.jira_soapclient.internal;

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.domain.issue.models.Issue;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;


public class SOAPClientUT {
      private static Log log = LogFactory.getLog(SOAPClientUT.class);
    // Login details
    static final String LOGIN_NAME = "soaptester";
    static final String LOGIN_PASSWORD = "soaptester";

    // Constants for issue creation
    static final String PROJECT_KEY = "TST";
    static final String ISSUE_TYPE_ID = "1";
    static final String SUMMARY_NAME = "An issue created via the JIRA SOAPClient sample : " + new Date();
    static final String PRIORITY_ID = "4";
    static final String COMPONENT_ID = "10240";
    static final String VERSION_ID = "10330";

    // Constants for issue update
    static final String NEW_SUMMARY = "New summary";
    static final String CUSTOM_FIELD_KEY_1 = "customfield_10061";
    static final String CUSTOM_FIELD_VALUE_1 = "10098";
    static final String CUSTOM_FIELD_KEY_2 = "customfield_10061:1";
    static final String CUSTOM_FIELD_VALUE_2 = "10105";

    // Constant for add comment
    static final String NEW_COMMENT_BODY = "This is a new comment";

    // Constant for get filter
    static final String FILTER_ID_FIXED_FOR_RELEASED_VERSION = "12355"; /// Fixed for released versions
    static final String SOAP_AS_A_SEARCH_TERM = "SOAPClient";
    private static JiraSoapService jiraSoapService;
    private static String authToken;
    private static RemoteIssue issue = new RemoteIssue();
    private static SOAPClient jiraClient;
    private static SOAPSession soapSession;

    /**
     * testing server provided by jira
     */
    private static String baseUrl = "http://jira.atlassian.com/rpc/soap/jirasoapservice-v2";
    private static String issueId;


    @BeforeClass
    public static void setUpClass() throws Exception {
        soapSession = new SOAPSession(new URL(baseUrl));
        jiraClient = new SOAPClient("id", soapSession, PROJECT_KEY);
        jiraClient.setJiraPassword(LOGIN_PASSWORD);
        jiraClient.setJiraUser(LOGIN_NAME);
        testCreateIssue();
    }

    public static void testCreateIssue() {
        log.debug("test to create an issue");
        Issue engsbIssue = createIssue();
        issueId = jiraClient.createIssue(engsbIssue);
        assertNotNull(issueId);
    }

    @Test
    public void testAddComment() {
        log.debug("test to add a command to an issue");
        jiraClient.addComment(issueId, "comment");
        
    }

    @Test
    public void delayIssue() {
       jiraClient.delayIssue(issueId);
    }

    @Ignore("user has no rights to close a release")
    @Test
    public void closeRelease() {
        jiraClient.closeRelease("Version 2.0");
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
