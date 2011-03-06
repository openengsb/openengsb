package com.atlassian.jira_soapclient.exercise;

import com.atlassian.jira.rpc.soap.client.RemoteComment;
import com.atlassian.jira_soapclient.SOAPSession;

import java.rmi.RemoteException;

/**
 * This class exercises the <b>Comments</b> functions of the JIRA SOAP API
 */
public class CommentsSoapExerciser extends AbstractSoapExerciser
{

    public CommentsSoapExerciser(final SOAPSession soapSession)
    {
        super(soapSession);
    }

    /**
     * Add a blank comment.
     * <p/>
     * <strong>NOTE: this should fail.</strong>
     *
     * @param issueKey key of issue to add comment for.
     * @throws java.rmi.RemoteException
     */
    public void testAddBlankComment(final String issueKey) throws java.rmi.RemoteException
    {

        final RemoteComment comment = new RemoteComment();
        comment.setBody(null);
        getJiraSoapService().addComment(getToken(), issueKey, comment);
    }

    public RemoteComment[] testGetComments(final String issueKey) throws RemoteException
    {
        return getJiraSoapService().getComments(getToken(), issueKey);
    }

    public void testAddComment(final String issueKey, String commentBody)
            throws java.rmi.RemoteException
    {
        // Adding a comment
        final RemoteComment comment = new RemoteComment();
        comment.setBody(commentBody);
        getJiraSoapService().addComment(getToken(), issueKey, comment);
    }

    public void testAddCommentWithVisibility(final String issueKey, final String groupLevel, final String roleLevel, String commentBody)
            throws java.rmi.RemoteException
    {
        // Adding a comment
        final RemoteComment comment = new RemoteComment();
        comment.setBody(commentBody);
        comment.setGroupLevel(groupLevel);
        comment.setRoleLevel(roleLevel);
        getJiraSoapService().addComment(getToken(), issueKey, comment);
    }

    public void testEditCommentAsIs(RemoteComment comment) throws RemoteException
    {
        getJiraSoapService().editComment(getToken(), comment);
    }

    public void testEditComment(RemoteComment comment, String commentBody) throws RemoteException
    {
        comment.setBody(commentBody);
        getJiraSoapService().editComment(getToken(), comment);
    }

    public void testEditCommentWithVisibility(RemoteComment comment, final String groupLevel, final String roleLevel, String commentBody)
            throws RemoteException
    {
        comment.setBody(commentBody);
        comment.setGroupLevel(groupLevel);
        comment.setRoleLevel(roleLevel);
        getJiraSoapService().editComment(getToken(), comment);
    }

    public boolean testHasPermissionToEditComment(RemoteComment comment) throws RemoteException
    {
        return getJiraSoapService().hasPermissionToEditComment(getToken(), comment);
    }

    public RemoteComment testGetComment(Long id) throws RemoteException
    {
        if (id != null)
        {
            return getJiraSoapService().getComment(getToken(), id.longValue());
        }
        return null;
    }

}