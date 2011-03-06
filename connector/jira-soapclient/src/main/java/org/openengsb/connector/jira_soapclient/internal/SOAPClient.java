package org.openengsb.connector.jira_soapclient.internal;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.connector.jira_soapclient.internal.misc.FieldConverter;
import org.openengsb.connector.jira_soapclient.internal.misc.PriorityConverter;
import org.openengsb.connector.jira_soapclient.internal.misc.StatusConverter;
import org.openengsb.connector.jira_soapclient.internal.misc.TypeConverter;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.AliveState;
import org.openengsb.domain.issue.IssueDomain;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.IssueAttribute;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteAuthenticationException;
import com.atlassian.jira.rpc.soap.client.RemoteComment;
import com.atlassian.jira.rpc.soap.client.RemoteFieldValue;
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
        RemoteIssue issue = null;
        try {
            soapSession.connect(jiraUser, jiraPassword);

            String authToken = soapSession.getAuthenticationToken();
            this.state = AliveState.ONLINE;
            // Create the issue
            issue = convertIssue(engsbIssue);

            // Run the create issue code
            issue = jiraSoapService.createIssue(authToken, issue);
            log.info("Successfully created issue " + issue.getKey());
        } catch (RemoteException e) {
            log.error("Error creating issue " + issue.getSummary() + ". XMLRPC call failed.");
            return null;
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
        return issue.getId();
    }


    @Override
    public void addComment(String id, String commentString) {
        //login
        this.state = AliveState.CONNECTING;
        JiraSoapService jiraSoapService = soapSession.getJiraSoapService();
        try {
            soapSession.connect(jiraUser, jiraPassword);

            String authToken = soapSession.getAuthenticationToken();
            this.state = AliveState.ONLINE;

            // Adding a comment
            final RemoteComment comment = new RemoteComment();
            comment.setBody(commentString);
            RemoteIssue issue = getIssueById(id);
            jiraSoapService.addComment(authToken, issue.getKey(), comment);
        } catch (RemoteException e) {
            log.error("Error commenting issue . XMLRPC call failed. ");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }

    }

    @Override
    public void updateIssue(String id, String comment, HashMap<IssueAttribute, String> changes) {
        //login
        this.state = AliveState.CONNECTING;
        JiraSoapService jiraSoapService = soapSession.getJiraSoapService();
        try {
            soapSession.connect(jiraUser, jiraPassword);

            String authToken = soapSession.getAuthenticationToken();
            this.state = AliveState.ONLINE;
            RemoteIssue issue = getIssueById(id);

            RemoteFieldValue[] values = convertChanges(changes);
            jiraSoapService.updateIssue(authToken, issue.getKey(), values);
        } catch (RemoteException e) {
            log.error("Error updating the issue . XMLRPC call failed. ");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
    }

    private RemoteFieldValue[] convertChanges(HashMap<IssueAttribute, String> changes) {
        Set<IssueAttribute> changedAttributes = new HashSet<IssueAttribute>(changes.keySet());
        ArrayList<RemoteFieldValue> remoteFields = new ArrayList<RemoteFieldValue>();

        if (changedAttributes.contains(Issue.Field.STATUS)) {
            Issue.Status status = Issue.Status.valueOf(changes.get(Issue.Field.STATUS));
            RemoteFieldValue rfv = new RemoteFieldValue();
            rfv.setId("STATUS");
            rfv.setValues(new String[]{StatusConverter.fromIssueStatus(status)});
            changedAttributes.remove(Issue.Field.STATUS);
            remoteFields.add(rfv);
        }

        if (changedAttributes.contains(Issue.Field.TYPE)) {
            Issue.Type type = Issue.Type.valueOf(changes.get(Issue.Field.TYPE));
            RemoteFieldValue rfv = new RemoteFieldValue();
            rfv.setId("issuetype");
            rfv.setValues(new String[]{TypeConverter.fromIssueType(type)});
            changedAttributes.remove(Issue.Field.TYPE);
            remoteFields.add(rfv);
        }
        if (changedAttributes.contains(Issue.Field.PRIORITY)) {
            Issue.Priority priority = Issue.Priority.valueOf(changes.get(Issue.Field.PRIORITY));
            RemoteFieldValue rfv = new RemoteFieldValue();
            rfv.setId("priority");
            rfv.setValues(new String[]{PriorityConverter.fromIssuePriority(priority)});
            changedAttributes.remove(Issue.Field.PRIORITY);
            remoteFields.add(rfv);
        }

        for (IssueAttribute attribute : changedAttributes) {
            String targetField = FieldConverter.fromIssueField((Issue.Field) attribute);
            if (targetField != null) {
                RemoteFieldValue rfv = new RemoteFieldValue();
                rfv.setId("priority");
                rfv.setValues(new String[]{changes.get(attribute)});
                remoteFields.add(rfv);
            }
        }
        RemoteFieldValue[] remoteFieldArray = new RemoteFieldValue[remoteFields.size()];
        remoteFields.toArray(remoteFieldArray);
        return remoteFieldArray;
    }

    @Override
    public void delayIssue(String id) {

    }

    @Override
    public AliveState getAliveState() {
        return this.state;
    }


    private RemoteIssue convertIssue(Issue engsbIssue) {
        RemoteIssue remoteIssue = new RemoteIssue();
        remoteIssue.setSummary(engsbIssue.getSummary());
        remoteIssue.setDescription(engsbIssue.getDescription());
        remoteIssue.setReporter(engsbIssue.getReporter());
        remoteIssue.setAssignee(engsbIssue.getOwner());
        remoteIssue.setProject(projectKey);

        remoteIssue.setPriority(PriorityConverter.fromIssuePriority(engsbIssue.getPriority()));
        remoteIssue.setStatus(StatusConverter.fromIssueStatus(engsbIssue.getStatus()));
        remoteIssue.setType(TypeConverter.fromIssueType(engsbIssue.getType()));

        // Add remote versions
        RemoteVersion version = new RemoteVersion();
        version.setId(engsbIssue.getDueVersion());
        RemoteVersion[] remoteVersions = new RemoteVersion[]{version};
        remoteIssue.setFixVersions(remoteVersions);

        return remoteIssue;
    }

    private RemoteIssue getIssueById(String id) throws RemoteException, RemoteAuthenticationException {
        this.state = AliveState.CONNECTING;
        JiraSoapService jiraSoapService = soapSession.getJiraSoapService();
        RemoteIssue remoteIssue = null;
        try {
            soapSession.connect(jiraUser, jiraPassword);

            String authToken = soapSession.getAuthenticationToken();
            this.state = AliveState.ONLINE;

            remoteIssue = jiraSoapService.getIssue(authToken, id);

        } finally {
            this.state = AliveState.DISCONNECTED;
        }
        return remoteIssue;
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
