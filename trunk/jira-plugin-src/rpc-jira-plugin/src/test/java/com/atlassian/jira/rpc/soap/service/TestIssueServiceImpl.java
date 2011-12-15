package com.atlassian.jira.rpc.soap.service;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.mock.MockIssue;
import com.atlassian.jira.rpc.soap.beans.RemoteSecurityLevel;
import com.atlassian.jira.rpc.soap.util.SoapUtilsBean;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import junit.framework.TestCase;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * @since v3.12
 */
public class TestIssueServiceImpl extends TestCase
{
    private MockGenericValue mockSecurityLevel;
    private PermissionManager mockPermissionManager;
    private IssueManager mockIssueManager;

    /**
     * This is a test for JRA-13703.  Simply checks that the getFieldsForEdit method calls setRemoteUserInJira.  It
     * doesn't check that the fields returned are correct, as this would involve mocking out the entire universe and
     * should already be covered by func tests/unit tests elswhere.
     */
    public void testGetFieldsForEditSetUserInAuthenticationContext() throws RemoteException
    {
        Mock mockMutableIssue = new Mock(MutableIssue.class);
        Mock mockIssueManager = new Mock(IssueManager.class);
        Issue issue = (Issue) mockMutableIssue.proxy();
        mockIssueManager.expectAndReturn("getIssueObject", P.ANY_ARGS, issue);
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        MockControl mockSoapUtilsBeanControl = MockClassControl.createControl(SoapUtilsBean.class);
        SoapUtilsBean mockSoapUtilsBean = (SoapUtilsBean) mockSoapUtilsBeanControl.getMock();
        mockSoapUtilsBean.getFieldsForEdit(null, issue);
        mockSoapUtilsBeanControl.setReturnValue(null);
        mockSoapUtilsBeanControl.replay();


        final MethodCallResultHolder methodCallResultHolder = new MethodCallResultHolder();
        IssueServiceImpl issueService = new IssueServiceImpl((PermissionManager) mockPermissionManager.proxy(), null,
                (IssueManager) mockIssueManager.proxy(), null, null, null, null, null, null, mockSoapUtilsBean,
                null, null, null, null, null, null, null, null, null, null, null, null)
        {
            User setRemoteUserInJira(User user)
            {
                methodCallResultHolder.methodCalled = true;
                return null;
            }

        };

        issueService.getFieldsForEdit(null, "TST-1");
        assertTrue(methodCallResultHolder.methodCalled);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        mockSecurityLevel = new MockGenericValue("entityname", EasyMap.build(
                "id", "10000",
                "name", "Everybody",
                "description", "some random text"
        ));
        mockPermissionManager = createMock(PermissionManager.class);
        mockIssueManager = createMock(IssueManager.class);
    }

    private class MethodCallResultHolder
    {
        public boolean methodCalled = false;
    }

    public void testGetSecurityLevelNone() throws Exception
    {
        final MutableIssue mockIssue = createMockIssue(null);
        final PermissionManager mockPermissionManager = createMockPermissionManager(mockIssue, true, true);
        final IssueManager mockIssueManager = createMockIssueManager(mockIssue);

        IssueServiceImpl issueService = createIssueService(mockPermissionManager, mockIssueManager);

        final RemoteSecurityLevel remoteSecurityLevel = issueService.getSecurityLevel(null, "TST-1");
        assertNull(remoteSecurityLevel);
    }

    public void testGetSecurityLevelSet() throws Exception
    {
        final MutableIssue mockIssue = createMockIssue(mockSecurityLevel);
        final PermissionManager mockPermissionManager = createMockPermissionManager(mockIssue, true, true);
        final IssueManager mockIssueManager = createMockIssueManager(mockIssue);

        IssueServiceImpl issueService = createIssueService(mockPermissionManager, mockIssueManager);

        final RemoteSecurityLevel remoteSecurityLevel = issueService.getSecurityLevel(null, "TST-1");
        assertEquals("10000", remoteSecurityLevel.getId());
        assertEquals("Everybody", remoteSecurityLevel.getName());
        assertEquals("some random text", remoteSecurityLevel.getDescription());
    }

    public void testGetSecurityLevelPermissionDenied() throws Exception
    {
        final MutableIssue mockIssue = createMockIssue(mockSecurityLevel);
        final PermissionManager mockPermissionManager = createMockPermissionManager(mockIssue, false, true);
        final IssueManager mockIssueManager = createMockIssueManager(mockIssue);

        IssueServiceImpl issueService = createIssueService(mockPermissionManager, mockIssueManager);
        final RemoteSecurityLevel remoteSecurityLevel = issueService.getSecurityLevel(null, "TST-1");
        assertEquals(mockSecurityLevel.getString("id"), remoteSecurityLevel.getId());
        assertEquals(mockSecurityLevel.getString("name"), remoteSecurityLevel.getName());
        assertEquals(mockSecurityLevel.getString("description"), remoteSecurityLevel.getDescription());
    }

    public void testInvalidIssueKey() throws Exception
    {
        final MockControl mockIssueManagerControl = MockControl.createControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject("TST-1");
        mockIssueManagerControl.setReturnValue(null);
        mockIssueManagerControl.replay();

        IssueServiceImpl issueService = createIssueService(null, mockIssueManager);
        try
        {
            issueService.getSecurityLevel(null, "TST-1");
            fail();
        }
        catch (RemotePermissionException e)
        {
            assertMessageContains(e, "This issue does not exist.");
        }
    }

    public void testNoPermissionToBrowseIssue() throws Exception
    {
        final MutableIssue mockIssue = createMockIssue(mockSecurityLevel);
        final PermissionManager mockPermissionManager = createMockPermissionManager(mockIssue, false, false);

        final MockControl mockIssueManagerControl = MockControl.createControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject("TST-1");
        mockIssueManagerControl.setReturnValue(mockIssue);
        mockIssueManagerControl.replay();

        IssueServiceImpl issueService = createIssueService(mockPermissionManager, mockIssueManager);
        final RemoteSecurityLevel level = issueService.getSecurityLevel(null, "TST-1");
        assertNull(level);
    }

    public void testGetResolutionDateByKeyNotResolved() throws RemoteException
    {
        final MutableIssue issue = new MockIssue(1L);

        expect(mockIssueManager.getIssueObject("HSP-1")).andStubReturn(issue);
        expect(mockIssueManager.getIssueObject((Long) null)).andStubReturn(issue); // parent issue lookup
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, issue, (com.atlassian.crowd.embedded.api.User)null)).andStubReturn(true);

        replay(mockPermissionManager, mockIssueManager);

        final IssueServiceImpl issueService = createIssueService(mockPermissionManager, mockIssueManager);
        final Date resolutionDate = issueService.getResolutionDateByKey(null, "HSP-1");
        assertNull(resolutionDate);

        verify(mockPermissionManager, mockIssueManager);
    }

    public void testGetResolutionDateByKeyResolved() throws RemoteException
    {
        final MutableIssue issue = new MockIssue(1L);
        final Timestamp now = new Timestamp(new Date().getTime());
        issue.setResolutionDate(now);
        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        final IssueManager mockIssueManager = createMock(IssueManager.class);

        expect(mockIssueManager.getIssueObject("HSP-1")).andStubReturn(issue);
        expect(mockIssueManager.getIssueObject((Long) null)).andStubReturn(issue); // parent issue lookup
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, issue, (com.atlassian.crowd.embedded.api.User)null)).andStubReturn(true);

        replay(mockPermissionManager, mockIssueManager);

        final IssueServiceImpl issueService = createIssueService(mockPermissionManager, mockIssueManager);
        final Date resolutionDate = issueService.getResolutionDateByKey(null, "HSP-1");
        assertEquals(now, resolutionDate);

        verify(mockPermissionManager, mockIssueManager);
    }

    public void testGetResolutionDateByKeyNoIssue() throws RemoteException
    {
        expect(mockIssueManager.getIssueObject("HSP-1")).andReturn(null);

        replay(mockIssueManager);

        final IssueServiceImpl issueService = createIssueService(null, mockIssueManager);
        try
        {
            issueService.getResolutionDateByKey(null, "HSP-1");
            fail("should have thrown exception about issue not existing!");
        }
        catch (RemoteException e)
        {
            //yay
        }

        verify(mockIssueManager);
    }

    public void testGetResolutionDateByKeyResolvedNoPermission() throws RemoteException
    {
        final MutableIssue issue = new MockIssue(1L);

        expect(mockIssueManager.getIssueObject("HSP-1")).andStubReturn(issue);
        expect(mockIssueManager.getIssueObject(1L)).andStubReturn(issue);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, issue, (com.atlassian.crowd.embedded.api.User)null)).andStubReturn(false);

        replay(mockPermissionManager, mockIssueManager);


        final IssueServiceImpl issueService = createIssueService(mockPermissionManager, mockIssueManager);
        try
        {
            issueService.getResolutionDateByKey(null, "HSP-1");
            fail("should have thrown exception about issue not existing!");
        }
        catch (RemoteException e)
        {
            //yay
        }

        verify(mockPermissionManager, mockIssueManager);
    }

    public void testGetResolutionDateByIdNotResolved() throws RemoteException
    {
        final MutableIssue issue = new MockIssue(1000L);

        expect(mockIssueManager.getIssueObject(1000L)).andStubReturn(issue);
        expect(mockIssueManager.getIssueObject((Long) null)).andStubReturn(issue); // parent issue lookup
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, issue, (com.atlassian.crowd.embedded.api.User)null)).andStubReturn(true);

        replay(mockPermissionManager, mockIssueManager);


        final IssueServiceImpl issueService = createIssueService(mockPermissionManager, mockIssueManager);
        final Date resolutionDate = issueService.getResolutionDateById(null, 1000L);
        assertNull(resolutionDate);

        verify(mockPermissionManager, mockIssueManager);
    }

    public void testGetResolutionDateByIdResolved() throws RemoteException
    {
        final Timestamp now = new Timestamp(new Date().getTime());
        final MutableIssue issue = new MockIssue(1000L);
        issue.setResolutionDate(now);

        expect(mockIssueManager.getIssueObject(1000L)).andStubReturn(issue);
        expect(mockIssueManager.getIssueObject((Long) null)).andStubReturn(issue); // parent issue lookup
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, issue, (com.atlassian.crowd.embedded.api.User)null)).andStubReturn(true);

        replay(mockPermissionManager, mockIssueManager);

        final IssueServiceImpl issueService = createIssueService(mockPermissionManager, mockIssueManager);
        final Date resolutionDate = issueService.getResolutionDateById(null, 1000L);
        assertEquals(now, resolutionDate);

        verify(mockPermissionManager, mockIssueManager);
    }

    public void testGetResolutionDateByIdNoIssue() throws RemoteException
    {
        expect(mockIssueManager.getIssueObject(1000L)).andReturn(null);
        replay(mockIssueManager);

        final IssueServiceImpl issueService = createIssueService(null, mockIssueManager);
        try
        {
            issueService.getResolutionDateById(null, 1000L);
            fail("should have thrown exception about issue not existing!");
        }
        catch (RemoteException e)
        {
            //yay
        }

        verify(mockIssueManager);
    }

    public void testGetResolutionDateByIdResolvedNoPermission() throws RemoteException
    {
        final MutableIssue issue = new MockIssue(1000L);

        expect(mockIssueManager.getIssueObject(1000L)).andStubReturn(issue);
        expect(mockIssueManager.getIssueObject(1000L)).andStubReturn(issue);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, issue, (com.atlassian.crowd.embedded.api.User)null)).andStubReturn(false);

        replay(mockPermissionManager, mockIssueManager);

        final IssueServiceImpl issueService = createIssueService(mockPermissionManager, mockIssueManager);
        try
        {
            issueService.getResolutionDateById(null, 1000L);
            fail("should have thrown exception about issue not existing!");
        }
        catch (RemoteException e)
        {
            //yay
        }

        verify(mockPermissionManager, mockIssueManager);
    }

    private PermissionManager createMockPermissionManager(final MutableIssue mockIssue, final boolean b, boolean browsePermission)
    {
        final MockControl mockPermissionManagerControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mockPermissionManager = (PermissionManager) mockPermissionManagerControl.getMock();
        mockPermissionManager.hasPermission(Permissions.SET_ISSUE_SECURITY, mockIssue, (User) null);
        mockPermissionManagerControl.setReturnValue(true);
        mockPermissionManager.hasPermission(Permissions.BROWSE, (GenericValue) null, (User) null);
        mockPermissionManagerControl.setReturnValue(browsePermission);
        mockPermissionManager.hasPermission(Permissions.SET_ISSUE_SECURITY, (Project) null, (User) null);
        mockPermissionManagerControl.setReturnValue(b);
        mockPermissionManagerControl.replay();
        return mockPermissionManager;
    }

    private MutableIssue createMockIssue(final MockGenericValue mockSecurityLevel)
    {
        final MockControl mockIssueControl = MockClassControl.createControl(MutableIssue.class);
        final MutableIssue mockIssue = (MutableIssue) mockIssueControl.getMock();
        mockIssue.getSecurityLevel();
        mockIssueControl.setReturnValue(mockSecurityLevel);
        mockIssue.getGenericValue();
        mockIssueControl.setReturnValue(null);
        mockIssue.getProjectObject();
        mockIssueControl.setReturnValue(null);
        mockIssueControl.replay();
        return mockIssue;
    }

    private IssueServiceImpl createIssueService(final PermissionManager mockPermissionManager, final IssueManager mockIssueManager)
    {
        return new IssueServiceImpl(mockPermissionManager, null, mockIssueManager, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private IssueManager createMockIssueManager(final MutableIssue mockIssue)
    {
        final MockControl mockIssueManagerControl = MockControl.createControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject("TST-1");
        mockIssueManagerControl.setReturnValue(mockIssue);
        mockIssueManagerControl.replay();
        return mockIssueManager;
    }

    private void assertMessageContains(final RemotePermissionException e, final String msg)
    {
        assertTrue(e.toString().indexOf(msg) >= 0);
    }

}
