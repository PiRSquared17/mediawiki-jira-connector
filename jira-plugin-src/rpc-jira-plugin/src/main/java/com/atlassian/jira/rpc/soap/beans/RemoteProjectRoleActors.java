package com.atlassian.jira.rpc.soap.beans;

/**
 * Remote version of @see com.atlassian.jira.security.roles.ProjectRoleActors
 */
public class RemoteProjectRoleActors extends RemoteRoleActors
{
    private RemoteProject project;

    public RemoteProjectRoleActors(RemoteProject project, RemoteProjectRole projectRole, RemoteRoleActor[] roleActors)
    {
        super(projectRole, roleActors);
        this.project = project;
    }

    public RemoteProject getProject()
    {
        return project;
    }
}
