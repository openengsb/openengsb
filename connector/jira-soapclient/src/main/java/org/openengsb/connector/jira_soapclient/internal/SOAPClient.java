package org.openengsb.connector.jira_soapclient.internal;

import java.rmi.RemoteException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.AliveState;
import org.openengsb.domain.issue.IssueDomain;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.IssueAttribute;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemoteVersion;


/**
 * SOAPClient is an example of the SOAP APIs offered by JIRA.
 * <p/>
 * It is designed to be run against http://jira.atlassian.com.
 * <p/>
 * NOTE : This is not a "client side API" per se.  Its an example of how to use
 * the JIRA SOAP API as a client, and some of the calls available.
 * <p/>
 * If you want to see more SOAP example code, have a look at the com.atlassian.jira_soapclient.exercise
 * and the code therein.  This code is used by our functional test framework to
 * run SOAP API calls and assert that they have the desired affect on a JIRA instance.
 * <p/>
 */
public class SOAPClient extends AbstractOpenEngSBService implements IssueDomain {

    private static Log log = LogFactory.getLog(SOAPClient.class);

    private AliveState state = AliveState.DISCONNECTED;
    private String jiraUser;
    private String jiraPassword;
    private SOAPSession soapSession;
    private String projectKey;

    public SOAPClient(String id, SOAPSession soapSession, String projectKey) {
        super(id);
        this.soapSession = soapSession;
        this.projectKey = projectKey;
    }

    @Override
    public String createIssue(Issue engsbIssue) {
        //login
        this.state = AliveState.CONNECTING;
        JiraSoapService jiraSoapService = soapSession.getJiraSoapService();
        String authToken = soapSession.getAuthenticationToken();
        this.state = AliveState.ONLINE;
        // Create the issue
        RemoteIssue issue = convertIssue(engsbIssue);
        try {

            // Run the create issue code
            issue = jiraSoapService.createIssue(authToken, issue);

            log.info("Successfully created issue " + issue.getKey());
        } catch (RemoteException e) {
            log.error("Error creating issue " + issue.getSummary() + ". XMLRPC call failed.");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
        return issue.getId();
    }

    private RemoteIssue convertIssue(Issue engsbIssue) {
        RemoteIssue remoteIssue = new RemoteIssue();
        remoteIssue.setId(engsbIssue.getId());
        remoteIssue.setSummary(engsbIssue.getSummary());
        remoteIssue.setDescription(engsbIssue.getDescription());
        remoteIssue.setReporter(engsbIssue.getReporter());
        remoteIssue.setAssignee(engsbIssue.getOwner());
        remoteIssue.setProject(projectKey);

        Issue.Priority priority = engsbIssue.getPriority();
        switch (priority) {
            case IMMEDIATE:
                //Blocker
                remoteIssue.setPriority("1");
                break;
            case HIGH:
                //Critical
                remoteIssue.setPriority("2");
                break;
            case URGEND:
                //Major
                remoteIssue.setPriority("3");
                break;
            case NONE:
                //Minor
                remoteIssue.setPriority("4");
                break;
            case LOW:
                //Trivial
                remoteIssue.setPriority("5");
                break;
            default:
                remoteIssue.setPriority("4");
                break;
        }

        Issue.Status status = engsbIssue.getStatus();
        switch (status) {
            case CLOSED:
                remoteIssue.setStatus("6");
                break;
            default:
                remoteIssue.setStatus("1");
                break;
        }

        // Add remote versions
        RemoteVersion version = new RemoteVersion();
        version.setId(engsbIssue.getDueVersion());
        RemoteVersion[] remoteVersions = new RemoteVersion[]{version};
        remoteIssue.setFixVersions(remoteVersions);

        return remoteIssue;
    }

    @Override
    public void addComment(String id, String comment) {

    }

    @Override
    public void updateIssue(String id, String comment, HashMap<IssueAttribute, String> changes) {

    }

    @Override
    public void delayIssue(String id) {

    }

    @Override
    public AliveState getAliveState() {
        return null;
    }

    public AliveState getState() {
        return state;
    }

    public void setState(AliveState state) {
        this.state = state;
    }

    public String getJiraUser() {
        return jiraUser;
    }

    public void setJiraUser(String jiraUser) {
        this.jiraUser = jiraUser;
    }

    public String getJiraPassword() {
        return jiraPassword;
    }

    public void setJiraPassword(String jiraPassword) {
        this.jiraPassword = jiraPassword;
    }
}
