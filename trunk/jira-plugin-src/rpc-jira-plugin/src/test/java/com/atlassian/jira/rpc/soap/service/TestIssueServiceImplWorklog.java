package com.atlassian.jira.rpc.soap.service;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResultFactory;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.mock.MockIssue;
import com.atlassian.jira.rpc.mock.MockUser;
import com.atlassian.jira.rpc.soap.beans.RemoteWorklog;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.multitenant.MultiTenantContext;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import junit.framework.TestCase;
import org.easymock.MockControl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Unit test for the Worklog related methods of IssueServiceImpl.
 */
public class TestIssueServiceImplWorklog extends TestCase
{
    private MockControl issueManagerCtrl;
    private IssueManager issueManager;
    private Issue issue;
    private User user;
    private JiraAuthenticationContext authContext;
    private static final Collection ERROR_COLLECTION = EasyList.build("abcdef", "error", "robot");
    private static final String ISSUE_KEY_FOO_1 = "FOO-1";


    protected void setUp() throws Exception
    {
        super.setUp();
        MultiTenantContextTestUtils.setupMultiTenantSystem();
        issue = new MockIssue();
        issueManagerCtrl = MockControl.createStrictControl(IssueManager.class);
        issueManager = (IssueManager) issueManagerCtrl.getMock();
        issueManager.getIssueObject(ISSUE_KEY_FOO_1);
        issueManagerCtrl.setReturnValue(issue);
        issueManagerCtrl.replay();

        user = OSUserConverter.convertToOSUser(new MockUser("frank"));

        authContext = (JiraAuthenticationContext) DuckTypeProxy.getProxy(JiraAuthenticationContext.class, new Object());
    }


    protected void tearDown() throws Exception
    {
        super.tearDown();
        MultiTenantContext.setFactory(null);
        issueManager = null;
        issueManagerCtrl = null;
    }


    public void testAddWorklogNewEstimateHappyPath() throws RemoteException
    {
        // setup the remoteworklog we are going to send to issue service
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        String newRemainingEstimate = "1h";

        Worklog worklog = getSimpleWorklog();
        WorklogResult newEstimateResult = WorklogResultFactory.createNewEstimate(worklog, new Long(0));

        Mock worklogServiceMock = new Mock(WorklogService.class);
        WorklogNewEstimateInputParameters expectedParams = WorklogInputParametersImpl
                .issue(issue)
                .timeSpent("60m")
                .startDate(remoteWorklog.getStartDate())
                .comment(remoteWorklog.getComment())
                .groupLevel(remoteWorklog.getGroupLevel())
                .roleLevelId(remoteWorklog.getRoleLevelId())
                .newEstimate(newRemainingEstimate)
                .buildNewEstimate();

        worklogServiceMock.expectAndReturn("validateCreateWithNewEstimate",
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(expectedParams),
                },
                newEstimateResult);
        worklogServiceMock.expectAndReturn("createWithNewRemainingEstimate",
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(newEstimateResult),
                        P.eq(Boolean.TRUE)
                }, worklog);
        WorklogService worklogService = (WorklogService) worklogServiceMock.proxy();

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        service.addWorklogWithNewRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog, newRemainingEstimate);

        issueManagerCtrl.verify();
        worklogServiceMock.verify();
    }

    public void testAddWorklogNewEstimateValidateExceptionPath()
    {

        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();

        String newRemainingEstimate = "1h";

        WorklogService worklogService = createValidateFailingWorklogService();
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.addWorklogWithNewRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog, newRemainingEstimate);
            fail("expected to get exception");
        }
        catch (RemoteValidationException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }

    }


    public void testAddWorklogValidateNewEstimateUnknownExceptionPath()
    {
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, EasyList.build(new Object()), DuckTypeProxy.RETURN_NULL);
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        String newRemainingEstimate = "1h";

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.addWorklogWithNewRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog, newRemainingEstimate);
            fail("expected exception due to WorklogService.validateCreateWithNewEstimate returning null");
        }
        catch (RemoteValidationException expected)
        {
            assertTrue(expected.getMessage().indexOf("error.unexpected.condition") != -1);
        }
        catch (RemoteException notQuite)
        {
            fail("expected RemoteValidationException");
        }
    }

    public void testAddWorklogValidateAutoEstimateUnknownExceptionPath()
    {
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, EasyList.build(new Object()), DuckTypeProxy.RETURN_NULL);
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.addWorklogAndAutoAdjustRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog);
            fail("expected exception due to WorklogService.validateCreateWithNewEstimate returning null");
        }
        catch (RemoteValidationException expected)
        {
            assertTrue(expected.getMessage().indexOf("error.unexpected.condition") != -1);
        }
        catch (RemoteException notQuite)
        {
            fail("expected RemoteValidationException");
        }
    }

    public void testAddWorklogValidateRetainRemainingEstimateUnknownExceptionPath()
    {
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, EasyList.build(new Object()), DuckTypeProxy.RETURN_NULL);
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.addWorklogAndRetainRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog);
            fail("expected exception due to WorklogService.validateCreateWithNewEstimate returning null");
        }
        catch (RemoteValidationException expected)
        {
            assertTrue(expected.getMessage().indexOf("error.unexpected.condition") != -1);
        }
        catch (RemoteException notQuite)
        {
            fail("expected RemoteValidationException");
        }
    }


    public void testAddWorklogNewEstimateCreateExceptionPath()
    {
        Object worklogServiceDuck = new Object()
        {
            public WorklogResult validateCreateWithNewEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateInputParameters params)
            {
                return WorklogResultFactory.createNewEstimate((Worklog) null, null);
            }

            public Worklog createWithNewRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateResult worklogResult, boolean dispatchEvent)
            {
                jiraServiceContext.getErrorCollection().addErrorMessages(ERROR_COLLECTION);
                return null;
            }
        };
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, worklogServiceDuck);
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        String newRemainingEstimate = "1h";

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.addWorklogWithNewRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog, newRemainingEstimate);
            fail("expected exception due to WorklogService.createWithNewRemainingEstimate returning null");
        }
        catch (RemoteException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
    }

    public void testGetIssueFromKey()
    {
        Object issueManagerDuck = new Object()
        {
            public MutableIssue getIssueObject(String key) throws DataAccessException
            {
                throw new DataAccessException("Our data access layer was thrown");
            }
        };
        issueManager = (IssueManager) DuckTypeProxy.getProxy(IssueManager.class, issueManagerDuck);

        IssueServiceImpl service = getIssueServiceImpl(null);
        try
        {
            service.getIssueFromKey(ISSUE_KEY_FOO_1);
            fail("expected exception due to IssueManager.getIssueObject returning a Data exception");
        }
        catch (RemoteException expected)
        {
            String mesg = expected.getMessage();
            assertTrue(mesg.indexOf("Our data access layer was thrown") != -1);
        }
    }

    /* ======================================================================
        =====================================================================
        AutoAdjustRemainingEstimate
        =====================================================================*/
    public void testAddWorklogAutoEstimateHappyPath() throws RemoteException
    {
        // setup the remoteworklog we are going to send to issue service
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        Mock worklogServiceMock = createHappyCreateWorklogServiceMock(remoteWorklog, "validateCreate", "createAndAutoAdjustRemainingEstimate");
        WorklogService worklogService = (WorklogService) worklogServiceMock.proxy();
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        service.addWorklogAndAutoAdjustRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog);

        issueManagerCtrl.verify();
        worklogServiceMock.verify();
    }


    public void testAddWorklogAutoEstimateValidateExceptionPath()
    {
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        Object worklogServiceDuck = new Object()
        {
            public WorklogResult validateCreate(JiraServiceContext jiraServiceContext, WorklogInputParameters params)
            {
                jiraServiceContext.getErrorCollection().addErrorMessages(ERROR_COLLECTION);
                return null;
            }
        };
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, worklogServiceDuck);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.addWorklogAndAutoAdjustRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog);
            fail("expected to get exception");
        }
        catch (RemoteValidationException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }

    public void testAddWorklogAutoEstimateCreateExceptionPath()
    {
        final Worklog worklog = getSimpleWorklog();
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);
        Object worklogServiceDuck = new Object()
        {
            public WorklogResult validateCreate(JiraServiceContext jiraServiceContext, WorklogInputParameters params)
            {
                return worklogResult;
            }

            public Worklog createAndAutoAdjustRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent)
            {
                jiraServiceContext.getErrorCollection().addErrorMessages(ERROR_COLLECTION);
                return null;
            }
        };
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, worklogServiceDuck);
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.addWorklogAndAutoAdjustRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog);
            fail("expected exception due to WorklogService.createAndAutoAdjustRemainingEstimate returning null");
        }
        catch (RemoteException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
    }

    /* ======================================================================
        =====================================================================
        RetainRemainingEstimate
        =====================================================================*/
    public void testAddWorklogRetainRemainingEstimateHappyPath() throws RemoteException
    {
        // setup the remoteworklog we are going to send to issue service
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();

        Mock worklogServiceMock = createHappyCreateWorklogServiceMock(remoteWorklog, "validateCreate", "createAndRetainRemainingEstimate");

        WorklogService worklogService = (WorklogService) worklogServiceMock.proxy();

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        service.addWorklogAndRetainRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog);

        issueManagerCtrl.verify();
        worklogServiceMock.verify();
    }

    public void testAddWorklogRetainRemainingEstimateValidateExceptionPath()
    {
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        Object worklogServiceDuck = new Object()
        {
            public WorklogResult validateCreate(JiraServiceContext jiraServiceContext, WorklogInputParameters params)
            {
                jiraServiceContext.getErrorCollection().addErrorMessages(ERROR_COLLECTION);
                return null;
            }
        };
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, worklogServiceDuck);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.addWorklogAndRetainRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog);
            fail("expected to get exception");
        }
        catch (RemoteValidationException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }

    public void testAddWorklogRetainRemainingEstimateCreateExceptionPath()
    {
        final Worklog worklog = getSimpleWorklog();
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);
        Object worklogServiceDuck = new Object()
        {
            public WorklogResult validateCreate(JiraServiceContext jiraServiceContext, WorklogInputParameters params)
            {
                return worklogResult;
            }

            public Worklog createAndRetainRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent)
            {
                jiraServiceContext.getErrorCollection().addErrorMessages(ERROR_COLLECTION);
                return null;
            }
        };
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, worklogServiceDuck);
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.addWorklogAndRetainRemainingEstimate(user, ISSUE_KEY_FOO_1, remoteWorklog);
            fail("expected exception due to WorklogService.createAndRetainRemainingEstimate returning null");
        }
        catch (RemoteException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
    }

    public void testGetWorklogsHappyPath() throws RemoteException, InvalidDurationException
    {
        final Worklog worklog1 = getSimpleWorklog(1);
        final Worklog worklog2 = getSimpleWorklog(2);
        Object worklogServiceDuck = new Object()
        {
            public List getByIssueVisibleToUser(JiraServiceContext jiraServiceContext, Issue issue)
            {
                return EasyList.build(worklog1, worklog2);
            }
        };
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, worklogServiceDuck);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        RemoteWorklog[] remoteWorklogs = service.getWorklogs(user, ISSUE_KEY_FOO_1);
        assertEquals(2, remoteWorklogs.length);
        RemoteWorklog remoteWorklog1;
        RemoteWorklog remoteWorklog2;
        if (remoteWorklogs[0].getId().equals("1"))
        {
            remoteWorklog1 = remoteWorklogs[0];
            remoteWorklog2 = remoteWorklogs[1];
        }
        else
        {
            remoteWorklog1 = remoteWorklogs[1];
            remoteWorklog2 = remoteWorklogs[0];
        }
        assertWorklogFieldsSameAsRemoteWorklog(worklog1, remoteWorklog1);
        assertWorklogFieldsSameAsRemoteWorklog(worklog2, remoteWorklog2);
    }

    public void testGetWorklogsEmptyArray() throws RemoteException, InvalidDurationException
    {
        Object worklogServiceDuck = new Object()
        {
            public List getByIssueVisibleToUser(JiraServiceContext jiraServiceContext, Issue issue)
            {
                return Collections.EMPTY_LIST;
            }
        };
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, worklogServiceDuck);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        RemoteWorklog[] remoteWorklogs = service.getWorklogs(user, ISSUE_KEY_FOO_1);
        assertNotNull(remoteWorklogs);
        assertEquals(0, remoteWorklogs.length);
    }

    /*
   =====================================================
       TEST UPDATE*
   =====================================================
    */

    public void testUpdateWorklogRetainRemainingEstimateHappyPath() throws RemoteException
    {
        // setup the remoteworklog we are going to send to issue service
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        remoteWorklog.setId("123");
        Mock worklogServiceMock = createHappyUpdateWorklogServiceMock(remoteWorklog, "validateUpdate", "updateAndRetainRemainingEstimate");

        WorklogService worklogService = (WorklogService) worklogServiceMock.proxy();

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        service.updateWorklogAndRetainRemainingEstimate(user, remoteWorklog);

        worklogServiceMock.verify();
    }

    public void testUpdateWorklogAutoEstimateHappyPath() throws RemoteException
    {
        // setup the remoteworklog we are going to send to issue service
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        remoteWorklog.setId("10101");
        Mock worklogServiceMock = createHappyUpdateWorklogServiceMock(remoteWorklog, "validateUpdate", "updateAndAutoAdjustRemainingEstimate");

        WorklogService worklogService = (WorklogService) worklogServiceMock.proxy();

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        service.updateWorklogAndAutoAdjustRemainingEstimate(user, remoteWorklog);

        worklogServiceMock.verify();
    }

    public void testUpdateWorklogNewEstimateHappyPath() throws RemoteException
    {
        // setup the remoteworklog we are going to send to issue service
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        remoteWorklog.setId("10101");
        Worklog worklog = getSimpleWorklog();
        Mock worklogServiceMock = new Mock(WorklogService.class);
        Long newEstimateInSeconds = new Long(5 * 60 * 60);
        String newEstimateStr = "5h";
        final WorklogResult worklogResult = WorklogResultFactory.createNewEstimate(worklog, newEstimateInSeconds);

        WorklogNewEstimateInputParameters expectedParams = WorklogInputParametersImpl
                .timeSpent("60m")
                .worklogId(Long.valueOf(remoteWorklog.getId()))
                .startDate(remoteWorklog.getStartDate())
                .comment(remoteWorklog.getComment())
                .groupLevel(remoteWorklog.getGroupLevel())
                .roleLevelId(remoteWorklog.getRoleLevelId())
                .newEstimate(newEstimateStr)
                .buildNewEstimate();
        worklogServiceMock.expectAndReturn("validateUpdateWithNewEstimate",
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(expectedParams)
                },
                worklogResult);


        worklogServiceMock.expectAndReturn("updateWithNewRemainingEstimate",
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(worklogResult),
                        P.eq(Boolean.TRUE)
                }, worklog);

        WorklogService worklogService = (WorklogService) worklogServiceMock.proxy();

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        service.updateWorklogWithNewRemainingEstimate(user, remoteWorklog, newEstimateStr);

        worklogServiceMock.verify();
    }

    public void testUpdateWorklogNewEstimateValidateExceptionPath()
    {
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        remoteWorklog.setId("555");
        String newRemainingEstimate = "1h";

        WorklogService worklogService = createValidateFailingWorklogService();
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.updateWorklogWithNewRemainingEstimate(user, remoteWorklog, newRemainingEstimate);
            fail("expected to get exception");
        }
        catch (RemoteValidationException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }

    }

    public void testUpdateWorklogRetainRemainingEstimateValidateExceptionPath()
    {
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        remoteWorklog.setId("555");
        WorklogService worklogService = createValidateFailingWorklogService();
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.updateWorklogAndRetainRemainingEstimate(user, remoteWorklog);
            fail("expected exception");
        }
        catch (RemoteValidationException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }

    public void testUpdateWorklogAutoEstimateValidateExceptionPath()
    {
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        remoteWorklog.setId("555");
        WorklogService worklogService = createValidateFailingWorklogService();
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.updateWorklogAndAutoAdjustRemainingEstimate(user, remoteWorklog);
            fail("expected exception");
        }
        catch (RemoteValidationException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }

    public void testUpdateWorklogNewEstimateUnknownExceptionPath()
    {
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        remoteWorklog.setId("555");
        String newRemainingEstimate = "1h";

        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, EasyList.build(new Object()), DuckTypeProxy.RETURN_NULL);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.updateWorklogWithNewRemainingEstimate(user, remoteWorklog, newRemainingEstimate);
            fail("expected to get exception");
        }
        catch (RemoteValidationException expected)
        {
            assertTrue(expected.getMessage().indexOf("error.unexpected.condition") != -1);
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }

    }

    public void testUpdateWorklogRetainRemainingEstimateUnknownExceptionPath()
    {
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        remoteWorklog.setId("555");
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, EasyList.build(new Object()), DuckTypeProxy.RETURN_NULL);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.updateWorklogAndRetainRemainingEstimate(user, remoteWorklog);
            fail("expected exception");
        }
        catch (RemoteValidationException expected)
        {
            assertTrue(expected.getMessage().indexOf("error.unexpected.condition") != -1);
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }

    public void testUpdateWorklogAutoEstimateUnknownExceptionPath()
    {
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        remoteWorklog.setId("555");
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, EasyList.build(new Object()), DuckTypeProxy.RETURN_NULL);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.updateWorklogAndAutoAdjustRemainingEstimate(user, remoteWorklog);
            fail("expected exception");
        }
        catch (RemoteValidationException expected)
        {
            assertTrue(expected.getMessage().indexOf("error.unexpected.condition") != -1);
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }


    /**
     * Extra test to see that failure to parse the id results in the expected
     * RemoteValidationException.
     *
     * @throws RemoteException if wrong exception is thrown.
     */
    public void testUpdateWorklogValidationFailure() throws RemoteException
    {
        RemoteWorklog remoteWorklog = getSimpleRemoteWorklog();
        remoteWorklog.setId(null);
        IssueServiceImpl service = getIssueServiceImpl(null);
        try
        {
            service.updateWorklogAndRetainRemainingEstimate(user, remoteWorklog);
            fail("expected RemoteValidationException");
        }
        catch (RemoteValidationException e)
        {
            assertTrue(e.getMessage().indexOf("NumberFormatException") != -1);
        }
        remoteWorklog.setId("notaNumber");
        try
        {
            service.updateWorklogAndRetainRemainingEstimate(user, remoteWorklog);
            fail("expected RemoteValidationException");
        }
        catch (RemoteValidationException e)
        {
            assertTrue(e.getMessage().indexOf("NumberFormatException") != -1);
        }
    }

    /*
   =====================================================
       TEST DELETE
   =====================================================
    */

    public void testDeleteWorklogRetainRemainingHappyPath() throws RemoteException
    {
        String remoteWorklogId = "123";
        Mock worklogServiceMock = createHappyDeleteWorklogServiceMock(remoteWorklogId, "validateDelete", "deleteAndRetainRemainingEstimate");

        WorklogService worklogService = (WorklogService) worklogServiceMock.proxy();

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        service.deleteWorklogAndRetainRemainingEstimate(user, remoteWorklogId);

        worklogServiceMock.verify();
    }


    public void testDeleteWorklogAutoEstimateHappyPath() throws RemoteException
    {
        String remoteWorklogId = "123";
        Mock worklogServiceMock = createHappyDeleteWorklogServiceMock(remoteWorklogId, "validateDelete", "deleteAndAutoAdjustRemainingEstimate");

        WorklogService worklogService = (WorklogService) worklogServiceMock.proxy();

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        service.deleteWorklogAndAutoAdjustRemainingEstimate(user, remoteWorklogId);

        worklogServiceMock.verify();
    }

    public void testDeleteWorklogNewEstimateHappyPath() throws RemoteException
    {
        String remoteWorklogId = "123";
        String newEstimate = "15h";
        Long newEstimateInSeconds = new Long(15 * 60 * 60);
        WorklogResult worklogNewEstimate = WorklogResultFactory.createNewEstimate(getSimpleWorklog(), newEstimateInSeconds);
        Mock worklogServiceMock = new Mock(WorklogService.class);
        worklogServiceMock.expectAndReturn("validateDeleteWithNewEstimate",
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(new Long(remoteWorklogId)),
                        P.eq(newEstimate)
                },
                worklogNewEstimate);

        worklogServiceMock.expectAndReturn("deleteWithNewRemainingEstimate",
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(worklogNewEstimate),
                        P.eq(Boolean.TRUE)
                }, Boolean.TRUE);
        WorklogService worklogService = (WorklogService) worklogServiceMock.proxy();

        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        service.deleteWorklogWithNewRemainingEstimate(user, remoteWorklogId, newEstimate);

        worklogServiceMock.verify();
    }

    public void testDeleteWorklogRetainRemainingValidateExceptionPath()
    {
        String remoteWorklogId = "555";
        WorklogService worklogService = createValidateFailingWorklogService();
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.deleteWorklogAndRetainRemainingEstimate(user, remoteWorklogId);
            fail("expected exception");
        }
        catch (RemoteValidationException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }

    public void testDeleteWorklogAutoEstimateValidateExceptionPath()
    {
        String remoteWorklogId = "555";
        WorklogService worklogService = createValidateFailingWorklogService();
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.deleteWorklogAndAutoAdjustRemainingEstimate(user, remoteWorklogId);
            fail("expected exception");
        }
        catch (RemoteValidationException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }

    public void testDeleteWorklogNewEstimateValidateExceptionPath()
    {
        String newEstimate = "5h";
        String remoteWorklogId = "555";
        WorklogService worklogService = createValidateFailingWorklogService();
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.deleteWorklogWithNewRemainingEstimate(user, remoteWorklogId, newEstimate);
            fail("expected exception");
        }
        catch (RemoteValidationException expected)
        {
            String mesg = expected.getMessage();
            for (Iterator iterator = ERROR_COLLECTION.iterator(); iterator.hasNext();)
            {
                assertTrue(mesg.indexOf((String) iterator.next()) != -1);
            }
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }

    public void testDeleteWorklogRetainRemainingUnknownExceptionPath()
    {
        String remoteWorklogId = "555";
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, EasyList.build(new Object()), DuckTypeProxy.RETURN_NULL);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.deleteWorklogAndRetainRemainingEstimate(user, remoteWorklogId);
            fail("expected exception");
        }
        catch (RemoteValidationException expected)
        {
            assertTrue(expected.getMessage().indexOf("error.unexpected.condition") != -1);
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }

    public void testDeleteWorklogAutoEstimateUnknownExceptionPath()
    {
        String remoteWorklogId = "555";
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, EasyList.build(new Object()), DuckTypeProxy.RETURN_NULL);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.deleteWorklogAndAutoAdjustRemainingEstimate(user, remoteWorklogId);
            fail("expected exception");
        }
        catch (RemoteValidationException expected)
        {
            assertTrue(expected.getMessage().indexOf("error.unexpected.condition") != -1);
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }

    public void testDeleteWorklogNewEstimateUnknownExceptionPath()
    {
        String remoteWorklogId = "555";
        WorklogService worklogService = (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, EasyList.build(new Object()), DuckTypeProxy.RETURN_NULL);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            service.deleteWorklogWithNewRemainingEstimate(user, remoteWorklogId, "1d");
            fail("expected exception");
        }
        catch (RemoteValidationException expected)
        {
            assertTrue(expected.getMessage().indexOf("error.unexpected.condition") != -1);
        }
        catch (RemoteException notQuite)
        {
            fail("wrong exception type caught, expected RemoteValidationException");
        }
    }

    /*
   =====================================================
       Permissons and Enabled Methods
   =====================================================
    */


    public void testHasPermissionToCreateHappy()
    {
        WorklogService worklogService = createEnabledTimeTrackingWorklogService(true, true, false, false);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            boolean ok = service.hasPermissionToCreateWorklog(user, ISSUE_KEY_FOO_1);
            assertTrue(ok);
        }
        catch (RemoteException e)
        {
            fail("Not expecting RemoteException - service.hasPermissionToCreateWorklog");
        }
    }

    public void testHasPermissionToCreateSad()
    {
        WorklogService worklogService = createEnabledTimeTrackingWorklogService(true, false, false, false);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            boolean ok = service.hasPermissionToCreateWorklog(user, ISSUE_KEY_FOO_1);
            assertFalse(ok);
        }
        catch (RemoteException e)
        {
            fail("Not expecting RemoteException - service.hasPermissionToCreateWorklog");
        }
    }


    public void testHasPermissionToDeleteHappy()
    {
        WorklogService worklogService = createEnabledTimeTrackingWorklogService(true, false, true, false);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            boolean ok = service.hasPermissionToDeleteWorklog(user, "123");
            assertTrue(ok);
        }
        catch (RemoteException e)
        {
            fail("Not expecting RemoteException - service.hasPermissionToDeleteWorklog");
        }
    }

    public void testHasPermissionToDeleteSad()
    {
        WorklogService worklogService = createEnabledTimeTrackingWorklogService(true, false, false, false);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            boolean ok = service.hasPermissionToDeleteWorklog(user, "123");
            assertFalse(ok);
        }
        catch (RemoteException e)
        {
            fail("Not expecting RemoteException - service.hasPermissionToDeleteWorklog");
        }
    }

    public void testHasPermissionToUpdateHappy()
    {
        WorklogService worklogService = createEnabledTimeTrackingWorklogService(true, false, false, true);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            boolean ok = service.hasPermissionToUpdateWorklog(user, "123");
            assertTrue(ok);
        }
        catch (RemoteException e)
        {
            fail("Not expecting RemoteException - service.hasPermissionToUpdateWorklog");
        }
    }

    public void testHasPermissionToUpdateSad()
    {
        WorklogService worklogService = createEnabledTimeTrackingWorklogService(true, false, false, false);
        IssueServiceImpl service = getIssueServiceImpl(worklogService);
        try
        {
            boolean ok = service.hasPermissionToUpdateWorklog(user, "123");
            assertFalse(ok);
        }
        catch (RemoteException e)
        {
            fail("Not expecting RemoteException - service.hasPermissionToUpdateWorklog");
        }
    }

    /*
   =====================================================
       HELPER METHODS
   =====================================================
    */

    private void assertWorklogFieldsSameAsRemoteWorklog(Worklog worklog, RemoteWorklog remoteWorklog) throws InvalidDurationException
    {
        assertEquals(worklog.getId().toString(), remoteWorklog.getId());
        assertEquals(worklog.getAuthor(), remoteWorklog.getAuthor());
        assertEquals(worklog.getComment(), remoteWorklog.getComment());
        assertEquals(worklog.getCreated(), remoteWorklog.getCreated());
        assertEquals(worklog.getGroupLevel(), remoteWorklog.getGroupLevel());
        assertEquals(worklog.getRoleLevelId().toString(), remoteWorklog.getRoleLevelId());
        assertEquals(worklog.getStartDate(), remoteWorklog.getStartDate());
        assertEquals(worklog.getUpdateAuthor(), remoteWorklog.getUpdateAuthor());
        assertEquals(worklog.getUpdated(), remoteWorklog.getUpdated());

        assertEquals(worklog.getTimeSpent().longValue(), remoteWorklog.getTimeSpentInSeconds());

    }


    /**
     * Creates a WorklogService Mock that validates that the values in the given RemoteWorklog
     * are passed into the service's update methods.
     *
     * @param remoteWorklog the RemoteWorklog whose values we verify come into the service.
     * @return the Mock.
     */
    private Mock createHappyUpdateWorklogServiceMock(RemoteWorklog remoteWorklog, String validateUpdateMethodName, String updateMethodName)
    {
        Worklog worklog = getSimpleWorklog();
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);

        Mock worklogServiceMock = new Mock(WorklogService.class);
        WorklogInputParameters expectedParams = WorklogInputParametersImpl
                .timeSpent("60m")
                .worklogId(Long.valueOf(remoteWorklog.getId()))
                .startDate(remoteWorklog.getStartDate())
                .comment(remoteWorklog.getComment())
                .groupLevel(remoteWorklog.getGroupLevel())
                .roleLevelId(remoteWorklog.getRoleLevelId())
                .build();

        worklogServiceMock.expectAndReturn(validateUpdateMethodName,
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(expectedParams)
                },
                worklogResult);


        worklogServiceMock.expectAndReturn(updateMethodName,
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(worklogResult),
                        P.eq(Boolean.TRUE)
                }, worklog);
        return worklogServiceMock;
    }

    private Mock createHappyDeleteWorklogServiceMock(String remoteWorklogId, String validateDeleteMethodName, String deleteMethodName)
    {
        Worklog worklog = getSimpleWorklog();
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);

        Mock worklogServiceMock = new Mock(WorklogService.class);
        worklogServiceMock.expectAndReturn(validateDeleteMethodName,
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(new Long(remoteWorklogId)),
                },
                worklogResult);


        worklogServiceMock.expectAndReturn(deleteMethodName,
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(worklogResult),
                        P.eq(Boolean.TRUE)
                }, Boolean.TRUE);
        return worklogServiceMock;
    }


    /**
     * Creates a WorklogService Mock that validates that the values in the given RemoteWorklog
     * are passed into the service.
     *
     * @param remoteWorklog the RemoteWorklog whose values we verify come into the service.
     * @return the Mock.
     */
    private Mock createHappyCreateWorklogServiceMock(RemoteWorklog remoteWorklog, String validateCreateMethodName, String createMethodName)
    {
        Worklog worklog = getSimpleWorklog();
        WorklogResult worklogResult = WorklogResultFactory.create(worklog);
        Mock worklogServiceMock = new Mock(WorklogService.class);

        WorklogInputParameters expectedParams = WorklogInputParametersImpl
                .issue(issue)
                .timeSpent("60m")
                .startDate(remoteWorklog.getStartDate())
                .comment(remoteWorklog.getComment())
                .groupLevel(remoteWorklog.getGroupLevel())
                .roleLevelId(remoteWorklog.getRoleLevelId())
                .build();

        worklogServiceMock.expectAndReturn(validateCreateMethodName,
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(expectedParams)
                },
                worklogResult);


        worklogServiceMock.expectAndReturn(createMethodName,
                new Constraint[] {
                        P.isA(JiraServiceContext.class),
                        P.eq(worklogResult),
                        P.eq(Boolean.TRUE)
                }, worklog);
        return worklogServiceMock;
    }


    private WorklogService createEnabledTimeTrackingWorklogService(final boolean enabled, final boolean createPerm, final boolean deletePerm, final boolean updatePerm)
    {
        Object worklogServiceDuck = new Object()
        {
            public boolean isTimeTrackingEnabled()
            {
                return enabled;
            }

            public boolean hasPermissionToCreate(JiraServiceContext jiraServiceContext, Issue issue, boolean isEditableCheckRequired)
            {
                return enabled && createPerm;
            }

            public boolean hasPermissionToDelete(JiraServiceContext jiraServiceContext, Worklog worklog)
            {
                return enabled && deletePerm;
            }

            public boolean hasPermissionToUpdate(JiraServiceContext jiraServiceContext, Worklog worklog)
            {
                return enabled && updatePerm;
            }

            public Worklog getById(JiraServiceContext jiraServiceContext, Long id)
            {
                return getSimpleWorklog();
            }
        };
        return (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, worklogServiceDuck);
    }

    private WorklogService createValidateFailingWorklogService()
    {
        Object worklogServiceDuck = new Object()
        {
            WorklogResult validateNewEstimate(JiraServiceContext jiraServiceContext)
            {
                jiraServiceContext.getErrorCollection().addErrorMessages(ERROR_COLLECTION);
                return null;
            }

            WorklogResult validateWorklog(JiraServiceContext jiraServiceContext)
            {
                jiraServiceContext.getErrorCollection().addErrorMessages(ERROR_COLLECTION);
                return null;
            }

            public WorklogResult validateCreateWithNewEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateInputParameters params)
            {
                return validateNewEstimate(jiraServiceContext);
            }

            public WorklogResult validateCreate(JiraServiceContext jiraServiceContext, WorklogInputParameters params)
            {
                return validateWorklog(jiraServiceContext);
            }

            public WorklogResult validateDeleteWithNewEstimate(JiraServiceContext jiraServiceContext, Long worklogId, String newEstimate)
            {
                return validateNewEstimate(jiraServiceContext);
            }

            public WorklogResult validateDelete(JiraServiceContext jiraServiceContext, Long worklogId)
            {
                return validateWorklog(jiraServiceContext);
            }

            public WorklogResult validateUpdateWithNewEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateInputParameters params)
            {
                return validateNewEstimate(jiraServiceContext);
            }

            public WorklogResult validateUpdate(JiraServiceContext jiraServiceContext, WorklogInputParameters params)
            {
                return validateWorklog(jiraServiceContext);
            }

            public Worklog getById(JiraServiceContext jiraServiceContext, Long id)
            {
                jiraServiceContext.getErrorCollection().addErrorMessages(ERROR_COLLECTION);
                return null;
            }
        };
        return (WorklogService) DuckTypeProxy.getProxy(WorklogService.class, worklogServiceDuck);
    }


    /**
     * @return Creates a simple RemoteWorklog object with most stuff filled in
     */
    private RemoteWorklog getSimpleRemoteWorklog()
    {
        Date now = new Date();
        RemoteWorklog remoteWorklog = new RemoteWorklogImpl("123", null, null, now, now, "60m", 3600);
        remoteWorklog.setStartDate(now);
        remoteWorklog.setComment("Some comment for the worklog");
        remoteWorklog.setRoleLevelId("3456");
        remoteWorklog.setGroupLevel("someGroup");
        return remoteWorklog;
    }


    /*
     * Builds our service implementation with I18n support and a specirfied worklog service
     */
    private IssueServiceImpl getIssueServiceImpl(WorklogService worklogService)
    {
        Object i18nDuck = new Object()
        {
            public String getText(String key)
            {
                return key;
            }

            public String getText(String key, String val)
            {
                return key + " " + val;
            }
        };
        final I18nHelper i18nHelper = (I18nHelper) DuckTypeProxy.getProxy(I18nHelper.class, EasyList.build(i18nDuck), DuckTypeProxy.RETURN_NULL);

        final JiraDurationUtils jiraDurationUtils = new MockJiraDurationUtils();

        return new IssueServiceImpl(
                null,
                null,
                issueManager,
                null,
                null,
                null,
                null,
                null,
                authContext,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                worklogService,
                jiraDurationUtils, null, null)
        {

            I18nHelper getI18nHelper()
            {
                return i18nHelper;
            }
        };
    }


    private Worklog getSimpleWorklog()
    {
        return getSimpleWorklog(1);
    }

    private Worklog getSimpleWorklog(long wid)
    {
        Long l = new Long(wid);
        Long timeSpent = new Long(3600);
        Worklog worklog = new WorklogImpl(null, null, new Long(wid), "author" + wid, "comment" + wid, new Date(wid), "groupLevel" + wid, l, timeSpent)
        {
            public Issue getIssue()
            {
                return issue;
            }
        };
        return worklog;
    }

    private static class MockJiraDurationUtils extends JiraDurationUtils
    {

        public MockJiraDurationUtils()
        {
            super(null, null, null, null, null);
        }

        public void updateFormatters(ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
        {

        }

        public String getFormattedDuration(Long duration)
        {
            return "";
        }

        public String getFormattedDuration(Long duration, Locale locale)
        {
            return "";
        }
    }
}
