package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.group.GroupService;
import com.atlassian.jira.bc.user.UserServiceResultHelper;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.mock.MockUserManager;
import com.atlassian.jira.rpc.soap.beans.RemoteGroup;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;
import com.atlassian.jira.rpc.soap.util.MockRemoteEntityFactory;
import com.atlassian.jira.rpc.soap.util.RemoteEntityFactory;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.multitenant.MultiTenantContext;
import com.opensymphony.user.UserManager;
import org.easymock.EasyMock;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;

import java.util.Collections;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

public class TestUserServiceImpl extends MockObjectTestCase
{
    private Mock mockSRS;
    private MockUserManager mockUserManager;
    private Mock mockGroupManager;
    private Mock mockPermissionManager;
    private Mock mockApplicationProperties;
    private Mock mockGroupService;

    private RemoteEntityFactory mockRemoteEntityFactory;

    private UserService service;
    private User user;
    private Mock mockUserService;
    private CrowdService crowdService;
    private GlobalPermissionManager globalPermissionManager;
    private UserUtil userUtil;
    private User updatedUser;

    protected void setUp() throws Exception
    {
        super.setUp();
        MultiTenantContextTestUtils.setupMultiTenantSystem();
        mockSRS = new Mock(SearchRequestService.class);
        mockUserManager = new MockUserManager();
        mockGroupManager = new Mock(GroupManager.class);
        mockPermissionManager = new Mock(PermissionManager.class);
        mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockGroupService = new Mock(GroupService.class);
        mockUserService = new Mock(com.atlassian.jira.bc.user.UserService.class);
        mockRemoteEntityFactory = new MockRemoteEntityFactory((GroupManager) mockGroupManager.proxy());

        user = ImmutableUser.newUser().name("driver").emailAddress("test@test.com").displayName("Test User").toUser();
        updatedUser = ImmutableUser.newUser().name("updatedUser").emailAddress("updated@test.com").displayName("Updated User").toUser();

        crowdService = createMock(CrowdService.class);
        globalPermissionManager = createMock(GlobalPermissionManager.class);
        userUtil = createMock(UserUtil.class);

        service = new UserServiceImpl((SearchRequestService) mockSRS.proxy(),
                mockUserManager,
                (GroupManager) mockGroupManager.proxy(),
                (PermissionManager) mockPermissionManager.proxy(),
                (ApplicationProperties) mockApplicationProperties.proxy(),
                (com.atlassian.jira.bc.user.UserService) mockUserService.proxy(),
                (GroupService) mockGroupService.proxy(),
                mockRemoteEntityFactory, crowdService, globalPermissionManager, userUtil);
    }

    protected void tearDown()
    {
        UserManager.reset();
        MultiTenantContext.setFactory(null);
    }

    public void testGetUser() throws Exception
    {
        User driver = ImmutableUser.newUser().name("driver").emailAddress("driver@example.com").displayName("Mr Bus Driver").toUser();
        mockUserManager.addUser(driver);

        assertNull(service.getUser(driver, "noone"));
        RemoteUser user = service.getUser(driver, "driver");
        assertEquals("driver", user.getName());
        assertEquals("Mr Bus Driver", user.getFullname());
        assertEquals("driver@example.com", user.getEmail());
    }

    public void testGetFavouriteFilters() throws Exception
    {
        final MockControl mockSearchRequestControl = MockClassControl.createControl(SearchRequest.class);
        final SearchRequest mockSearchRequest = (SearchRequest) mockSearchRequestControl.getMock();
        mockSearchRequest.getId();
        mockSearchRequestControl.setReturnValue(new Long(10));
        mockSearchRequest.getName();
        mockSearchRequestControl.setReturnValue("Test Filter");
        mockSearchRequest.getDescription();
        mockSearchRequestControl.setReturnValue("desc");
        mockSearchRequest.getOwnerUserName();
        mockSearchRequestControl.setReturnValue("owner");
        mockSearchRequestControl.replay();

        mockSRS.expects(once()).method("getFavouriteFilters").will(returnValue(Collections.singleton(mockSearchRequest)));

        service.getFavouriteFilters(user);
        mockSearchRequestControl.verify();
    }

    public void testCreateUser() throws Exception
    {
        com.atlassian.jira.bc.user.UserService.CreateUserValidationResult result =
                UserServiceResultHelper.getCreateUserValidationResult(
                        "testuser",
                        "testpassword",
                        "user@email.com",
                        "first last");
        mockUserService.expects(once()).method("validateCreateUserForAdminPasswordRequired").with(
                new Constraint[]{eq(user),
                        eq("testuser"),
                        eq("testpassword"),
                        eq("testpassword"),
                        eq("user@email.com"),
                        eq("first last")
                }).will(returnValue(result));
        mockUserService.expects(once()).method("createUserWithNotification").with(eq(result)).will(returnValue(user));

        service.createUser(user, "testuser", "testpassword", "first last", "user@email.com");

        checkCreateUserError("testuser", "testpassword", "testpassword", "first last", "user@email.com");
        checkCreateUserError(null, "testpassword", "testpassword", "first last", "user@email.com");
        checkCreateUserError("testuser2", null, null, "first last", "user@email.com");
        checkCreateUserError("testuser3", "testpassword", "testpassword", null, "user@email.com");
        checkCreateUserError("testuser4", "testpassword", "testpassword", "first last", null);
        checkCreateUserError("testuser5", "testpassword", "testpassword", "first last", "user.email.com");

        mockUserService.verify();
    }

    public void testCreateUserWithLeadingOrTrailingSpaces() throws Exception
    {
        com.atlassian.jira.bc.user.UserService.CreateUserValidationResult result =
                UserServiceResultHelper.getCreateUserValidationResult(
                        "testuser",
                        "testpassword",
                        "user@email.com",
                        "first last");
        mockUserService.expects(once()).method("validateCreateUserForAdminPasswordRequired").with(
                new Constraint[]{eq(user),
                        eq("testuser"),
                        eq("testpassword"),
                        eq("testpassword"),
                        eq("user@email.com"),
                        eq("first last")
                }).will(returnValue(result));
        mockUserService.expects(once()).method("createUserWithNotification").with(eq(result)).will(returnValue(user));

        service.createUser(user, " testuser ", "testpassword", "first last", "user@email.com");

        mockUserService.verify();
    }

    private void checkCreateUserError(String username, String password, String confirm, String email, String fullname)
            throws RemoteException
    {
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addError("field", "content");
        com.atlassian.jira.bc.user.UserService.CreateUserValidationResult errorResult =
                UserServiceResultHelper.getCreateUserValidationResult(errors);
        mockUserService.expects(once()).method("validateCreateUserForAdminPasswordRequired").with(
                new Constraint[]{eq(user),
                        eq(username),
                        eq(password),
                        eq(confirm),
                        eq(email),
                        eq(fullname)
                }).will(returnValue(errorResult));


        try
        {
            service.createUser(user, username, password, fullname, email);
            fail("validator error not caught");
        } catch (RemoteValidationException e)
        {
            // expected
        }
    }

    public void testCreateGroup() throws RemoteException
    {
        mockUserManager.addUser(user);
        final ImmutableGroup groupGeeks = new ImmutableGroup("geeks");

        mockGroupManager.expects(once()).method("createGroup").with(eq("geeks")).will(returnValue(groupGeeks));
        mockGroupManager.expects(once()).method("addUserToGroup").with(eq(user), eq(groupGeeks));
        mockGroupManager.expects(once()).method("getUsersInGroup").with(eq("geeks")).will(returnValue(Collections.singletonList(user)));
        mockPermissionManager.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));
        mockApplicationProperties.expects(atLeastOnce()).method("getOption").with(eq(APKeys.JIRA_OPTION_USER_EXTERNALMGT)).will(returnValue(false));

        RemoteUser remoteUser = new RemoteUser(user);
        //test create
        RemoteGroup created = service.createGroup(user, "geeks", remoteUser);
        assertEquals("geeks", created.getName());
        assertEquals(remoteUser, created.getUsers()[0]);

        mockGroupManager.verify();
    }

    public void testCreateGroupNullGroupName() throws RemoteException
    {
        mockUserManager.addUser(user);

        mockPermissionManager.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));
        mockApplicationProperties.expects(atLeastOnce()).method("getOption").with(eq(APKeys.JIRA_OPTION_USER_EXTERNALMGT)).will(returnValue(false));

        RemoteUser remoteUser = new RemoteUser(user);

        //test create barfs with null group name
        try
        {
            service.createGroup(user, null, remoteUser);
            fail("creating null group should have barfed");
        } catch (Exception e)
        {
            //expected
        }

        mockGroupManager.verify();
    }

    public void testCreateGroupExistingGroup() throws RemoteException
    {
        mockUserManager.addUser(user);

        mockGroupManager.expects(once()).method("createGroup").with(eq("geeks")).will(throwException(new InvalidGroupException(null, "")
        {
        }));
        mockPermissionManager.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));
        mockApplicationProperties.expects(atLeastOnce()).method("getOption").with(eq(APKeys.JIRA_OPTION_USER_EXTERNALMGT)).will(returnValue(false));

        RemoteUser remoteUser = new RemoteUser(user);

        //test create barfs with existing group name
        try
        {
            service.createGroup(user, "geeks", remoteUser);
            fail("creating existing group should have barfed");
        } catch (Exception e)
        {
            //expected
        }

        mockGroupManager.verify();
    }

    public void testCreateGroupNullFirstUser() throws RemoteException
    {
        mockUserManager.addUser(user);

        mockGroupManager.expects(once()).method("createGroup").with(eq("nerds")).will(returnValue(new ImmutableGroup("nerds")));
        mockGroupManager.expects(once()).method("getUsersInGroup").with(eq("nerds")).will(returnValue(Collections.<User>emptyList()));
        mockPermissionManager.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));
        mockApplicationProperties.expects(atLeastOnce()).method("getOption").with(eq(APKeys.JIRA_OPTION_USER_EXTERNALMGT)).will(returnValue(false));

        RemoteUser remoteUser = new RemoteUser(user);

        //test create works with null optional first user
        RemoteGroup created2 = service.createGroup(user, "nerds", null);
        assertEquals("nerds", created2.getName());
        assertTrue("nerds", created2.getUsers().length == 0);

        mockGroupManager.verify();
    }

    /*
     The following tests have been written with a real mock framework.  Sorry for the duplication but I cant pay for all
     of the sins of the fathers.
     */

    private UserServiceImpl easyMockCreateUserService()
    {
        EasyMock.replay(crowdService, globalPermissionManager, userUtil);
        return new UserServiceImpl(createMock(SearchRequestService.class),
                mockUserManager,
                createMock(GroupManager.class),
                createMock(PermissionManager.class),
                createMock(ApplicationProperties.class),
                createMock(com.atlassian.jira.bc.user.UserService.class),
                createMock(GroupService.class),
                mockRemoteEntityFactory, crowdService, globalPermissionManager, userUtil);
    }

    public void testUpdateUser_WhenTheyAreNotAndAdminOrSysAdmin()
    {
        expect(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(false);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andStubReturn(false);

        try
        {
            service = easyMockCreateUserService();
            service.updateUser(user, new RemoteUser(updatedUser));
            fail("should have failed because they dont have the right permissions");
        } catch (RemoteException expected)
        {
        }
        EasyMock.verify(crowdService, globalPermissionManager, userUtil);
    }

    public void testUpdateUser_WhenThereIsNoUserToUpdate()
    {
        expect(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(true);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andStubReturn(true);
        try
        {
            service = easyMockCreateUserService();
            service.updateUser(user, new RemoteUser(updatedUser));
            fail("should have failed because there is no user in the user manager");
        } catch (RemoteException expected)
        {

        }
        EasyMock.verify(crowdService, globalPermissionManager, userUtil);
    }

    public void testUpdateUser_WhenTheyUpdatingASysAdminButTheyAreNotASysAdmin()
    {
        mockUserManager.addUser(updatedUser);
        expect(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(true);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andStubReturn(false);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, updatedUser)).andStubReturn(true);

        try
        {
            service = easyMockCreateUserService();
            service.updateUser(user, new RemoteUser(updatedUser));
            fail("should have failed because they are a not sys admin and they are updating a sys admin");
        } catch (RemoteException expected)
        {

        }
        EasyMock.verify(crowdService, globalPermissionManager, userUtil);
    }

    public void testUpdateUser_WhenUserManagerSaysYouCant()
    {
        mockUserManager = new MockUserManager()
        {
            public boolean canUpdateUser(User user)
            {
                return false;
            }
        };
        mockUserManager.addUser(updatedUser);
        expect(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(true);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andStubReturn(false);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, updatedUser)).andStubReturn(true);

        try
        {
            service = easyMockCreateUserService();
            service.updateUser(user, new RemoteUser(updatedUser));
            fail("should have failed because they are a not sys admin and they are updating a sys admin");
        } catch (RemoteException expected)
        {

        }
        EasyMock.verify(crowdService, globalPermissionManager, userUtil);
    }

    public void testUpdateUser_HappyPath() throws OperationNotPermittedException, InvalidUserException
    {
        mockUserManager.addUser(updatedUser);
        expect(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(true);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andStubReturn(true);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, updatedUser)).andStubReturn(true);
        expect(crowdService.updateUser(EasyMock.<User>anyObject())).andStubReturn(updatedUser);
        try
        {
            service = easyMockCreateUserService();
            final RemoteUser resultUser = service.updateUser(user, new RemoteUser(updatedUser));

            // because we are mocking I cant show it really doding the update so this justs tests the --> RemoteUser code.
            assertEquals(updatedUser.getName(), resultUser.getName());
            assertEquals(updatedUser.getDisplayName(), resultUser.getFullname());
            assertEquals(updatedUser.getEmailAddress(), resultUser.getEmail());

        } catch (RemoteException expected)
        {
            fail("Hey why has this barfed?");
        }
        EasyMock.verify(crowdService, globalPermissionManager, userUtil);
    }


        public void testSetUserPassword_WhenTheyAreNotAndAdminOrSysAdmin()
    {
        expect(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(false);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andStubReturn(false);

        try
        {
            service = easyMockCreateUserService();
            service.setUserPassword(user, new RemoteUser(updatedUser), "newPassword");
            fail("should have failed because they dont have the right permissions");
        } catch (RemoteException expected)
        {
        }
        EasyMock.verify(crowdService, globalPermissionManager, userUtil);
    }

    public void testSetUserPassword_WhenThereIsNoUserToUpdate()
    {
        expect(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(true);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andStubReturn(true);
        try
        {
            service = easyMockCreateUserService();
            service.setUserPassword(user, new RemoteUser(updatedUser), "newpassword");
            fail("should have failed because there is no user in the user manager");
        } catch (RemoteException expected)
        {

        }
        EasyMock.verify(crowdService, globalPermissionManager, userUtil);
    }

    public void testSetUserPassword_WhenTheyUpdatingASysAdminButTheyAreNotASysAdmin()
    {
        mockUserManager.addUser(updatedUser);
        expect(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(true);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andStubReturn(false);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, updatedUser)).andStubReturn(true);

        try
        {
            service = easyMockCreateUserService();
            service.setUserPassword(user, new RemoteUser(updatedUser), "newpassword");
            fail("should have failed because they are a not sys admin and they are updating a sys admin");
        } catch (RemoteException expected)
        {

        }
        EasyMock.verify(crowdService, globalPermissionManager, userUtil);
    }

    public void testSetUserPassword_WhenUserManagerSaysYouCant()
    {
        mockUserManager = new MockUserManager()
        {
            public boolean canUpdateUser(User user)
            {
                return false;
            }
        };
        mockUserManager.addUser(updatedUser);
        expect(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(true);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andStubReturn(false);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, updatedUser)).andStubReturn(true);

        try
        {
            service = easyMockCreateUserService();
            service.setUserPassword(user, new RemoteUser(updatedUser), "newpassword");
            fail("should have failed because they are a not sys admin and they are updating a sys admin");
        } catch (RemoteException expected)
        {

        }
        EasyMock.verify(crowdService, globalPermissionManager, userUtil);
    }

    public void testSetUserPassword_HappyPath() throws OperationNotPermittedException, InvalidUserException, PermissionException, UserNotFoundException, InvalidCredentialException
    {
        mockUserManager.addUser(updatedUser);
        expect(globalPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andStubReturn(true);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).andStubReturn(true);
        expect(globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, updatedUser)).andStubReturn(true);
        userUtil.changePassword(updatedUser,"newPassword"); EasyMock.expectLastCall();
        try
        {
            service = easyMockCreateUserService();
            service.setUserPassword(user, new RemoteUser(updatedUser), "newPassword");
        } catch (RemoteException expected)
        {
            fail("Hey why has this barfed?");
        }
        EasyMock.verify(crowdService, globalPermissionManager, userUtil);
    }
}
