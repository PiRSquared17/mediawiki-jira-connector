package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.group.GroupService;
import com.atlassian.jira.bc.user.UserServiceResultHelper;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.mock.MockUser;
import com.atlassian.jira.rpc.soap.util.MockRemoteEntityFactory;
import com.atlassian.jira.rpc.soap.util.RemoteEntityFactory;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.easymock.EasyMock;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;

import java.util.Collections;

public class TestUserServiceImplWithExternalManagement extends MockObjectTestCase
{
    private Mock mockSRM;
    private Mock mockGroupManager;
    private Mock mockPermissionManager;
    private Mock mockApplicationProperties;
    private Mock mockGroupService;
    private Mock mockUserService;

    private UserService service;
    private User user;
    private User user2;
    private User user3;
    private CrowdService crowdService;
    private GlobalPermissionManager globalPermissionManager;
    private UserUtil userUtil;

    protected void setUp() throws Exception
    {
        super.setUp();
        mockSRM = new Mock(SearchRequestService.class);
        mockPermissionManager = new Mock(PermissionManager.class);
        mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockGroupManager = new Mock(GroupManager.class);
        mockGroupService = new Mock(GroupService.class);
        mockUserService = new Mock(com.atlassian.jira.bc.user.UserService.class);
        final RemoteEntityFactory mockRemoteEntityFactory = new MockRemoteEntityFactory();

        user = new MockUser("driver", "Test User", "test@test.com");
        user2 = new MockUser("driver2", "Test User", "test@test.com");
        user3 = new MockUser("driver3", "Test User", "test@test.com");

        Group group = new ImmutableGroup("jira-test");

        crowdService = EasyMock.createMock(CrowdService.class);
        globalPermissionManager = EasyMock.createMock(GlobalPermissionManager.class);
        userUtil = EasyMock.createMock(UserUtil.class);


        service = new UserServiceImpl((SearchRequestService) mockSRM.proxy(),
                null,
                (GroupManager) mockGroupManager.proxy(),
                (PermissionManager) mockPermissionManager.proxy(),
                (ApplicationProperties) mockApplicationProperties.proxy(),
                (com.atlassian.jira.bc.user.UserService) mockUserService.proxy(),
                (GroupService) mockGroupService.proxy(),
                mockRemoteEntityFactory, crowdService, globalPermissionManager, userUtil);
    }

    public void testCreateUserWithExternalUserManagement()
    {
        com.atlassian.jira.bc.user.UserService.CreateUserValidationResult result =
                UserServiceResultHelper.getCreateUserValidationResult(
                        "testuser",
                        "testpassword",
                        "user@email.com",
                        "first last");
        mockUserService.expects(once()).method("validateCreateUserForAdminPasswordRequired").with(
                new Constraint[] { eq(user),
                                   eq("testuser"),
                                   eq("testpassword"),
                                   eq("testpassword"),
                                   eq("user@email.com"),
                                   eq("first last")
                }).will(returnValue(result));
        mockUserService.expects(once()).method("createUserWithNotification").with(eq(result)).will(returnValue(user));

        try
        {
            service.createUser(user, "testuser", "testpassword", "first last", "user@email.com");
        }
        catch (RemoteException e)
        {
            fail("failed creating user without ext mgmt turned on");
        }

        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("content");
        com.atlassian.jira.bc.user.UserService.CreateUserValidationResult errorResult =
                UserServiceResultHelper.getCreateUserValidationResult(errors);
        mockUserService.expects(once()).method("validateCreateUserForAdminPasswordRequired").with(
                new Constraint[] { eq(user),
                                   eq("testuser1"),
                                   eq("testpassword"),
                                   eq("testpassword"),
                                   eq("user@email.com"),
                                   eq("first last")
                }).will(returnValue(errorResult));

        try
        {
            service.createUser(user, "testuser1", "testpassword", "first last", "user@email.com");
            fail("ext mgmt is turned on. Creation of user should have failed.");
        }
        catch (RemoteException e)
        {

        }

        mockUserService.verify();
    }

    public void testDeleteUserWithExternalUserManagement()
    {
        com.atlassian.jira.bc.user.UserService.DeleteUserValidationResult result =
                UserServiceResultHelper.getDeleteUserValidationResult(user2);
        mockUserService.expects(once()).method("validateDeleteUser").with(eq(user), eq(user2.getName())).will(returnValue(result));
        mockUserService.expects(once()).method("removeUser").with(eq(user), eq(result));

        try
        {
            service.deleteUser(user, user2.getName());
        }
        catch (RemoteException e)
        {
            fail("failed deleting user with ext mgmt turned off");
        }

        // just checking error - real extranal managemet is tested in UserUtil
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("error message");
        result = UserServiceResultHelper.getDeleteUserValidationResult(errors);
        mockUserService.expects(once()).method("validateDeleteUser").with(eq(user), eq(user3.getName())).will(returnValue(result));

        try
        {
            service.deleteUser(user, user3.getName());
            fail("ext mgmt is turned on. Deletion of user should have failed.");
        }
        catch (RemoteException e)
        {

        }
    }

    public void testCreateGroupWithExternalUserManagement()
    {
        mockGroupManager.expects(once()).method("createGroup").with(eq("test-group")).will(returnValue(new ImmutableGroup("test-group")));
        mockPermissionManager.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));
        mockApplicationProperties.expects(atLeastOnce()).method("getOption").with(eq(APKeys.JIRA_OPTION_USER_EXTERNALMGT)).will(returnValue(false));


        Mock mockGlobalPermissionManager = new Mock(GlobalPermissionManager.class);
        mockGlobalPermissionManager.expects(atLeastOnce()).method("getGroups").with(eq(Permissions.USE)).will(returnValue(Collections.EMPTY_LIST));

        try
        {
            service.createGroup(user, "test-group", null);
        }
        catch (RemoteException e)
        {
            fail("failed creating group without ext mgmt turned on: " + e.getMessage());
        }
        mockApplicationProperties.expects(atLeastOnce()).method("getOption").with(eq(APKeys.JIRA_OPTION_USER_EXTERNALMGT)).will(returnValue(true));

        try
        {
            service.createGroup(user, "testgroup2", null);
            fail("ext mgmt is turned on. Creation of group should have failed.");
        }
        catch (RemoteException e)
        {

        }

        mockGroupManager.verify();
    }

}
