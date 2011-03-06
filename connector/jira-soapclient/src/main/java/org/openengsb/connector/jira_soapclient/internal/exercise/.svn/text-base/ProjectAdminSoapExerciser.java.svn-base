package com.atlassian.jira_soapclient.exercise;

import com.atlassian.jira.rpc.soap.client.RemoteIssueType;
import com.atlassian.jira.rpc.soap.client.RemotePermissionScheme;
import com.atlassian.jira.rpc.soap.client.RemoteProject;
import com.atlassian.jira.rpc.soap.client.RemoteProjectRole;
import com.atlassian.jira.rpc.soap.client.RemoteScheme;
import com.atlassian.jira.rpc.soap.client.RemoteVersion;
import com.atlassian.jira_soapclient.SOAPSession;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class exercises the <b>Project Admin</b> functions of the JIRA SOAP API
 */
public class ProjectAdminSoapExerciser extends AbstractSoapExerciser
{
    public ProjectAdminSoapExerciser(final SOAPSession soapSession)
    {
        super(soapSession);
    }

    public RemoteVersion testCreateVersion() throws java.rmi.RemoteException
    {
        final RemoteVersion version = new RemoteVersion();
        version.setName("3 SOAP created version " + new Date());
        version.setSequence(new Long(6));
        version.setReleaseDate(getDateForServer(2008, 11, 4));
        final RemoteVersion createdVersion = getJiraSoapService().addVersion(getToken(), com.atlassian.jira_soapclient.exercise.ExerciserClientConstants.PROJECT_KEY, version);
        System.out.println("createdVersion.getId(): " + createdVersion.getId());
        return createdVersion;
    }

    public RemoteVersion[] testGetVersions(String projectKey) throws RemoteException
    {
        return getJiraSoapService().getVersions(getToken(), projectKey);
    }

    /**
     * Returns a Calendar that represents the given year/month/day relative to the server's timezone.
     *
     * Example: the SOAP client is located in GMT+11, and the server is GMT-1. If you were to simply set today's year/
     * month/ day on a Calendar, that value will be interpreted as 12 hours earlier on the server side i.e. the previous
     * day on the server.
     * So instead, we use a Calendar and set its TimeZone to the server time zone, and then set the year/month/day
     * explicitly. This will ensure that when the value gets to the server, it will be the exact date you intended.
     *
     * @param year the year
     * @param month the month (0-based)
     * @param day the day of the month
     * @return a Calendar that represents that date relative to server time
     */
    private Calendar getDateForServer(int year, int month, int day) throws RemoteException
    {
        Calendar calServer = Calendar.getInstance();
        calServer.setTimeZone(getServerTimeZone());
        calServer.set(year, month, day, 0, 0, 0);
        return calServer;
    }

    private TimeZone getServerTimeZone() throws RemoteException
    {
        return TimeZone.getTimeZone(getJiraSoapService().getServerInfo(getToken()).getServerTime().getTimeZoneId());
    }

    public void testReleaseVersion(RemoteVersion remoteVersion, String projectKey) throws java.rmi.RemoteException
    {
        getJiraSoapService().releaseVersion(getToken(), projectKey, remoteVersion);
    }

    public void testArchiveVersion(String projectKey, String versionName) throws java.rmi.RemoteException
    {
        getJiraSoapService().archiveVersion(getToken(), projectKey, versionName, true);
    }


    public String testCreateProject()
            throws java.rmi.RemoteException
    {
        RemoteProject project = new RemoteProject();
        project.setKey(ExerciserClientConstants.CREATE_PROJECT_KEY);
        project.setLead(ExerciserClientConstants.LOGIN_NAME);
        project.setName(ExerciserClientConstants.PROJECT_NAME);
        project.setDescription(ExerciserClientConstants.PROJECT_DESCRIPTION);

        RemotePermissionScheme defaultPermScheme = new RemotePermissionScheme();
        defaultPermScheme.setId(new Long(0));
        project.setPermissionScheme(defaultPermScheme);

        RemoteProject returnedProject = testCreateProjectFromObject(project);

        final String projectKey = returnedProject.getKey();
        System.out.println("Created project " + projectKey);

        return projectKey;
    }

    public RemoteProject testCreateProjectFromObject(RemoteProject project) throws java.rmi.RemoteException
    {
        return getJiraSoapService().createProjectFromObject(getToken(), project);
    }

    public void testDeleteProject(String projectKey) throws java.rmi.RemoteException
    {
        getJiraSoapService().deleteProject(getToken(), projectKey);
        System.out.println("Deleted project " + projectKey);
    }

    public RemoteIssueType[] getIssueTypesForProject(String userName, String password, String projectId)
            throws RemoteException
    {
        String currentToken = getTokenForUser(userName, password);
        return getJiraSoapService().getIssueTypesForProject(currentToken, projectId);
    }

    public RemoteIssueType[] getSubTaskIssueTypesForProject(String userName, String password, String projectId)
            throws RemoteException
    {
        String currentToken = getTokenForUser(userName, password);
        return getJiraSoapService().getSubTaskIssueTypesForProject(currentToken, projectId);
    }


    public RemoteScheme[] testGetAssociatedNotificationSchemes(RemoteProjectRole remoteprojectRole)
            throws RemoteException
    {
        return getJiraSoapService().getAssociatedNotificationSchemes(getToken(), remoteprojectRole);
    }

    public RemoteScheme[] testGetAssociatedPermissionSchemes(RemoteProjectRole remoteprojectRole) throws RemoteException
    {
        return getJiraSoapService().getAssociatedPermissionSchemes(getToken(), remoteprojectRole);
    }

    public RemoteProject testGetProjectById(Long id) throws RemoteException
    {
        return getJiraSoapService().getProjectById(getToken(), id.longValue());
    }

    public RemoteProject testGetProjectByKey(String key) throws RemoteException
    {
        return getJiraSoapService().getProjectByKey(getToken(), key);
    }

}