package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.soap.beans.RemoteIssueType;
import com.atlassian.jira.rpc.soap.beans.RemotePriority;
import com.atlassian.jira.rpc.soap.beans.RemoteResolution;
import com.atlassian.jira.rpc.soap.beans.RemoteStatus;
import com.atlassian.jira.rpc.soap.util.SoapUtils;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * Handles issue constants and permission checks related to accessing them
 */
public class IssueConstantsServiceImpl implements IssueConstantsService
{
    private ConstantsManager constantsManager;
    private PermissionManager permissionManager;
    private IssueTypeSchemeManager issueTypeSchemeManager;
    private final ProjectManager projectManager;

    public IssueConstantsServiceImpl(ConstantsManager constantsManager, PermissionManager permissionManager, IssueTypeSchemeManager issueTypeSchemeManager, ProjectManager projectManager)
    {
        this.constantsManager = constantsManager;
        this.permissionManager = permissionManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.projectManager = projectManager;
    }

    public RemoteIssueType[] getIssueTypesForProject(User user, String projectId) throws RemotePermissionException
    {
        if (projectId != null)
        {
            Project project = projectManager.getProjectObj(new Long(projectId));
            if (project != null)
            {
                confirmUserHasBrowseProjectPermission(user, project);
                return SoapUtils.getIssueTypeObjects(issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project));
            }
        }
        return new RemoteIssueType[0];
    }

    public RemoteIssueType[] getSubTaskIssueTypesForProject(User user, String projectId) throws RemotePermissionException
    {
        if (projectId != null)
        {
            Project project = projectManager.getProjectObj(new Long(projectId));
            if (project != null)
            {
                confirmUserHasBrowseProjectPermission(user, project);
                return SoapUtils.getIssueTypeObjects(issueTypeSchemeManager.getSubTaskIssueTypesForProject(project));
            }
        }
        return new RemoteIssueType[0];
    }

    public RemoteIssueType[] getIssueTypes(User user) throws RemotePermissionException
    {
        confirmUserHasABrowseProjectPermission(user);
        return SoapUtils.getIssueTypes(constantsManager.getIssueTypes());
    }

    public RemoteIssueType[] getSubTaskIssueTypes(User user) throws RemotePermissionException
    {
        confirmUserHasABrowseProjectPermission(user);
        return SoapUtils.getIssueTypes(constantsManager.getSubTaskIssueTypes());
    }

    public RemotePriority[] getPriorities(User user) throws RemotePermissionException
    {
        confirmUserHasABrowseProjectPermission(user);
        return SoapUtils.getPriorities(constantsManager.getPriorities());
    }

    public RemoteStatus[] getStatuses(User user) throws RemotePermissionException
    {
        confirmUserHasABrowseProjectPermission(user);
        return SoapUtils.getStatuses(constantsManager.getStatuses());
    }

    public RemoteResolution[] getResolutions(User user) throws RemotePermissionException
    {
        confirmUserHasABrowseProjectPermission(user);
        return SoapUtils.getResolutions(constantsManager.getResolutions());
    }

    private void confirmUserHasABrowseProjectPermission(User user) throws RemotePermissionException
    {
        try
        {
            if (!permissionManager.hasProjects(Permissions.BROWSE, user))
            {
                throw new RemotePermissionException("The user " + user.getName() + " does not have the Browse Project permission for any project in JIRA");
            }
        }
        catch (Exception e)
        {
            throw new RemotePermissionException(e.getMessage());
        }
    }

    private void confirmUserHasBrowseProjectPermission(User user, Project project) throws RemotePermissionException
    {
        try
        {
            if (!permissionManager.hasPermission(Permissions.BROWSE, project, user))
            {
                throw new RemotePermissionException("The user '" + (user == null ? "null" : user.getName()) + "' does not have the Browse Project permission for project '" + (project == null ? "null" : project.getName()) + "'");
            }
        }
        catch (Exception e)
        {
            throw new RemotePermissionException(e.getMessage());
        }
    }
}
