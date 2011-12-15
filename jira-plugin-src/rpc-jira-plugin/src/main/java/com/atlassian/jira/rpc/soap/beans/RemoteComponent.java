/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.soap.beans;

import org.ofbiz.core.entity.GenericValue;

public class RemoteComponent extends AbstractNamedRemoteEntity
{
    ///CLOVER:OFF
    public RemoteComponent()
    {
    }

    public RemoteComponent(GenericValue gv)
    {
        super(gv);
    }

    public RemoteComponent(String id, String name)
    {
        super(id, name);
    }

}
