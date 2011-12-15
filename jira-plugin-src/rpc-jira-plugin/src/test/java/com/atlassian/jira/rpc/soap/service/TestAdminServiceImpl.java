package com.atlassian.jira.rpc.soap.service;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.mock.MockUser;
import com.atlassian.jira.rpc.soap.beans.RemoteConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.EasyList;
import com.atlassian.multitenant.MultiTenantContext;
import junit.framework.TestCase;

/**
 *
 */
public class TestAdminServiceImpl extends TestCase
{
    JiraAuthenticationContext authContext;
    User user;

    protected void setUp() throws Exception
    {
        super.setUp();
        MultiTenantContextTestUtils.setupMultiTenantSystem();
        user = OSUserConverter.convertToOSUser(new MockUser("frank"));

        authContext = (JiraAuthenticationContext) DuckTypeProxy.getProxy(JiraAuthenticationContext.class, new Object());
    }

    protected void tearDown() throws Exception
    {
        MultiTenantContext.setFactory(null);
        super.tearDown();
    }

    public void testGetConfigurationTimeTracking() throws RemoteException
    {

        String allowedKeys[] = {
                APKeys.JIRA_OPTION_TIMETRACKING,
        };

        AdminService adminService = getAdminService(allowedKeys, true, 5, 12);

        RemoteConfiguration rConfig = adminService.getConfiguration(user);

        assertTrue(rConfig.isAllowTimeTracking());
        assertEquals(5, rConfig.getTimeTrackingHoursPerDay());
        assertEquals(12, rConfig.getTimeTrackingDaysPerWeek());

        assertFalse(rConfig.isAllowAttachments());
        assertFalse(rConfig.isAllowAttachments());
        assertFalse(rConfig.isAllowExternalUserManagment());
        assertFalse(rConfig.isAllowIssueLinking());
        assertFalse(rConfig.isAllowSubTasks());
        assertFalse(rConfig.isAllowUnassignedIssues());
        assertFalse(rConfig.isAllowVoting());
        assertFalse(rConfig.isAllowWatching());
    }

    public void testGetConfiguration() throws RemoteException
    {
        String[] allowedKeys = new String[] {
                //APKeys.JIRA_OPTION_TIMETRACKING,
                APKeys.JIRA_OPTION_ALLOWATTACHMENTS,
                APKeys.JIRA_OPTION_VOTING,
                APKeys.JIRA_OPTION_WATCHING,
                APKeys.JIRA_OPTION_ALLOWUNASSIGNED,
                APKeys.JIRA_OPTION_ALLOWSUBTASKS,
                APKeys.JIRA_OPTION_ISSUELINKING,
                APKeys.JIRA_OPTION_USER_EXTERNALMGT,
        };

        AdminService adminService = getAdminService(allowedKeys, true, 5, 12);
        RemoteConfiguration rConfig = adminService.getConfiguration(user);

        assertTrue(rConfig.isAllowAttachments());
        assertTrue(rConfig.isAllowExternalUserManagment());
        assertTrue(rConfig.isAllowIssueLinking());
        assertTrue(rConfig.isAllowSubTasks());
        assertTrue(rConfig.isAllowUnassignedIssues());
        assertTrue(rConfig.isAllowVoting());
        assertTrue(rConfig.isAllowWatching());

        assertFalse(rConfig.isAllowTimeTracking());

    }

    public void testGetConfigurationWihtoutPermission()
    {

        AdminService adminService = getAdminService(new String[0], false, 5, 12);
        try
        {
            adminService.getConfiguration(user);
            fail("We should not have permission to make this call");
        }
        catch (RemotePermissionException e)
        {
            // ok
        }

    }


    private AdminService getAdminService(String[] allowedKeys, boolean hasPermission, int hoursPerDay, int daysPerWeek)
    {
        PermissionManager permManager = getPermManager(hasPermission);
        ApplicationProperties appProperties = getAppProperties(allowedKeys, hoursPerDay, daysPerWeek);
        AdminService adminService = new AdminServiceImpl(null, null, permManager, appProperties);
        return adminService;
    }


    private ApplicationProperties getAppProperties(final String[] allowedKeys, final int hoursInDay, final int daysInWeek)
    {
        Object apDuck = new Object()
        {
            public boolean getOption(String s)
            {
                for (int i = 0; i < allowedKeys.length; i++)
                {
                    if (s.equals(allowedKeys[i]))
                    {
                        return true;
                    }
                }
                return false;
            }

            public String getDefaultBackedString(String s)
            {
                if (APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY.equals(s))
                {
                    return String.valueOf(hoursInDay);
                }
                else if (APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK.equals(s))
                {
                    return String.valueOf(daysInWeek);
                }
                return null;
            }

        };
        return (ApplicationProperties) DuckTypeProxy.getProxy(ApplicationProperties.class, EasyList.build(apDuck), DuckTypeProxy.RETURN_NULL);

    }

    private PermissionManager getPermManager(final boolean allowed)
    {
        Object pmDuck = new Object()
        {
            public boolean hasPermission(int i, User user)
            {
                return allowed;
            }
        };
        return (PermissionManager) DuckTypeProxy.getProxy(PermissionManager.class, EasyList.build(pmDuck), DuckTypeProxy.RETURN_NULL);
    }
}
