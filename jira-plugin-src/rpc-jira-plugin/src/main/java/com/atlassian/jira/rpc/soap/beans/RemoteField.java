package com.atlassian.jira.rpc.soap.beans;

public class RemoteField extends AbstractNamedRemoteEntity
{
    public RemoteField(String id, String name)
    {
        setId(id);
        setName(name);
    }
}
