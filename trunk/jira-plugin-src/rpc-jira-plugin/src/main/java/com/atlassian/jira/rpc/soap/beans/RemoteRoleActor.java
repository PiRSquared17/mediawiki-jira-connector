package com.atlassian.jira.rpc.soap.beans;

/**
 * Remote version of @see com.atlassian.jira.security.roles.RoleActor
 */
public class RemoteRoleActor
{
    private final RemoteProjectRole remoteProjectRole;
    private final String descriptor;
    private final String type;
    private final String parameter;
    private final RemoteUser[] users;

    public RemoteRoleActor(RemoteProjectRole remoteProjectRole, String descriptor, String type, String parameter, RemoteUser[] users)
    {
        this.remoteProjectRole = remoteProjectRole;
        this.descriptor = descriptor;
        this.type = type;
        this.parameter = parameter;
        this.users = users;
    }

    public RemoteProjectRole getProjectRole()
    {
        return remoteProjectRole;
    }

    public String getDescriptor()
    {
        return descriptor;
    }

    public String getType()
    {
        return type;
    }

    public String getParameter()
    {
        return parameter;
    }

    public RemoteUser[] getUsers()
    {
        return users;
    }

}
