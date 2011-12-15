package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.plugin.searchrequestview.auth.Authorizer;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.mock.MockIssue;
import com.atlassian.jira.rpc.mock.MockUser;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.atlassian.jira.rpc.soap.util.SoapUtilsBean;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.Collections;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

/**
 * @since v4.0
 */
public class TestSearchServiceImpl extends TestCase
{
    private IssueManager issueManager;
    private PermissionManager permissionManager;
    private User user;

    protected void setUp()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();

        user = OSUserConverter.convertToOSUser(new MockUser("admin"));

        issueManager = createMock(IssueManager.class);
        expect(issueManager.getIssueObject(EasyMock.anyLong())).andStubReturn(new MockIssue());

        permissionManager = createMock(PermissionManager.class);
        expect(permissionManager.hasPermission(EasyMock.anyInt(), EasyMock.<Issue>anyObject(), EasyMock.same(user))).andStubReturn(true);
    }

    protected void tearDown()
    {
        MultiTenantContext.setFactory(null);
    }

    public void testNormalQuery() throws Exception
    {
        final String jqlSearch = "";
        final int maxNumResults = 10;

        final QueryImpl query = new QueryImpl();

        final SearchResults results = new SearchResults(Collections.<Issue>emptyList(), new PagerFilter(maxNumResults));
        final SearchService.ParseResult parseResult = new SearchService.ParseResult(query, new MessageSetImpl());

        // return OK authorization for any params
        final Authorizer requestAuthorizer = createMock(Authorizer.class);
        expect(requestAuthorizer.isSearchRequestAuthorized(EasyMock.eq(user), (SearchRequest) EasyMock.anyObject(), (SearchRequestParams) EasyMock.anyObject())).andReturn(Authorizer.Result.OK);

        // set up the SearchService facade
        final com.atlassian.jira.bc.issue.search.SearchService theRealSearchService = createMock(SearchService.class);
        expect(theRealSearchService.parseQuery(EasyMock.eq(user), EasyMock.eq(jqlSearch))).andReturn(parseResult);
        expect(theRealSearchService.validateQuery(EasyMock.eq(user), EasyMock.eq(parseResult.getQuery()))).andReturn(new MessageSetImpl());
        expect(theRealSearchService.search(EasyMock.eq(user), EasyMock.eq(parseResult.getQuery()), (PagerFilter) EasyMock.anyObject())).andReturn(results);

        EasyMock.replay(requestAuthorizer, theRealSearchService);

        final SearchRequestService searchRequestService = null;
        final CustomFieldManager customFieldManager = null;
        final ProjectManager projectManager = null;
        final AttachmentManager attachmentManager = null;
        final SearchProvider searchProvider = null;
        final SoapUtilsBean soapUtilsBean = null;

        final SearchServiceImpl searchService = new SearchServiceImpl(searchProvider, searchRequestService, customFieldManager, soapUtilsBean, projectManager, attachmentManager, requestAuthorizer, theRealSearchService, issueManager, permissionManager);

        final RemoteIssue[] resultIssues = searchService.getIssuesFromJqlSearch(user, jqlSearch, maxNumResults);
        assertEquals(0, resultIssues.length);

        EasyMock.verify(requestAuthorizer, theRealSearchService);
    }

    public void testSearchException() throws Exception
    {
        final String jqlSearch = "";
        final int maxNumResults = 10;

        final QueryImpl query = new QueryImpl();

        final SearchResults results = new SearchResults(Collections.<Issue>emptyList(), new PagerFilter(maxNumResults));
        final SearchService.ParseResult parseResult = new SearchService.ParseResult(query, new MessageSetImpl());

        // return OK authorization for any params
        final Authorizer requestAuthorizer = createMock(Authorizer.class);
        expect(requestAuthorizer.isSearchRequestAuthorized(EasyMock.eq(user), (SearchRequest) EasyMock.anyObject(), (SearchRequestParams) EasyMock.anyObject())).andReturn(Authorizer.Result.OK);

        // set up the SearchService facade
        final com.atlassian.jira.bc.issue.search.SearchService theRealSearchService = createMock(SearchService.class);
        expect(theRealSearchService.parseQuery(EasyMock.eq(user), EasyMock.eq(jqlSearch))).andReturn(parseResult);
        expect(theRealSearchService.validateQuery(EasyMock.eq(user), EasyMock.eq(parseResult.getQuery()))).andReturn(new MessageSetImpl());
        expect(theRealSearchService.search(EasyMock.eq(user), EasyMock.eq(parseResult.getQuery()), (PagerFilter) EasyMock.anyObject())).andThrow(new SearchException("error message"));

        EasyMock.replay(requestAuthorizer, theRealSearchService);

        final SearchRequestService searchRequestService = null;
        final CustomFieldManager customFieldManager = null;
        final ProjectManager projectManager = null;
        final AttachmentManager attachmentManager = null;
        final SearchProvider searchProvider = null;
        final SoapUtilsBean soapUtilsBean = null;

        final SearchServiceImpl searchService = new SearchServiceImpl(searchProvider, searchRequestService, customFieldManager, soapUtilsBean, projectManager, attachmentManager, requestAuthorizer, theRealSearchService, issueManager, permissionManager);

        try
        {
            searchService.getIssuesFromJqlSearch(user, jqlSearch, maxNumResults);
            fail();
        }
        catch (RemoteException expected)
        {
            final String errorStart = "Error occurred during searching: Caused by com.atlassian.jira.issue.search.SearchException: error message";
            assertTrue(expected.getMessage().startsWith(errorStart));
        }

        EasyMock.verify(requestAuthorizer, theRealSearchService);
    }

    public void testBadParse() throws Exception
    {
        final int maxNumResults = 10;
        final String jqlSearch = "bad bad";

        final MessageSetImpl messages = new MessageSetImpl();
        messages.addErrorMessage("an error message");
        final SearchService.ParseResult parseResult = new SearchService.ParseResult(new QueryImpl(), messages);

        // set up the SearchService facade
        final com.atlassian.jira.bc.issue.search.SearchService theRealSearchService = createMock(SearchService.class);
        expect(theRealSearchService.parseQuery(EasyMock.eq(user), EasyMock.eq(jqlSearch))).andReturn(parseResult);

        EasyMock.replay(theRealSearchService);

        final SearchRequestService searchRequestService = null;
        final CustomFieldManager customFieldManager = null;
        final SoapUtilsBean soapUtilsBean = null;
        final ProjectManager projectManager = null;
        final AttachmentManager attachmentManager = null;
        final SearchProvider searchProvider = null;
        final Authorizer requestAuthorizer = null;

        final SearchServiceImpl searchService = new SearchServiceImpl(searchProvider, searchRequestService, customFieldManager, soapUtilsBean, projectManager, attachmentManager, requestAuthorizer, theRealSearchService, issueManager, permissionManager);

        try
        {
            searchService.getIssuesFromJqlSearch(user, jqlSearch, maxNumResults);
            fail();
        }
        catch (RemoteException expected)
        {
            // expected
            assertEquals("Parsing failed: an error message", expected.getMessage().trim());
        }

        EasyMock.verify(theRealSearchService);
    }

    public void testAuthorizationFailure() throws Exception
    {
        final int maxNumResults = 10;
        final String jqlSearch = "key is not empty";
        final QueryImpl query = new QueryImpl(new TerminalClauseImpl("bob", "happy"));
        final Authorizer.Result authorizerResult = new Authorizer.Result()
        {
            public boolean isOK()
            {
                return false;
            }

            public String getReason()
            {
                return "bob is a bad person";
            }
        };

        final SearchResults results = new SearchResults(Collections.<Issue>emptyList(), new PagerFilter(maxNumResults));
        final SearchService.ParseResult parseResult = new SearchService.ParseResult(query, new MessageSetImpl());

        // return OK authorization for any params
        Authorizer requestAuthorizer = createMock(Authorizer.class);
        expect(requestAuthorizer.isSearchRequestAuthorized(EasyMock.eq(user), (SearchRequest) EasyMock.anyObject(), (SearchRequestParams) EasyMock.anyObject())).andReturn(authorizerResult);

        // set up the SearchService facade
        final com.atlassian.jira.bc.issue.search.SearchService theRealSearchService = createMock(SearchService.class);
        expect(theRealSearchService.parseQuery(EasyMock.eq(user), EasyMock.eq(jqlSearch))).andReturn(parseResult);

        EasyMock.replay(theRealSearchService, requestAuthorizer);

        final SearchRequestService searchRequestService = null;
        final CustomFieldManager customFieldManager = null;
        final SoapUtilsBean soapUtilsBean = null;
        final ProjectManager projectManager = null;
        final AttachmentManager attachmentManager = null;
        final SearchProvider searchProvider = null;

        final SearchServiceImpl searchService = new SearchServiceImpl(searchProvider, searchRequestService, customFieldManager, soapUtilsBean, projectManager, attachmentManager, requestAuthorizer, theRealSearchService, issueManager, permissionManager);

        try
        {
            searchService.getIssuesFromJqlSearch(user, jqlSearch, maxNumResults);
            fail();
        }
        catch (RemoteException expected)
        {
            // expected
            assertEquals("Error occurred during searching: 'bob is a bad person'.", expected.getMessage());
        }

        EasyMock.verify(theRealSearchService, requestAuthorizer);
    }

    public void testValidationFailure() throws Exception
    {
        final String jqlSearch = "";
        final int maxNumResults = 10;

        final QueryImpl query = new QueryImpl();

        final SearchService.ParseResult parseResult = new SearchService.ParseResult(query, new MessageSetImpl());

        // return OK authorization for any params
        final Authorizer requestAuthorizer = createMock(Authorizer.class);
        expect(requestAuthorizer.isSearchRequestAuthorized(EasyMock.eq(user), (SearchRequest) EasyMock.anyObject(), (SearchRequestParams) EasyMock.anyObject())).andReturn(Authorizer.Result.OK);

        // set up the SearchService facade
        final com.atlassian.jira.bc.issue.search.SearchService theRealSearchService = createMock(SearchService.class);
        expect(theRealSearchService.parseQuery(EasyMock.eq(user), EasyMock.eq(jqlSearch))).andReturn(parseResult);
        final MessageSetImpl validationMessageSet = new MessageSetImpl();
        validationMessageSet.addErrorMessage("error message");
        expect(theRealSearchService.validateQuery(EasyMock.eq(user), EasyMock.eq(parseResult.getQuery()))).andReturn(validationMessageSet);

        EasyMock.replay(requestAuthorizer, theRealSearchService);

        final SearchRequestService searchRequestService = null;
        final CustomFieldManager customFieldManager = null;
        final ProjectManager projectManager = null;
        final AttachmentManager attachmentManager = null;
        final SearchProvider searchProvider = null;
        final SoapUtilsBean soapUtilsBean = null;

        final SearchServiceImpl searchService = new SearchServiceImpl(searchProvider, searchRequestService, customFieldManager, soapUtilsBean, projectManager, attachmentManager, requestAuthorizer, theRealSearchService, issueManager, permissionManager);

        try
        {
            searchService.getIssuesFromJqlSearch(user, jqlSearch, maxNumResults);
            fail();
        }
        catch (RemoteException expected)
        {
            // expected
            assertEquals("Query validation failed: error message", expected.getMessage().trim());
        }

        EasyMock.verify(requestAuthorizer, theRealSearchService);
    }
}
