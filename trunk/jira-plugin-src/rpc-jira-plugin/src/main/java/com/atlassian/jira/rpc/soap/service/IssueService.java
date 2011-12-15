package com.atlassian.jira.rpc.soap.service;


import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.soap.beans.*;

import java.util.Date;
import java.util.Map;

public interface IssueService
{
    RemoteIssue getIssue(User user, String issueKey) throws RemoteException, RemotePermissionException;

    RemoteIssue createIssueWithSecurityLevel(User user, String parentIssueKey, RemoteIssue rIssue, Long securityLevelId) throws RemotePermissionException, RemoteValidationException, RemoteException;

    RemoteComment[] getComments(User user, String issueKey) throws RemoteException, RemotePermissionException;

    boolean addAttachmentsToIssue(User user, String issueKey, String[] fileNames, byte[][] attachments) throws RemoteException, RemotePermissionException;

    RemoteAttachment[] getAttachmentsFromIssue(User user, String issueKey) throws RemoteException;

    void deleteIssue(User user, String issueKey) throws RemoteException, RemotePermissionException;

    void addComment(User user, String issueKey, RemoteComment remoteComment) throws RemoteException, RemotePermissionException;

    boolean hasPermissionToEditComment(User user, RemoteComment remoteComment) throws RemoteException;

    RemoteComment editComment(User user, RemoteComment remoteComment) throws RemoteException;

    RemoteComment getComment(User user, Long commentId) throws RemoteException;

    RemoteIssue updateIssue(User user, String issueKey, Map actionParams) throws RemoteException;

    RemoteField[] getFieldsForCreate(User user, String projectKey, Long issueTypeId) throws RemoteException;

    RemoteField[] getFieldsForEdit(User user, String issueKey) throws RemoteException;

    RemoteIssue updateIssue(User user, String issueKey, RemoteFieldValue[] actionParams) throws RemoteException;

    RemoteNamedObject[] getAvailableActions(User user, String issueKey) throws RemoteException;

    RemoteField[] getFieldsForAction(User user, String issueKey, String actionIdString) throws RemoteException;

    RemoteIssue progressWorkflowAction(User user, String issueKey, String actionIdString, RemoteFieldValue[] actionParams) throws RemoteException;

    RemoteIssue getIssueById(User user, String issueId) throws RemoteException, RemotePermissionException;

    RemoteWorklog addWorklogWithNewRemainingEstimate(User user, String issueKey, RemoteWorklog remoteWorklog, String newRemainingEstimate) throws RemoteException, RemotePermissionException, RemoteValidationException;

    RemoteWorklog addWorklogAndAutoAdjustRemainingEstimate(User user, String issueKey, RemoteWorklog remoteWorklog) throws RemoteException, RemotePermissionException, RemoteValidationException;

    RemoteWorklog addWorklogAndRetainRemainingEstimate(User user, String issueKey, RemoteWorklog remoteWorklog) throws RemoteException, RemotePermissionException, RemoteValidationException;

    void deleteWorklogWithNewRemainingEstimate(User user, String remoteWorklogId, String newRemainingEstimate) throws RemoteException, RemotePermissionException, RemoteValidationException;

    void deleteWorklogAndAutoAdjustRemainingEstimate(User user, String remoteWorklogId) throws RemoteException, RemotePermissionException, RemoteValidationException;

    void deleteWorklogAndRetainRemainingEstimate(User user, String remoteWorklogId) throws RemoteException, RemotePermissionException, RemoteValidationException;

    void updateWorklogWithNewRemainingEstimate(User user, RemoteWorklog remoteWorklog, String newRemainingEstimate) throws RemoteException, RemotePermissionException, RemoteValidationException;

    void updateWorklogAndAutoAdjustRemainingEstimate(User user, RemoteWorklog remoteWorklog) throws RemoteException, RemotePermissionException, RemoteValidationException;

    void updateWorklogAndRetainRemainingEstimate(User user, RemoteWorklog remoteWorklog) throws RemoteException, RemotePermissionException, RemoteValidationException;

    RemoteWorklog[] getWorklogs(User user, String issueKey) throws RemoteException, RemotePermissionException, RemoteValidationException;

    boolean hasPermissionToCreateWorklog(User user, String issueKey) throws RemoteException, RemoteValidationException;

    boolean hasPermissionToDeleteWorklog(User user, String worklogId) throws RemoteException, RemoteValidationException;

    boolean hasPermissionToUpdateWorklog(User user, String worklogId) throws RemoteException, RemoteValidationException;

    /**
     * Returns the current security level for given issue.
     * <p/>
     * If the user has permission to see the issue, but not the security level, null is returned.
     *
     * @param user     user performing this operation
     * @param issueKey the issue key
     * @return issue security level or null
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the issue key is invalid
     *                                   or the user is not permitted to see this issue.
     * @since v3.13
     */
    RemoteSecurityLevel getSecurityLevel(User user, String issueKey)
            throws RemoteException, RemotePermissionException;

    /**
     * Returns the resolution date given an issue key
     *
     * @param user     The user making the request
     * @param issueKey the key of the issue being retrieved
     * @return the resolution date or null of the issue
     * @throws RemoteException If the user doesn't have permission to browse the issue or the issue doesn't exist
     * @since 4.0
     */
    Date getResolutionDateByKey(User user, String issueKey) throws RemoteException;

    /**
     * Returns the resolution date given an issue id
     *
     * @param user    The user making the request
     * @param issueId the id of the issue being retrieved
     * @return the resolution date or null of the issue
     * @throws RemoteException If the user doesn't have permission to browse the issue or the issue doesn't exist
     * @since 4.0
     */
    Date getResolutionDateById(User user, Long issueId) throws RemoteException;
}
