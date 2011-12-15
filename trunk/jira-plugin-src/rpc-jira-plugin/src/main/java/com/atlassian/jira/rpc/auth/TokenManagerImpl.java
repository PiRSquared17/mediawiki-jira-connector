/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.auth;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.RandomGenerator;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.security.login.LoginReason;
import com.atlassian.jira.bc.security.login.LoginResult;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.rpc.exception.RemoteAuthenticationException;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;

public class TokenManagerImpl implements TokenManager
{
    public static long DEFAULT_TIMEOUT = 30 * DateUtils.MINUTE_MILLIS;
    private static final Logger log = Logger.getLogger(TokenManagerImpl.class);

    private final Map<String, String> userTokens;
    private PermissionManager permissionManager;
    private final LoginService loginService;
    private final JiraAuthenticationContext authenticationContext;
    private final UserManager userManager;
    private static final String TRUSTED_APPS_TOKEN = "trustedappstoken";

    public TokenManagerImpl(final PermissionManager permissionManager, final LoginService loginService, final JiraAuthenticationContext authenticationContext, final UserManager userManager)
    {
        this(DEFAULT_TIMEOUT, permissionManager, loginService, authenticationContext, userManager);
    }

    TokenManagerImpl(final long timeout, final PermissionManager permissionManager, final LoginService loginService, final JiraAuthenticationContext authenticationContext, final UserManager userManager)
    {
        this.permissionManager = permissionManager;
        this.loginService = loginService;
        this.authenticationContext = authenticationContext;
        this.userManager = userManager;
        userTokens = new TokenMap<String, String>(timeout);
    }

    public String login(String username, String password) throws RemoteException
    {
        //
        // Check if a user is already authenticated say via trusted apps.  If the authentication context is non null then
        // they must have already passed a check and we consider it kosher
        //
        if (authenticationContext.getLoggedInUser() != null)
        {
            log.debug("User '" + authenticationContext.getLoggedInUser().getName() + "' already authenticated, not attempting authentication.");
            //
            // we give off a special token for trusted apps but we don't require it back nor do we ever check it.
            // we do this just to allow people to known it was trusted apps that allowed the login and any debugging will be a
            // tad easier.
            //
            return TRUSTED_APPS_TOKEN;
        }
        User user = userManager.getUserObject(username);
        if (user != null)
        {
            final LoginResult loginResult = loginService.authenticate(user, password);
            // do they require elevated security.  if so then they cant come in via this mechanism
            if (loginResult.getReason() == LoginReason.AUTHENTICATION_DENIED)
            {
                throw new RemoteAuthenticationException("Attempt to log in user '" + username + "' failed. The maximum number of failed login attempts has been reached. Please log into the application through the web interface to reset the number of failed login attempts.");
            }
            if (loginResult.isOK())
            {
                return createToken(user);
            }
        }

        // thrown if user not found, user is null or the password didn't match.
        throw new RemoteAuthenticationException("Invalid username or password.");
    }

    public boolean logout(String token)
    {
        return token == null || (userTokens.remove(token) != null);
    }

    private String createToken(User user) throws RemoteException
    {
        String token;


        int count = 0;
        token = RandomGenerator.randomString(10);
        while (userTokens.containsKey(token) && count++ < 10)
        {
            token = RandomGenerator.randomString(10);
        }
        if (count >= 10)
        {
            throw new RemoteException("Error generating authentication token after 10 attempts?");
        }

        userTokens.put(token, user.getName());

        return token;
    }

    public User retrieveUser(String token) throws RemoteAuthenticationException, RemotePermissionException
    {
        final User user = getUserFromToken(token);
        if (!permissionManager.hasPermission(Permissions.USE, user))
        {
            throw new RemotePermissionException("No permission to perform operation.");
        }

        return user;
    }

    public User retrieveUserNoPermissionCheck(String token)
            throws RemoteAuthenticationException, RemotePermissionException
    {
        if (StringUtils.isBlank(token))
        {
            return null;
        }
        return getUserFromToken(token);

    }

    private User getUserFromToken(final String token) throws RemoteAuthenticationException
    {
        //
        // if we have someone in the authentication context then that trumps any tokens as they must
        // have come in via trusted apps or the like
        //
        User user = authenticationContext.getLoggedInUser();
        if (user != null)
        {
            log.debug("Ignoring token '" + token + "' because user '" + user.getName() + "' is already in the AuthenticationContext.");
            return user;
        }
        // Retrieve user from token
        String userName = userTokens.get(token);
        if (StringUtils.isNotBlank(userName))
        {
            user = userManager.getUserObject(userName);
        }
        if (user == null)
        {
            throw new RemoteAuthenticationException("User not authenticated yet, or session timed out.");
        }
        return user;
    }
}
