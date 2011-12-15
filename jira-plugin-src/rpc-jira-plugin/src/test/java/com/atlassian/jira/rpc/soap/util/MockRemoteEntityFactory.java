package com.atlassian.jira.rpc.soap.util;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.soap.beans.RemoteGroup;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;
import com.atlassian.jira.security.groups.GroupManager;

import java.util.Collection;

/**
 * Mock implementation of the Remote Entity Factory for testing purposes.
 *
 * @since v4.1
 */
public class MockRemoteEntityFactory implements RemoteEntityFactory
{
    private GroupManager groupManager;

    public MockRemoteEntityFactory()
    {}

    public MockRemoteEntityFactory(GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }


    public RemoteUser createUser(final User user)
    {
        return new RemoteUser(user);
    }

    public RemoteGroup createGroup(final Group group)
    {
        RemoteGroup remoteGroup = new RemoteGroup();
        remoteGroup.setName(group.getName());

        // Add in members if we have a GroupManager
        if (groupManager != null)
        {
            final Collection<User> usersInGroup = groupManager.getUsersInGroup(group.getName());
            final RemoteUser[] remoteUsers = new RemoteUser[usersInGroup.size()];
            int i = 0;
            for (User user : usersInGroup)
            {
                remoteUsers[i] = createUser(user);
                i++;
            }
            remoteGroup.setUsers(remoteUsers);
        }

        return remoteGroup;        
    }
}
