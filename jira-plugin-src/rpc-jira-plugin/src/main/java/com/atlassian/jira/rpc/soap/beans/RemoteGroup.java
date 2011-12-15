package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.core.user.UserUtils;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.Group;

/**
 * RemoteGroup
 */
public class RemoteGroup extends RemoteEntity
{
    protected RemoteUser[] users;

    public RemoteGroup()
    {
        //do nothing
    }

    public RemoteGroup(Group group)
    {
        this.name = group.getName();
        this.users = new RemoteUser[group.getUsers().size()];
        for (int i = 0; i < group.getUsers().size(); i++)
        {
            try
            {
                users[i] = new RemoteUser(UserUtils.getUser((String) group.getUsers().get(i)));
            }
            catch (EntityNotFoundException e)
            {
                // TODO log : "EntityNotFoundException getting previously retrieved user, this should never happen";
            }
        }
    }

    public RemoteUser[] getUsers()
    {
        return users;
    }

    public void setUsers(RemoteUser[] users)
    {
        this.users = users;
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
