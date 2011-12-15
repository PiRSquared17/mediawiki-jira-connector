package com.atlassian.jira.rpc.soap.beans;

public class RemoteFieldValue
{
    String id;
    String[] values;

    public RemoteFieldValue()
    {
    }

    public RemoteFieldValue(String id, String[] values)
    {
        this.id = id;
        this.values = values;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String[] getValues()
    {
        return values;
    }

    public void setValues(String[] values)
    {
        this.values = values;
    }
}
