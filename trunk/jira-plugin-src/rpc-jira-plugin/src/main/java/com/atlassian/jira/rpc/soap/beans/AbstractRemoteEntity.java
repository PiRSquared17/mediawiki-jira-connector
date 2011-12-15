/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 9, 2004
 * Time: 12:24:50 PM
 */
package com.atlassian.jira.rpc.soap.beans;

import org.ofbiz.core.entity.GenericValue;

public abstract class AbstractRemoteEntity
{
    String id;

    public AbstractRemoteEntity()
    {
    }

    public AbstractRemoteEntity(GenericValue gv)
    {
        this(gv.getString("id"));
    }

    public AbstractRemoteEntity(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    ///CLOVER:OFF
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractRemoteEntity))
        {
            return false;
        }

        final AbstractRemoteEntity abstractRemoteEntity = (AbstractRemoteEntity) o;

        if (id != null ? !id.equals(abstractRemoteEntity.id) : abstractRemoteEntity.id != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (id != null ? id.hashCode() : 0);
    }
}