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


public class JiraService extends AbstractOpenEngSBService implements IssueDomain {

    private static Log log = LogFactory.getLog(JiraService.class);

    private AliveState state = AliveState.DISCONNECTED;
    private String jiraUser;
    private String jiraPassword;
    private JiraSOAPSession jiraSoapSession;
    private String projectKey;

    public JiraService(String id, JiraSOAPSession jiraSoapSession, String projectKey) {
        super(id);
        this.jiraSoapSession = jiraSoapSession;
        this.projectKey = projectKey;
    }

    @Override
    public String createIssue(Issue engsbIssue) {
        //login
        this.state = AliveState.CONNECTING;
        JiraSoapService jiraSoapService = jiraSoapSession.getJiraSoapService();
        RemoteIssue issue = null;
        try {
            jiraSoapSession.connect(jiraUser, jiraPassword);

            String authToken = jiraSoapSession.getAuthenticationToken();
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
        return issue.getKey();
    }


    @Override
    public void addComment(String issueKey, String commentString) {
        //login
        this.state = AliveState.CONNECTING;
        JiraSoapService jiraSoapService = jiraSoapSession.getJiraSoapService();
        try {
            jiraSoapSession.connect(jiraUser, jiraPassword);

            String authToken = jiraSoapSession.getAuthenticationToken();
            this.state = AliveState.ONLINE;

            // Adding a comment
            final RemoteComment comment = new RemoteComment();
            comment.setBody(commentString);
            jiraSoapService.addComment(authToken, issueKey, comment);
        } catch (RemoteException e) {
            log.error("Error commenting issue . XMLRPC call failed. ");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }

    }

    @Override
    public void updateIssue(String issueKey, String comment, HashMap<IssueAttribute, String> changes) {
        //login
        this.state = AliveState.CONNECTING;
        JiraSoapService jiraSoapService = jiraSoapSession.getJiraSoapService();
        try {
            jiraSoapSession.connect(jiraUser, jiraPassword);

            String authToken = jiraSoapSession.getAuthenticationToken();
            this.state = AliveState.ONLINE;

            RemoteFieldValue[] values = convertChanges(changes);
            jiraSoapService.updateIssue(authToken, issueKey, values);
        } catch (RemoteException e) {
            log.error("Error updating the issue . XMLRPC call failed. ");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
    }


    @Override
    public void moveIssuesFromReleaseToRelease(String releaseFromId, String releaseToId) {
        //login
        this.state = AliveState.CONNECTING;
        JiraSoapService jiraSoapService = jiraSoapSession.getJiraSoapService();
        try {
            jiraSoapSession.connect(jiraUser, jiraPassword);

            String authToken = jiraSoapSession.getAuthenticationToken();
            this.state = AliveState.ONLINE;

            RemoteVersion version = getNextVersion(authToken, jiraSoapService, releaseToId);

            RemoteIssue[] issues = jiraSoapService
                .getIssuesFromJqlSearch(authToken, "fixVersion in (\"Test Version 1\") ", 199);

            RemoteFieldValue[] changes = new RemoteFieldValue[1];
            RemoteFieldValue change = new RemoteFieldValue();
            change.setId("fixVersions");
            change.setValues(new String[]{version.getId()});

            changes[0] = change;
            for (RemoteIssue issue : issues) {
                jiraSoapService.updateIssue(authToken, issue.getKey(), changes);
            }
        } catch (RemoteException e) {
            log.error("Error updating the issue . XMLRPC call failed. ");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
    }

    @Override
    public void closeRelease(String id) {
        //login
        this.state = AliveState.CONNECTING;
        JiraSoapService jiraSoapService = jiraSoapSession.getJiraSoapService();
        try {
            jiraSoapSession.connect(jiraUser, jiraPassword);

            String authToken = jiraSoapSession.getAuthenticationToken();
            this.state = AliveState.ONLINE;

            RemoteVersion[] versions = jiraSoapService.getVersions(authToken, projectKey);
            RemoteVersion version = null;
            for (RemoteVersion ver : versions) {
                if (id.equals(ver.getName())) {
                    version = ver;
                }
            }
            if (version == null) {
                log.error("Release not found");
                return;
            }
            jiraSoapService.releaseVersion(authToken, projectKey, version);
        } catch (RemoteException e) {
            log.error("Error closing release, Remote exception ");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
    }

    private RemoteVersion getNextVersion(String authToken, JiraSoapService jiraSoapService, String releaseToId)
        throws RemoteException {
        RemoteVersion[] versions = jiraSoapService.getVersions(authToken, this.projectKey);
        RemoteVersion next = null;
        for (RemoteVersion version : versions) {
            if (releaseToId.equals(version.getId())) {
                next = version;
            }
        }
        return next;
    }

    @Override
    public AliveState getAliveState() {
        return this.state;
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
        JiraSoapService jiraSoapService = jiraSoapSession.getJiraSoapService();
        RemoteIssue remoteIssue = null;
        try {
            jiraSoapSession.connect(jiraUser, jiraPassword);

            String authToken = jiraSoapSession.getAuthenticationToken();
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

    public JiraSOAPSession getSoapSession() {
        return jiraSoapSession;
    }

    public void setSoapSession(JiraSOAPSession jiraSoapSession) {
        this.jiraSoapSession = jiraSoapSession;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }
}
