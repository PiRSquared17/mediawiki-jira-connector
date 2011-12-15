/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.soap.beans;

import org.ofbiz.core.entity.GenericValue;

// @todo rename this to be BasenamedRemoteEntity
public abstract class AbstractNamedRemoteEntity extends AbstractRemoteEntity
{
    String name;

    public AbstractNamedRemoteEntity()
    {
    }

    public AbstractNamedRemoteEntity(GenericValue gv)
    {
        super(gv);
        this.name = gv.getString("name");
    }

    public AbstractNamedRemoteEntity(String id, String name)
    {
        setId(id);
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    ///CLOVER:OFF
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractNamedRemoteEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        final AbstractNamedRemoteEntity abstractNamedRemoteEntity = (AbstractNamedRemoteEntity) o;

        if (name != null ? !name.equals(abstractNamedRemoteEntity.name) : abstractNamedRemoteEntity.name != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 29 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return "Name: " + name;
    }

}
