package com.atlassian.jira.rpc.soap.util;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.soap.beans.RemoteGroup;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;

/**
 * An interface for the creation of {@link com.atlassian.jira.rpc.soap.beans.RemoteEntity} objects based on their
 * real OSUser counterparts. This factory should perform extra steps to ensure that the remote entities are complete in
 * their representation, such as masking or hiding email addresses in {@link RemoteUser} objects depending on system state.
 *
 * @since v4.1
 */
public interface RemoteEntityFactory
{
    /**
     * Returns a RemoteUser based on the OSUser implementation.
     *
     * @param user real user; must not be null.
     * @return a RemoteUser bean
     */
    RemoteUser createUser(User user);

    /**
     * Returns a RemoteGroup based on the OSUser implementation.
     *
     * @param group real group; must not be null.
     * @return a RemoteGroup bean
     */
    RemoteGroup createGroup(Group group);
}
