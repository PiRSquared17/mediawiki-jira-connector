package com.atlassian.jira.rpc.soap.beans;

/**
 * RemotePermissionMapping
 */
public class RemotePermissionMapping
{
    protected RemotePermission permission;
    protected RemoteEntity[] remoteEntities;

    public RemotePermissionMapping()
    {
        //do nothing
    }

    public RemotePermissionMapping(RemotePermission permission, RemoteEntity[] remoteEntities)
    {
        this.permission = permission;
        this.remoteEntities = remoteEntities;
    }

    public RemotePermission getPermission()
    {
        return permission;
    }

    public void setPermission(RemotePermission permission)
    {
        this.permission = permission;
    }

    public RemoteEntity[] getRemoteEntities()
    {
        return remoteEntities;
    }

    public void setRemoteEntities(RemoteEntity[] remoteEntities)
    {
        this.remoteEntities = remoteEntities;
    }
}
