package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * Simply helps to retrieve issue objects and possible their parent
 */
class IssueRetriever
{
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;

    public IssueRetriever(IssueManager issueManager, PermissionManager permissionManager)
    {
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
    }

    static class IssueInfo
    {
        private final Issue issue;
        private final Issue parentIssue;

        private IssueInfo(Issue issue, Issue parentIssue)
        {
            this.issue = issue;
            this.parentIssue = parentIssue;
        }

        public Issue getIssue()
        {
            return issue;
        }

        public Issue getParentIssue()
        {
            return parentIssue;
        }
    }

    /**
     * Retrieve a single issue via it's database id, throwing an exception if the user has no permissions
     * or the issue does not exist
     *
     * @param issueKey the possible issue key
     * @param user     the user in play
     * @return info about the issue including its possible parent
     * @throws RemotePermissionException if the user cant see the issue in question or it does not exist
     */
    IssueInfo retrieveIssue(String issueKey, User user) throws RemotePermissionException
    {
        MutableIssue issue = issueManager.getIssueObject(issueKey);

        if (issue == null || !permissionManager.hasPermission(Permissions.BROWSE, issue, user))
        {
            throw new RemotePermissionException("This issue does not exist or you don't have permission to view it.");
        }
        Issue parentIssue = retrieveParentIssue(issue, user);
        return new IssueInfo(issue, parentIssue);
    }

    /**
     * Retrieve a single issue via it's database id, throwing an exception if the user has no permissions
     * or the issue does not exist
     *
     * @param issueId the possible issue id
     * @param user    the user in play
     * @return info about the issue including its possible parent
     * @throws RemotePermissionException if the user cant see the issue in question or it does not exist
     */
    IssueInfo retrieveIssue(Long issueId, User user) throws RemotePermissionException
    {
        MutableIssue issue = issueManager.getIssueObject(issueId);

        if (issue == null || !permissionManager.hasPermission(Permissions.BROWSE, issue, user))
        {
            throw new RemotePermissionException("This issue does not exist or you don't have permission to view it.");
        }
        Issue parentIssue = retrieveParentIssue(issue, user);
        return new IssueInfo(issue, parentIssue);
    }

    /**
     * Retrieve a single issue info based on the fact we already have an issue
     *
     * @param issue the issue id
     * @param user  the user in play
     * @return info about the issue including its possible parent
     * @throws RemotePermissionException if the user cant see the issue in question or it does not exist
     */
    IssueInfo retrieveIssue(Issue issue, User user) throws RemotePermissionException
    {
        if (issue == null || !permissionManager.hasPermission(Permissions.BROWSE, issue.getGenericValue(), user))
        {
            throw new RemotePermissionException("This issue does not exist or you don't have permission to view it.");
        }
        Issue parentIssue = retrieveParentIssue(issue, user);
        return new IssueInfo(issue, parentIssue);
    }


    /**
     * Retrieve a single issue via it's database id, throwing an exception if the user has no permissions
     * but not if it does not exist
     *
     * @param issue the possible child issue
     * @param user  the user in play
     * @return an issue or null if its not a sub issue
     * @throws RemotePermissionException if the user cant see the parent issue in question
     */
    private Issue retrieveParentIssue(Issue issue, User user) throws RemotePermissionException
    {
        final Long parentId = issue.getParentId();
        if (parentId == null)
        {
            return null;
        }
        Issue parentIssue = issueManager.getIssueObject(parentId);

        if (parentIssue != null && !permissionManager.hasPermission(Permissions.BROWSE, parentIssue, user))
        {
            throw new RemotePermissionException("This issue does not exist or you don't have permission to view it.");
        }
        return parentIssue;
    }

}
