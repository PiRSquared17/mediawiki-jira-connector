package com.atlassian.jira.rpc.soap.beans;

import org.ofbiz.core.entity.GenericValue;

/**
 * RemoteScheme
 */
public class RemoteScheme
{
    protected Long id;
    protected String name;
    protected String description;
    protected String type;

    ///CLOVER:OFF
    public RemoteScheme()
    {
        super();
    }

    public RemoteScheme(GenericValue scheme, String type)
    {
        this.id = scheme.getLong("id");
        this.name = scheme.getString("name");
        this.description = scheme.getString("description");
        this.type = type;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
