package com.atlassian.jira.rpc.soap.service;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionImpl;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.soap.beans.RemotePermission;
import com.atlassian.jira.rpc.soap.beans.RemotePermissionScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;
import com.atlassian.jira.rpc.soap.util.MockRemoteEntityFactory;
import com.atlassian.jira.rpc.soap.util.RemoteEntityFactory;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.multitenant.MultiTenantContext;
import org.apache.commons.collections.map.ListOrderedMap;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TestSchemeServiceImpl
 */
public class TestSchemeServiceImpl extends MockObjectTestCase
{
    private Mock mockPermissionManager;
    private Mock mockNotificationSchemeManager;
    private Mock mockPermissionSchemeManager;
    private Mock mockIssueSecuritySchemeManager;
    private Mock mockUserManager;

    private MockControl ctrlSchemePermissions = MockClassControl.createControl(SchemePermissions.class);
    private SchemePermissions mockSchemePermissions = (SchemePermissions) ctrlSchemePermissions.getMock();

    private User user;
    private SchemeService schemeService;

    protected void setUp() throws Exception
    {
        super.setUp();
        MultiTenantContextTestUtils.setupMultiTenantSystem();
        mockPermissionManager = new Mock(PermissionManager.class);
        mockNotificationSchemeManager = new Mock(NotificationSchemeManager.class);
        mockPermissionSchemeManager = new Mock(PermissionSchemeManager.class);
        mockIssueSecuritySchemeManager = new Mock(IssueSecuritySchemeManager.class);
        mockUserManager = new Mock(UserManager.class);
        final RemoteEntityFactory mockRemoteEntityFactory = new MockRemoteEntityFactory();

        user = ImmutableUser.newUser().name("driver").toUser();

        schemeService = new SchemeServiceImpl((PermissionManager) mockPermissionManager.proxy(),
                (NotificationSchemeManager) mockNotificationSchemeManager.proxy(),
                (PermissionSchemeManager) mockPermissionSchemeManager.proxy(),
                (IssueSecuritySchemeManager) mockIssueSecuritySchemeManager.proxy(),
                mockSchemePermissions,
                (UserManager) mockUserManager.proxy(),
                mockRemoteEntityFactory);
    }

    protected void tearDown()
    {
        com.opensymphony.user.UserManager.reset();
        MultiTenantContext.setFactory(null);
    }
        
    public void testGetNotificationSchemes() throws Exception
    {
        GenericValue defaultNotificationScheme = new MockGenericValue("DefaultNotificationScheme", UtilMisc.toMap("id", new Long(0), "name", "A NotificationScheme"));
        GenericValue anotherNotificationScheme = new MockGenericValue("AnotherNotificationScheme", UtilMisc.toMap("id", new Long(1), "name", "Another NotificationScheme"));
        mockNotificationSchemeManager.expects(once()).method("getSchemes").withNoArguments().will(returnValue(UtilMisc.toList(defaultNotificationScheme, anotherNotificationScheme)));
        mockPermissionManager.expects(once()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));

        RemoteScheme[] returnedNotificationSchemes = schemeService.getNotificationSchemes(user);
        assertEquals(returnedNotificationSchemes[0].getId(), new Long(0));
        assertEquals(returnedNotificationSchemes[0].getName(), "A NotificationScheme");
        assertEquals(returnedNotificationSchemes[0].getType(), "notification");
        assertEquals(returnedNotificationSchemes[1].getId(), new Long(1));
        assertEquals(returnedNotificationSchemes[1].getName(), "Another NotificationScheme");
        assertEquals(returnedNotificationSchemes[1].getType(), "notification");
    }

    public void testGetPermissionSchemes() throws Exception
    {
        GenericValue defaultPermissionScheme = new MockGenericValue("DefaultPermissionScheme", UtilMisc.toMap("id", new Long(0), "name", "A PermissionScheme"));
        GenericValue anotherPermissionScheme = new MockGenericValue("AnotherPermissionScheme", UtilMisc.toMap("id", new Long(1), "name", "Another PermissionScheme"));
        mockPermissionSchemeManager.expects(once()).method("getSchemes").withNoArguments().will(returnValue(UtilMisc.toList(defaultPermissionScheme, anotherPermissionScheme)));
        mockPermissionSchemeManager.expects(atLeastOnce()).method("getEntities").with(ANYTHING, ANYTHING).will(returnValue(new ArrayList()));
        mockPermissionManager.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));

        Map permissions = new HashMap();
        permissions.put(new Integer(Permissions.WORK_ISSUE), new PermissionImpl(String.valueOf(Permissions.WORK_ISSUE), "Work On Issues", "Ability to log work done against an issue. Only useful if Time Tracking is turned on.", "admin.permissions.WORK_ISSUE", "admin.permissions.descriptions.WORK_ISSUE"));

        mockSchemePermissions.getSchemePermissions();
        ctrlSchemePermissions.setReturnValue(permissions);
        mockSchemePermissions.getSchemePermissions();
        ctrlSchemePermissions.setReturnValue(permissions);
        ctrlSchemePermissions.replay();

        RemotePermissionScheme[] returnedPermissionSchemes = schemeService.getPermissionSchemes(user);
        assertEquals(returnedPermissionSchemes[0].getId(), new Long(0));
        assertEquals(returnedPermissionSchemes[0].getName(), "A PermissionScheme");
        assertEquals(returnedPermissionSchemes[0].getType(), "permission");
        assertEquals(returnedPermissionSchemes[1].getId(), new Long(1));
        assertEquals(returnedPermissionSchemes[1].getName(), "Another PermissionScheme");
        assertEquals(returnedPermissionSchemes[1].getType(), "permission");
    }

    public void testGetIssueSecuritySchemes() throws Exception
    {
        GenericValue defaultIssueSecurityScheme = new MockGenericValue("DefaultIssueSecurityScheme", UtilMisc.toMap("id", new Long(0), "name", "DefaultIssueSecurityScheme"));
        GenericValue anotherIssueSecurityScheme = new MockGenericValue("AnotherIssueSecurityScheme", UtilMisc.toMap("id", new Long(1), "name", "AnotherIssueSecurityScheme"));
        mockIssueSecuritySchemeManager.expects(once()).method("getSchemes").withNoArguments().will(returnValue(UtilMisc.toList(defaultIssueSecurityScheme, anotherIssueSecurityScheme)));
        mockPermissionManager.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));

        try
        {
            RemoteScheme[] returnedIssueSecuritySchemes = schemeService.getIssueSecuritySchemes(user);

            assertEquals(returnedIssueSecuritySchemes[0].getId(), new Long(0));
            assertEquals(returnedIssueSecuritySchemes[0].getName(), "DefaultIssueSecurityScheme");
            assertEquals(returnedIssueSecuritySchemes[0].getType(), "issueSecurity");
            assertEquals(returnedIssueSecuritySchemes[1].getId(), new Long(1));
            assertEquals(returnedIssueSecuritySchemes[1].getName(), "AnotherIssueSecurityScheme");
            assertEquals(returnedIssueSecuritySchemes[1].getType(), "issueSecurity");
        }
        catch (RemoteException e)
        {
            assertTrue(e.getMessage().indexOf("Your edition of Jira does not support IssueSecuritySchemes") > -1);
        }
    }

    public void testGetAllPermissions() throws Exception
    {
        mockPermissionManager.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));
        Map permissions = new ListOrderedMap();
        permissions.put(new Integer(Permissions.WORK_ISSUE), new PermissionImpl(String.valueOf(Permissions.WORK_ISSUE), "Work On Issues", "Ability to log work done against an issue. Only useful if Time Tracking is turned on.", "admin.permissions.WORK_ISSUE", "admin.permissions.descriptions.WORK_ISSUE"));
        permissions.put(new Integer(Permissions.PROJECT_ADMIN), new PermissionImpl(String.valueOf(Permissions.PROJECT_ADMIN), "Administer Projects", "Ability to administer a project in JIRA.", "admin.permissions.PROJECT_ADMIN", "admin.permissions.descriptions.PROJECT_ADMIN"));
        permissions.put(new Integer(Permissions.EDIT_ISSUE), new PermissionImpl(String.valueOf(Permissions.EDIT_ISSUE), "Edit Issues", "Ability to edit issues.", "admin.permissions.EDIT_ISSUE", "admin.permissions.descriptions.EDIT_ISSUE"));
        permissions.put(new Integer(Permissions.MANAGE_WATCHER_LIST), new PermissionImpl(String.valueOf(Permissions.MANAGE_WATCHER_LIST), "Manage Watchers", "Ability to manage the watchers of an issue.", "admin.permissions.MANAGE_WATCHER_LIST", "admin.permissions.descriptions.MANAGE_WATCHER_LIST"));
        permissions.put(new Integer(Permissions.COMMENT_EDIT_OWN), new PermissionImpl(String.valueOf(Permissions.COMMENT_EDIT_OWN), "Edit Own Comments", "Ability to edit own comments made on issues.", "admin.permissions.COMMENT_EDIT_OWN", "admin.permissions.descriptions.COMMENT_EDIT_OWN"));

        mockSchemePermissions.getSchemePermissions();
        ctrlSchemePermissions.setReturnValue(permissions);
        ctrlSchemePermissions.replay();

        RemotePermission[] allPermissions = schemeService.getAllPermissions(user);
        assertNotNull(allPermissions);
        assertEquals(5, allPermissions.length);
        assertPermissionPresent(1, Permissions.PROJECT_ADMIN, "Administer Projects", allPermissions);
        assertPermissionPresent(3, Permissions.MANAGE_WATCHER_LIST, "Manage Watchers", allPermissions);
    }

    private void assertPermissionPresent(int pos, int permissionId, String permissionName, RemotePermission[] remotePermissions)
    {
        Long id = new Long(permissionId);

        RemotePermission remotePermission = remotePermissions[pos];
        if (id.equals(remotePermission.getPermission()) && permissionName.equals(remotePermission.getName()))
        {
            //permission found!
            return;
        }

        fail("Could not find permission " + permissionId + ": " + permissionName);
    }

    public void testCreatePermissionScheme() throws Exception
    {
        mockPermissionManager.expects(once()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));
        mockPermissionSchemeManager.expects(once()).method("getScheme").with(eq("blurby")).will(returnValue(null));
        GenericValue fields = new MockGenericValue("PermissionScheme", UtilMisc.toMap("id", new Long(42), "name", "blurby", "description", "the blurby scheme"));
        mockPermissionSchemeManager.expects(once()).method("createScheme").with(eq("blurby"), eq("the blurby scheme")).will(returnValue(fields));
        RemotePermissionScheme scheme = schemeService.createPermissionScheme(user, "blurby", "the blurby scheme");
        assertEquals(new Long(42), scheme.getId());
        assertEquals("blurby", scheme.getName());
        assertEquals("the blurby scheme", scheme.getDescription());
    }

    public void testDeletePermissionScheme() throws Exception
    {
        mockPermissionManager.expects(once()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));
        GenericValue schemeFields = new MockGenericValue("PermissionScheme", UtilMisc.toMap("id", new Long(42), "name", "blah", "description", "the blah scheme"));
        mockPermissionSchemeManager.expects(once()).method("getScheme").with(eq("blah")).will(returnValue(schemeFields));
        GenericValue defaultSchemeFields = new MockGenericValue("DefaultPermissionScheme", UtilMisc.toMap("id", new Long(0), "name", "DefaultPermissionScheme", "description", "the jira default permission scheme"));
        mockPermissionSchemeManager.expects(atLeastOnce()).method("getDefaultScheme").withNoArguments().will(returnValue(defaultSchemeFields));
        List projects = UtilMisc.toList(new MockGenericValue("Project", UtilMisc.toMap("id", new Long(1), "name", "project")));
        mockPermissionSchemeManager.expects(atLeastOnce()).method("getProjects").with(eq(schemeFields)).will(returnValue(projects));
        mockPermissionSchemeManager.expects(once()).method("removeSchemesFromProject").with(eq(projects.get(0))).isVoid();
        mockPermissionSchemeManager.expects(once()).method("addDefaultSchemeToProject").with(eq(projects.get(0))).isVoid();
        mockPermissionSchemeManager.expects(once()).method("deleteScheme").with(eq(new Long(42))).isVoid();
        schemeService.deletePermissionScheme(user, "blah");
    }

    public void testAddPermissionToScheme() throws Exception
    {
        Map permissions = new ListOrderedMap();
        permissions.put(new Integer(Permissions.WORK_ISSUE), new PermissionImpl(String.valueOf(Permissions.WORK_ISSUE), "Work On Issues", "Ability to log work done against an issue. Only useful if Time Tracking is turned on.", "admin.permissions.WORK_ISSUE", "admin.permissions.descriptions.WORK_ISSUE"));
        permissions.put(new Integer(Permissions.PROJECT_ADMIN), new PermissionImpl(String.valueOf(Permissions.PROJECT_ADMIN), "Administer Projects", "Ability to administer a project in JIRA.", "admin.permissions.PROJECT_ADMIN", "admin.permissions.descriptions.PROJECT_ADMIN"));
        permissions.put(new Integer(Permissions.EDIT_ISSUE), new PermissionImpl(String.valueOf(Permissions.EDIT_ISSUE), "Edit Issues", "Ability to edit issues.", "admin.permissions.EDIT_ISSUE", "admin.permissions.descriptions.EDIT_ISSUE"));
        permissions.put(new Integer(Permissions.MANAGE_WATCHER_LIST), new PermissionImpl(String.valueOf(Permissions.MANAGE_WATCHER_LIST), "Manage Watchers", "Ability to manage the watchers of an issue.", "admin.permissions.MANAGE_WATCHER_LIST", "admin.permissions.descriptions.MANAGE_WATCHER_LIST"));
        permissions.put(new Integer(Permissions.COMMENT_EDIT_OWN), new PermissionImpl(String.valueOf(Permissions.COMMENT_EDIT_OWN), "Edit Own Comments", "Ability to edit own comments made on issues.", "admin.permissions.COMMENT_EDIT_OWN", "admin.permissions.descriptions.COMMENT_EDIT_OWN"));

        mockSchemePermissions.getPermissionName(new Integer(23));
        ctrlSchemePermissions.setReturnValue("Administer Projects");
        mockSchemePermissions.getSchemePermissions();
        ctrlSchemePermissions.setReturnValue(permissions);
        ctrlSchemePermissions.replay();

        mockPermissionManager.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));
        GenericValue permissionSchemeFields = new MockGenericValue("spiffy", UtilMisc.toMap("id", new Long(0), "name", "spiffy", "description", "the spiffy permission scheme"));
        RemotePermissionScheme permissionScheme = new RemotePermissionScheme(permissionSchemeFields);
        RemotePermission permission = new RemotePermission(new Long(23), "Administer Projects");
        RemoteUser remoteUser = new RemoteUser(user);
        mockPermissionSchemeManager.expects(once()).method("getScheme").with(eq("spiffy")).will(returnValue(permissionSchemeFields));
        mockPermissionSchemeManager.expects(once()).method("getScheme").with(eq(new Long(0))).will(returnValue(permissionSchemeFields));
        mockPermissionSchemeManager.expects(once()).method("getEntities").with(eq(permissionSchemeFields), eq(new Long(23)), eq("user"), eq("driver")).will(returnValue(new ArrayList()));
        mockPermissionSchemeManager.expects(once()).method("createSchemeEntity").with(eq(permissionSchemeFields), eq(new SchemeEntity("user", "driver", new Long(23)))).will(returnValue(null));
        List entities = UtilMisc.toList(new MockGenericValue("entityMappings", UtilMisc.toMap("type", "user", "parameter", "driver")));
        mockPermissionSchemeManager.expects(once()).method("getEntities").with(eq(permissionSchemeFields), eq(new Long(23))).will(returnValue(entities));
        mockPermissionSchemeManager.expects(atLeastOnce()).method("getEntities").with(eq(permissionSchemeFields), not(eq(new Long(23)))).will(returnValue(new ArrayList()));
        mockUserManager.expects(atLeastOnce()).method("getUser").with(eq("driver")).will(returnValue(OSUserConverter.convertToOSUser(user)));
        
        RemotePermissionScheme rval = schemeService.addPermissionTo(user, permissionScheme, permission, remoteUser);
        assertEquals(permission, rval.getPermissionMappings()[0].getPermission());
        assertEquals(remoteUser, rval.getPermissionMappings()[0].getRemoteEntities()[0]);
    }

    public void testDeletePermissionFromScheme() throws Exception
    {
        Map permissions = new ListOrderedMap();
        permissions.put(new Integer(Permissions.WORK_ISSUE), new PermissionImpl(String.valueOf(Permissions.WORK_ISSUE), "Work On Issues", "Ability to log work done against an issue. Only useful if Time Tracking is turned on.", "admin.permissions.WORK_ISSUE", "admin.permissions.descriptions.WORK_ISSUE"));
        permissions.put(new Integer(Permissions.PROJECT_ADMIN), new PermissionImpl(String.valueOf(Permissions.PROJECT_ADMIN), "Administer Projects", "Ability to administer a project in JIRA.", "admin.permissions.PROJECT_ADMIN", "admin.permissions.descriptions.PROJECT_ADMIN"));
        permissions.put(new Integer(Permissions.EDIT_ISSUE), new PermissionImpl(String.valueOf(Permissions.EDIT_ISSUE), "Edit Issues", "Ability to edit issues.", "admin.permissions.EDIT_ISSUE", "admin.permissions.descriptions.EDIT_ISSUE"));
        permissions.put(new Integer(Permissions.MANAGE_WATCHER_LIST), new PermissionImpl(String.valueOf(Permissions.MANAGE_WATCHER_LIST), "Manage Watchers", "Ability to manage the watchers of an issue.", "admin.permissions.MANAGE_WATCHER_LIST", "admin.permissions.descriptions.MANAGE_WATCHER_LIST"));
        permissions.put(new Integer(Permissions.COMMENT_EDIT_OWN), new PermissionImpl(String.valueOf(Permissions.COMMENT_EDIT_OWN), "Edit Own Comments", "Ability to edit own comments made on issues.", "admin.permissions.COMMENT_EDIT_OWN", "admin.permissions.descriptions.COMMENT_EDIT_OWN"));

        mockSchemePermissions.getPermissionName(new Integer(23));
        ctrlSchemePermissions.setReturnValue("Administer Projects");

        mockSchemePermissions.getSchemePermissions();
        ctrlSchemePermissions.setReturnValue(permissions);
        ctrlSchemePermissions.replay();

        mockPermissionManager.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));
        GenericValue permissionSchemeFields = new MockGenericValue("spiffy", UtilMisc.toMap("id", new Long(0), "name", "spiffy", "description", "the spiffy permission scheme"));
        RemotePermissionScheme permissionScheme = new RemotePermissionScheme(permissionSchemeFields);
        RemotePermission permission = new RemotePermission(new Long(23), "Administer Projects");
        RemoteUser remoteUser = new RemoteUser(user);
        mockPermissionSchemeManager.expects(once()).method("getScheme").with(eq("spiffy")).will(returnValue(permissionSchemeFields));
        mockPermissionSchemeManager.expects(once()).method("getScheme").with(eq(new Long(0))).will(returnValue(permissionSchemeFields));

        List entities = UtilMisc.toList(new MockGenericValue("Entity", UtilMisc.toMap("id", new Long(99))));
        mockPermissionSchemeManager.expects(once()).method("getEntities").with(eq(permissionSchemeFields), eq(new Long(23)), eq("user"), eq("driver")).will(returnValue(entities));
        mockPermissionSchemeManager.expects(once()).method("deleteEntity").with(eq(new Long(99))).isVoid();
        mockPermissionSchemeManager.expects(atLeastOnce()).method("getEntities").with(eq(permissionSchemeFields), ANYTHING).will(returnValue(new ArrayList()));
        mockUserManager.expects(once()).method("getUser").with(eq("driver")).will(returnValue(OSUserConverter.convertToOSUser(user)));

        schemeService.deletePermissionFrom(user, permissionScheme, permission, remoteUser);
    }
}