package com.atlassian.jira.rpc.auth;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.security.login.LoginReason;
import com.atlassian.jira.bc.security.login.LoginResult;
import com.atlassian.jira.bc.security.login.LoginResultImpl;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.rpc.exception.RemoteAuthenticationException;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.mock.MockUser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.multitenant.MultiTenantContext;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.ImmutableException;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestTokenManagerImpl extends TestCase
{
    private PermissionManager permissionManager;
    private LoginService loginService;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private UserManager userManager;
    private User bob;

    protected void setUp() throws Exception
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
        permissionManager = EasyMock.createMock(PermissionManager.class);
        loginService = EasyMock.createMock(LoginService.class);
        jiraAuthenticationContext = EasyMock.createMock(JiraAuthenticationContext.class);
        userManager = EasyMock.createMock(UserManager.class);
        bob = OSUserConverter.convertToOSUser(new MockUser("bob"));
    }

    @Override
    protected void tearDown() throws Exception
    {
        verify(permissionManager, loginService, userManager, jiraAuthenticationContext);
        MultiTenantContext.setFactory(null);
    }

    private TokenManager instantiateTokenManager()
    {
        replay(permissionManager, loginService, userManager, jiraAuthenticationContext);
        return new TokenManagerImpl(permissionManager, loginService, jiraAuthenticationContext, userManager);
    }

    public void testLogin_FAIL() throws RemoteException, ImmutableException, DuplicateEntityException
    {

        final LoginResult loginResultFAIL = new LoginResultImpl(LoginReason.AUTHENTICATED_FAILED, null, "bob");

        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(null);
        expect(userManager.getUserObject("bob")).andStubReturn(bob);
        expect(loginService.authenticate(bob, "badpass")).andStubReturn(loginResultFAIL);
        expect(permissionManager.hasPermission(Permissions.USE, bob)).andStubReturn(true);

        TokenManager tokenManager = instantiateTokenManager();

        // invalid login
        try
        {
            tokenManager.login("bob", "badpass");
            fail("Should have barfed.");
        }
        catch (RemoteAuthenticationException e)
        {
            assertTrue(e.getMessage().contains("Invalid username or password"));
        }
    }

    public void testLogin_FAIL_for_unknown_user() throws RemoteException, ImmutableException, DuplicateEntityException
    {
        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(null);
        expect(userManager.getUserObject("bob")).andStubReturn(null);

        TokenManager tokenManager = instantiateTokenManager();

        // invalid login
        try
        {
            tokenManager.login("bob", "badpass");
            fail("Should have barfed.");
        }
        catch (RemoteAuthenticationException e)
        {
            assertTrue(e.getMessage().contains("Invalid username or password"));
        }
    }

    public void testLogin_FAIL_for_ElevateSecurity()
            throws RemoteException, ImmutableException, DuplicateEntityException
    {
        final LoginResult loginResultFAIL = new LoginResultImpl(LoginReason.AUTHENTICATION_DENIED, null, "bob");

        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(null);
        expect(userManager.getUserObject("bob")).andStubReturn(bob);
        expect(loginService.authenticate(bob, "badpass")).andStubReturn(loginResultFAIL);
        expect(permissionManager.hasPermission(Permissions.USE, bob)).andStubReturn(true);

        TokenManager tokenManager = instantiateTokenManager();

        // invalid login
        try
        {
            tokenManager.login("bob", "badpass");
            fail("Should have barfed.");
        }
        catch (RemoteAuthenticationException e)
        {
            assertTrue(e.getMessage().contains("The maximum number of failed login attempts has been reached. Please log into the application through the web interface to reset the number of failed login attempts."));
        }
    }

    public void testLogin_OK_inContext()
            throws RemoteException, ImmutableException, DuplicateEntityException
    {
        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(bob);

        TokenManager tokenManager = instantiateTokenManager();

        String token = tokenManager.login("bob", "badpass");
        assertEquals("trustedappstoken", token);
    }

    public void testLoginRetrieveLogout() throws RemoteException, ImmutableException, DuplicateEntityException
    {

        final LoginResult loginResultOK = new LoginResultImpl(LoginReason.OK, null, "bob");

        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(null);
        expect(userManager.getUserObject("bob")).andStubReturn(bob);
        expect(loginService.authenticate(bob, "password")).andStubReturn(loginResultOK);
        expect(permissionManager.hasPermission(Permissions.USE, bob)).andStubReturn(true);

        TokenManager tokenManager = instantiateTokenManager();

        String token = tokenManager.login("bob", "password");

        assertEquals(bob, tokenManager.retrieveUser(token));

        assertTrue(tokenManager.logout(token));
        assertFalse(tokenManager.logout("badtoken"));
        assertFalse(tokenManager.logout(token)); // check we can't logout twice
        assertTrue(tokenManager.logout(null));
    }

    public void testRetrieveUser_UnknownToken() throws RemoteException
    {
        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(null);
        TokenManager tokenManager = instantiateTokenManager();
        try
        {
            tokenManager.retrieveUser("badtoken");
            fail("Should have barfed.");
        }
        catch (RemoteAuthenticationException e)
        {
        }
    }

    public void testRetrieveUser_NullToken() throws RemoteException
    {
        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(null);
        TokenManager tokenManager = instantiateTokenManager();
        try
        {
            tokenManager.retrieveUser(null);
            fail("Should have barfed.");
        }
        catch (RemoteAuthenticationException e)
        {
        }
    }

    public void testRetrieveUser_WithNoPermission() throws RemoteException, ImmutableException, DuplicateEntityException
    {
        final LoginResult loginResult = new LoginResultImpl(LoginReason.OK, null, "bob");

        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(null);
        expect(userManager.getUserObject("bob")).andStubReturn(bob);
        expect(loginService.authenticate(bob, "password")).andStubReturn(loginResult);
        expect(permissionManager.hasPermission(Permissions.USE, bob)).andStubReturn(false);

        TokenManager tokenManager = instantiateTokenManager();
        try
        {
            String token = tokenManager.login("bob", "password");
            tokenManager.retrieveUser(token);
            fail("Should have barfed.");
        }
        catch (RemotePermissionException e)
        {
        }
    }

    public void testRetrieveUser_OK() throws RemoteException, ImmutableException, DuplicateEntityException
    {
        final LoginResult loginResult = new LoginResultImpl(LoginReason.OK, null, "bob");

        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(null);
        expect(userManager.getUserObject("bob")).andStubReturn(bob);
        expect(loginService.authenticate(bob, "password")).andStubReturn(loginResult);
        expect(permissionManager.hasPermission(Permissions.USE, bob)).andStubReturn(true);

        TokenManager tokenManager = instantiateTokenManager();
        String token = tokenManager.login("bob", "password");
        User actual = tokenManager.retrieveUser(token);
        assertEquals(bob.getName(), actual.getName());
    }

    public void testRetrieveUser_FAIL_noPermission_fromAuthContext()
            throws RemoteException, ImmutableException, DuplicateEntityException
    {
        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(bob);
        expect(permissionManager.hasPermission(Permissions.USE, bob)).andStubReturn(false);

        TokenManager tokenManager = instantiateTokenManager();
        String token = tokenManager.login("bob", "password");
        try
        {
            tokenManager.retrieveUser(token);
            fail("Should have barfed");
        }
        catch (RemotePermissionException ignored)
        {
        }
    }

    public void testRetrieveUser_OK_butfromAuthContext()
            throws RemoteException, ImmutableException, DuplicateEntityException
    {
        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(bob);
        expect(permissionManager.hasPermission(Permissions.USE, bob)).andStubReturn(true);

        TokenManager tokenManager = instantiateTokenManager();
        String token = tokenManager.login("bob", "password");
        User actual = tokenManager.retrieveUser(token);
        assertEquals(bob.getName(), actual.getName());
    }

    public void testRetrieveUserNoPermissions_NullToken()
            throws RemoteException, ImmutableException, DuplicateEntityException
    {
        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(null);

        TokenManager tokenManager = instantiateTokenManager();
        assertNull(tokenManager.retrieveUserNoPermissionCheck(null));
    }

    public void testRetrieveUserNoPermissions_BadToken()
            throws RemoteException, ImmutableException, DuplicateEntityException
    {
        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(null);

        TokenManager tokenManager = instantiateTokenManager();
        try
        {
            tokenManager.retrieveUserNoPermissionCheck("badtoken");
            fail("Should have barfed");
        }
        catch (RemoteAuthenticationException e)
        {
        }
    }

    public void testRetrieveUserNoPermissions_OK() throws RemoteException, ImmutableException, DuplicateEntityException
    {
        final LoginResult loginResult = new LoginResultImpl(LoginReason.OK, null, "bob");

        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(null);
        expect(userManager.getUserObject("bob")).andStubReturn(bob);
        expect(loginService.authenticate(bob, "password")).andStubReturn(loginResult);
        expect(permissionManager.hasPermission(Permissions.USE, bob)).andStubReturn(false);

        TokenManager tokenManager = instantiateTokenManager();
        String token = tokenManager.login("bob", "password");
        final User actualUser = tokenManager.retrieveUserNoPermissionCheck(token);
        assertEquals(bob.getName(), actualUser.getName());
    }

    public void testRetrieveUserNoPermissions_OK_FromAuthContext() throws RemoteException, ImmutableException, DuplicateEntityException
    {
        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(bob);

        TokenManager tokenManager = instantiateTokenManager();
        String token = tokenManager.login("bob", "password");
        final User actualUser = tokenManager.retrieveUserNoPermissionCheck(token);
        assertEquals(bob.getName(), actualUser.getName());
    }
}
