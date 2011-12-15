/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.soap.beans;

import org.ofbiz.core.entity.GenericValue;

public class RemoteIssueType extends AbstractRemoteConstant
{
    boolean isSubTask;

    ///CLOVER:OFF
    public RemoteIssueType()
    {
    }

    public RemoteIssueType(GenericValue gv, boolean isSubTask)
    {
        super(gv);
        this.isSubTask = isSubTask;
    }

    public boolean isSubTask()
    {
        return isSubTask;
    }

    public void setSubTask(boolean subTask)
    {
        isSubTask = subTask;
    }
}
