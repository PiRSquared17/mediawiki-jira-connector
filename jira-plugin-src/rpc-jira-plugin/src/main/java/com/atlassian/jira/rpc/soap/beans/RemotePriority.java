/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.soap.beans;

import org.ofbiz.core.entity.GenericValue;

public class RemotePriority extends AbstractRemoteConstant
{
    private String color;

    public RemotePriority()
    {
    }

    public RemotePriority(GenericValue gv)
    {
        super(gv);
        this.color = gv.getString("statusColor");
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }
}
