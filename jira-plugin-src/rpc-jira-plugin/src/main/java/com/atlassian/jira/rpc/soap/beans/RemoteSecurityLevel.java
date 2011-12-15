package com.atlassian.jira.rpc.soap.beans;

import org.ofbiz.core.entity.GenericValue;

/**
 * This represents issue's security level entity
 *
 * @since v3.13
 */
public class RemoteSecurityLevel extends AbstractNamedRemoteEntity
{
    String description;

    // TODO Do we need this?
    public RemoteSecurityLevel()
    {
    }

    public RemoteSecurityLevel(final GenericValue gv)
    {
        super(gv);
        this.description = gv.getString("description");
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

}
