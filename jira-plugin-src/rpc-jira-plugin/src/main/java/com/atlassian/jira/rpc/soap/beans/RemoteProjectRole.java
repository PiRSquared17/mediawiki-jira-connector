package com.atlassian.jira.rpc.soap.beans;

/**
 * Remote version of @see com.atlassian.jira.security.roles.ProjectRole
 */
public class RemoteProjectRole
{
    private Long id = null;
    private String name = null;
    private String description = null;

    public RemoteProjectRole()
    {
    }

    public RemoteProjectRole(Long id, String name, String description)
    {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final RemoteProjectRole that = (RemoteProjectRole) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (name != null ? name.hashCode() : 0);
    }
}
