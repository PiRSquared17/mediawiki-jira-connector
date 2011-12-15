package com.atlassian.jira.rpc.soap.beans;


/**
 * RemotePermission
 */
public class RemotePermission
{
    protected Long permission;
    protected String name;

    public RemotePermission()
    {
        //do nothing
    }

    public RemotePermission(Long permission, String name)
    {
        this.permission = permission;
        this.name = name;
    }

    public Long getPermission()
    {
        return permission;
    }

    public void setPermission(Long permission)
    {
        this.permission = permission;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RemotePermission))
        {
            return false;
        }

        final RemotePermission remotePermission = (RemotePermission) o;

        if (name != null ? !name.equals(remotePermission.name) : remotePermission.name != null)
        {
            return false;
        }
        if (permission != null ? !permission.equals(remotePermission.permission) : remotePermission.permission != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (permission != null ? permission.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
