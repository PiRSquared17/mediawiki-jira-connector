package com.atlassian.jira.rpc.auth;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.exception.RemoteAuthenticationException;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;

public interface TokenManager
{
    String login(String username, String password) throws RemoteException, RemoteAuthenticationException;

    boolean logout(String token);

    /**
     * Retrieve the user, checking that the token is valid, and that the user has the 'USE' permission
     *
     * @param token
     * @return A valid user.  Note that this method will never return null
     * @throws RemoteAuthenticationException If the token is not valid, or if it has timed out
     * @throws RemotePermissionException If the user does not have the 'USE' permission.
     * @deprecated As some instances may want to allow anonymous access, individual methods should check for their
     *             relevant permission instead.  Use {@link #retrieveUserNoPermissionCheck(String)} instead.
     */
    // @todo - these need to be revisited as part of JRA-7858
    User retrieveUser(String token) throws RemoteAuthenticationException, RemotePermissionException;

    // @todo - these need to be revisited as part of JRA-7858
    User retrieveUserNoPermissionCheck(String token) throws RemoteAuthenticationException, RemotePermissionException;
}
