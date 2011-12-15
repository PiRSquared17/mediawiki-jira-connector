/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.crowd.embedded.api.User;


public class RemoteUser extends RemoteEntity
{
    String fullname;
    String email;

    public RemoteUser()
    {
        //do nothing
    }

    public RemoteUser(User user)
    {
        this.name = user.getName();
        this.fullname = user.getDisplayName();
        this.email = user.getEmailAddress();
    }

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    ///CLOVER:OFF
    @SuppressWarnings ({ "RedundantIfStatement" })
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RemoteUser))
        {
            return false;
        }

        final RemoteUser remoteUser = (RemoteUser) o;

        if (email != null ? !email.equals(remoteUser.email) : remoteUser.email != null)
        {
            return false;
        }
        if (fullname != null ? !fullname.equals(remoteUser.fullname) : remoteUser.fullname != null)
        {
            return false;
        }
        if (name != null ? !name.equals(remoteUser.name) : remoteUser.name != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 29 * result + (fullname != null ? fullname.hashCode() : 0);
        result = 29 * result + (email != null ? email.hashCode() : 0);
        return result;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
