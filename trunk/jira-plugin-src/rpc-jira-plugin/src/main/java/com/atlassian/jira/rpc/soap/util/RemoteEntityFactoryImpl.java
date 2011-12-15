package com.atlassian.jira.rpc.soap.util;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.soap.beans.RemoteGroup;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Collection;

/**
 * Default implementation of the {@link RemoteEntityFactory} interface. Provides email address protection to prevent
 * information leakage in RPC land.
 *
 * @since v4.1
 */
public class RemoteEntityFactoryImpl implements RemoteEntityFactory
{
    private final EmailFormatter emailFormatter;
    private final GroupManager groupManager;

    public RemoteEntityFactoryImpl(final EmailFormatter emailFormatter, GroupManager groupManager)
    {
        this.emailFormatter = emailFormatter;
        this.groupManager = groupManager;
    }

    public RemoteUser createUser(final User user)
    {
        Assertions.notNull("user", user);
        final RemoteUser remoteUser = new RemoteUser(user);

        // JRA-19498: set email according to visibility settings
        remoteUser.setEmail(emailFormatter.formatEmail(user.getEmailAddress(), true));
        return remoteUser;
    }

    public RemoteGroup createGroup(final Group group)
    {
        Assertions.notNull("group", group);
        final RemoteGroup remoteGroup = new RemoteGroup();
        remoteGroup.setName(group.getName());

        final Collection<User> usersInGroup = groupManager.getUsersInGroup(group.getName());
        final RemoteUser[] remoteUsers = new RemoteUser[usersInGroup.size()];
        int i = 0;
        for (User user : usersInGroup)
        {
            remoteUsers[i] = createUser(user);
            i++;
        }
        remoteGroup.setUsers(remoteUsers);

        return remoteGroup;
    }
}
