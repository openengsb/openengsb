package com.atlassian.jira_soapclient.exercise;

import com.atlassian.jira.rpc.soap.client.*;
import com.atlassian.jira_soapclient.SOAPSession;

import java.rmi.RemoteException;

/**
 * This class exercises the <b>User Admin</b> functions of the JIRA SOAP API
 */
public class UserAdminSoapExerciser extends AbstractSoapExerciser
{
    public UserAdminSoapExerciser(final SOAPSession soapSession)
    {
        super(soapSession);
    }

    public RemoteUser testAddUser(String username, String password, String fullname, String email)
            throws RemoteException
    {
        System.out.println("Testing create User: " + username);
        RemoteUser remoteUser = getJiraSoapService().createUser(getToken(), username, password, fullname, email);
        System.out.println("Successfully created User: " + username);
        return remoteUser;
    }

    public void testDeleteGroup(String groupName, String swapGroup) throws RemoteException
    {
        System.out.println("Testing group delete..");
        getJiraSoapService().deleteGroup(getToken(), groupName, swapGroup);
    }

    public void testCreateGroup(String groupName) throws RemoteException
    {
        System.out.println("Testing group create..");
        getJiraSoapService().createGroup(getToken(), groupName, null);
    }

    public void testAddUserToGroup(String groupName, String userName) throws RemoteException
    {
        System.out.println("Testing add user to group..");
        RemoteGroup group = new RemoteGroup(groupName, new RemoteUser[0]);
        RemoteUser user = new RemoteUser(null, null, userName);
        getJiraSoapService().addUserToGroup(getToken(), group, user);
    }

    public void testRemoveUserFromGroup(String groupName, String userName) throws RemoteException
    {
        System.out.println("Testing removing user from group..");
        RemoteGroup group = new RemoteGroup(groupName, new RemoteUser[0]);
        RemoteUser user = new RemoteUser(null, null, userName);
        getJiraSoapService().removeUserFromGroup(getToken(), group, user);
    }

    public void testDeleteUser(String username)
            throws RemoteException
    {
        System.out.println("Testing user delete..");
        getJiraSoapService().deleteUser(getToken(), username);
    }


    public void testUpdateGroup(String groupName, final String[] userNames)
            throws RemoteException
    {
        System.out.println("Testing group update..");

        RemoteUser[] remoteUsers = new RemoteUser[userNames.length];
        for (int i = 0; i < userNames.length; i++)
        {
            remoteUsers[i] = new RemoteUser(null, null, userNames[i]);
            System.out.println("newUser.getName() = " + remoteUsers[i].getName());
            System.out.println("newUser.getFullname() = " + remoteUsers[i].getFullname());
        }

        RemoteGroup group = new RemoteGroup(groupName, remoteUsers);
        System.out.println("Updating group: " + group.getName());
        System.out.println("group.getUsers(): " + (group.getUsers().length));
        getJiraSoapService().updateGroup(getToken(), group);

        group = getJiraSoapService().getGroup(getToken(), "jira-developers");
        System.out.println("group: " + group);
        System.out.println("group.getUsers(): " + (group.getUsers().length));
    }

    public RemoteProjectRole[] testGetProjectRoles() throws RemoteException
    {
        return getJiraSoapService().getProjectRoles(getToken());
    }

    public RemoteProjectRole testGetProjectRole() throws RemoteException
    {
        // This will return the 'Users' role
        return getJiraSoapService().getProjectRole(getToken(), Long.parseLong(ExerciserClientConstants.USER_PROJECT_ROLE_ID));
    }

    public RemoteProjectRole testCreateRole(final String roleName) throws RemoteException
    {
        RemoteProjectRole projectRole = new RemoteProjectRole();
        projectRole.setName(roleName);

        return getJiraSoapService().createProjectRole(getToken(), projectRole);
    }

    public boolean testIsRoleNameUnique(final String roleName) throws RemoteException
    {
        return getJiraSoapService().isProjectRoleNameUnique(getToken(), roleName);
    }

    public void testDeleteRole(final RemoteProjectRole remoteProjectRole) throws RemoteException
    {
        getJiraSoapService().deleteProjectRole(getToken(), remoteProjectRole, true);
    }

    public void testAddDefaultActorsToProjectRole(RemoteProjectRole projectRole, String actorName, String type)
            throws RemoteException
    {
        getJiraSoapService().addDefaultActorsToProjectRole(getToken(), new String[] { actorName }, projectRole, type);
    }

    public void testRemoveDefaultActorsFromProjectRole(RemoteProjectRole projectRole, String actorName, String type)
            throws RemoteException
    {
        getJiraSoapService().removeDefaultActorsFromProjectRole(getToken(), new String[] { actorName }, projectRole, type);
    }

    public void testRemoveAllRoleActorsByNameAndType(final String name, final String type) throws RemoteException
    {
        getJiraSoapService().removeAllRoleActorsByNameAndType(getToken(), name, type);
    }

    public void testRemoveAllRoleActorsByProject(RemoteProject remoteProject) throws RemoteException
    {
        getJiraSoapService().removeAllRoleActorsByProject(getToken(), remoteProject);
    }

    public void testAddActorsToProjectRole(RemoteProjectRole projectRole, RemoteProject remoteProject, String actor, String actorType)
            throws RemoteException
    {
        getJiraSoapService().addActorsToProjectRole(getToken(), new String[] { actor }, projectRole, remoteProject, actorType);
    }

    public void testRemoveActorsFromProjectRole(RemoteProjectRole projectRole, RemoteProject remoteProject, String actor, String actorType)
            throws RemoteException
    {
        getJiraSoapService().removeActorsFromProjectRole(getToken(), new String[] { actor }, projectRole, remoteProject, actorType);
    }

    public RemoteProjectRoleActors testGetProjectRoleActors(Long projectRoleId) throws RemoteException
    {
        RemoteProjectRole projectRole = getJiraSoapService().getProjectRole(getToken(), projectRoleId.longValue());

        // TODO : this is crap.  We should move out the assumption on monkey project into the actual test
        RemoteProject remoteProject = new RemoteProject();
        remoteProject.setId("10001");
        remoteProject.setName("monkey");
        remoteProject.setKey("MKY");


        return getJiraSoapService().getProjectRoleActors(getToken(), projectRole, remoteProject);
    }


    public void testUpdateRole(Long projectRoleId, String description) throws RemoteException
    {
        RemoteProjectRole projectRole = getJiraSoapService().getProjectRole(getToken(), projectRoleId.longValue());

        projectRole.setDescription(description);

        getJiraSoapService().updateProjectRole(getToken(), projectRole);

    }

    public RemoteRoleActors testGetDefaultRoleActors(Long projectRoleId) throws RemoteException
    {
        RemoteProjectRole projectRole = getJiraSoapService().getProjectRole(getToken(), projectRoleId.longValue());
        return getJiraSoapService().getDefaultRoleActors(getToken(), projectRole);
    }
}
