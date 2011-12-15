/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

public abstract class AbstractRemoteConstant extends AbstractNamedRemoteEntity
{
    String description;
    String icon;

    public AbstractRemoteConstant()
    {
    }

    public AbstractRemoteConstant(GenericValue gv)
    {
        super(gv);
        this.description = gv.getString("description");

        this.icon = gv.getString("iconurl");
        if (TextUtils.stringSet(icon) && !this.icon.startsWith("http://"))
        {
            this.icon = ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_BASEURL) + icon;
        }
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    ///CLOVER:OFF
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractRemoteConstant))
        {
            return false;
        }

        final AbstractRemoteConstant abstractRemoteConstant = (AbstractRemoteConstant) o;

        if (description != null ? !description.equals(abstractRemoteConstant.description) : abstractRemoteConstant.description != null)
        {
            return false;
        }
        if (icon != null ? !icon.equals(abstractRemoteConstant.icon) : abstractRemoteConstant.icon != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (description != null ? description.hashCode() : 0);
        result = 29 * result + (icon != null ? icon.hashCode() : 0);
        return result;
    }
}
