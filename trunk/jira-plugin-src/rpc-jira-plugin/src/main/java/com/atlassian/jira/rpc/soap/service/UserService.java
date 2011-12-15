/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 9:53:50 AM
 */
package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.soap.beans.RemoteFilter;
import com.atlassian.jira.rpc.soap.beans.RemoteGroup;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;

public interface UserService
{
    RemoteUser getUser(final User currentUser, String username);

    RemoteUser createUser(User admin, String username, String password, String fullName, String email) throws RemoteException, RemoteValidationException, RemotePermissionException;

    void deleteUser(User admin, String username) throws RemoteException, RemoteValidationException, RemotePermissionException;

    RemoteGroup getGroup(User admin, String groupName) throws RemoteException, RemoteValidationException, RemotePermissionException;

    RemoteGroup createGroup(User admin, String groupName, RemoteUser user) throws RemoteException, RemoteValidationException, RemotePermissionException;

    void addUserToGroup(User admin, RemoteGroup group, RemoteUser user) throws RemoteException, RemoteValidationException, RemotePermissionException;

    void removeUserFromGroup(User admin, RemoteGroup group, RemoteUser user) throws RemoteException, RemoteValidationException, RemotePermissionException;

    RemoteGroup updateGroup(User admin, RemoteGroup group) throws RemoteException, RemoteValidationException, RemotePermissionException;

    void deleteGroup(User admin, String groupName, String swapGroupName) throws RemoteException, RemoteValidationException, RemotePermissionException;

    /**
     * Returns a list of the passed in users favourite filters.
     *
     * @param user The user whose fitlers to retreive
     *
     * @return A list of filters that the ser a favourited.
     *
     * @throws RemoteException if a remote exception happens.
     */
    RemoteFilter[] getFavouriteFilters(User user) throws RemoteException;

    RemoteUser updateUser(User admin, RemoteUser remoteUser) throws RemoteValidationException, RemoteException;

    RemoteUser setUserPassword(User admin, RemoteUser remoteUser, String newPassword) throws RemoteValidationException, RemoteException;
}