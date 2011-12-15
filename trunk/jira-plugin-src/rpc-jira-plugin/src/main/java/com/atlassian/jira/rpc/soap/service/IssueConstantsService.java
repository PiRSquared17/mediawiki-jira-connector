package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.soap.beans.RemoteIssueType;
import com.atlassian.jira.rpc.soap.beans.RemotePriority;
import com.atlassian.jira.rpc.soap.beans.RemoteResolution;
import com.atlassian.jira.rpc.soap.beans.RemoteStatus;

/**
 * Deals with issue constants.
 */
public interface IssueConstantsService
{
    public RemoteIssueType[] getIssueTypesForProject(User user, String projectId) throws RemotePermissionException;

    public RemoteIssueType[] getSubTaskIssueTypesForProject(User user, String projectId) throws RemotePermissionException;

    public RemoteIssueType[] getIssueTypes(User user) throws RemotePermissionException;

    public RemoteIssueType[] getSubTaskIssueTypes(User user) throws RemotePermissionException;

    public RemotePriority[] getPriorities(User user) throws RemotePermissionException;

    public RemoteStatus[] getStatuses(User user) throws RemotePermissionException;

    public RemoteResolution[] getResolutions(User user) throws RemotePermissionException;
}
