package com.atlassian.jira.rpc.mock;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.util.concurrent.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockUserManager implements UserManager
{
    Map<String, User> userMap = new HashMap<String, User>();

    public int getTotalUserCount()
    {
        return userMap.size();
    }

    public Set<com.opensymphony.user.User> getAllUsers()
    {
        return OSUserConverter.convertToOSUserSet(getUsers());
    }

    public com.opensymphony.user.User getUser(final @Nullable String userName)
    {
        return OSUserConverter.convertToOSUser(userMap.get(userName));
    }

    public User getUserObject(@Nullable final String userName)
    {
        return userMap.get(userName);
    }

    public User findUserInDirectory(final String s, final Long aLong)
    {
        throw new UnsupportedOperationException();
    }

    public User getUserEvenWhenUnknown(final String userName)
    {
        return getUser(userName);
    }

    public Set<com.opensymphony.user.Group> getAllGroups()
    {
        return Collections.emptySet();
    }

    public com.opensymphony.user.Group getGroup(final @Nullable String groupName)
    {
        return null;
    }

    public Group getGroupObject(@Nullable final String s)
    {
        return null;
    }

    public List<Directory> getWritableDirectories()
    {
        throw new UnsupportedOperationException();
    }

    public Directory getDirectory(final Long aLong)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<User> getUsers()
    {
        return userMap.values();
    }

    public Collection<com.atlassian.crowd.embedded.api.Group> getGroups()
    {
        return Collections.emptySet();
    }

    public void addUser(User user)
    {
        userMap.put(user.getName(), user);
    }

    public boolean canUpdateUser(final User user)
    {
        return true;
    }

    public boolean canUpdateUserPassword(final User user)
    {
        return true;
    }

    public boolean canUpdateGroupMembershipForUser(final User user)
    {
        return true;
    }

    public boolean hasPasswordWritableDirectory()
    {
        return false;
    }

    public boolean canDirectoryUpdateUserPassword(final Directory directory)
    {
        return false;
    }
}
