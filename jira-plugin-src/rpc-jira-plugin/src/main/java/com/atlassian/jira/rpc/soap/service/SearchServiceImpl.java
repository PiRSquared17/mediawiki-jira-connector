package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParamsImpl;
import com.atlassian.jira.plugin.searchrequestview.auth.Authorizer;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.atlassian.jira.rpc.soap.util.SoapUtilsBean;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.operator.Operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SearchServiceImpl implements SearchService
{
    private final SearchProvider searchProvider;
    private final SearchRequestService searchRequestService;
    private final CustomFieldManager customFieldManager;
    private final SoapUtilsBean soapUtilsBean;
    private final ProjectManager projectManager;
    private final AttachmentManager attachmentManager;
    private final Authorizer requestAuthorizer;
    private final com.atlassian.jira.bc.issue.search.SearchService theRealSearchService;
    private final IssueRetriever issueRetriever;

    public SearchServiceImpl(SearchProvider searchProvider, SearchRequestService searchRequestService, CustomFieldManager customFieldManager,
                             SoapUtilsBean soapUtilsBean, ProjectManager projectManager, AttachmentManager attachmentManager,
                             Authorizer requestAuthorizer, final com.atlassian.jira.bc.issue.search.SearchService theRealSearchService,
                             final IssueManager issueManager,
                             final PermissionManager permissionManager)
    {
        this.searchProvider = searchProvider;
        this.searchRequestService = searchRequestService;
        this.customFieldManager = customFieldManager;
        this.soapUtilsBean = soapUtilsBean;
        this.projectManager = projectManager;
        this.attachmentManager = attachmentManager;
        this.requestAuthorizer = requestAuthorizer;
        this.theRealSearchService = theRealSearchService;
        this.issueRetriever = new IssueRetriever(issueManager, permissionManager);
    }

    public RemoteIssue[] getIssues(User user, String filterId) throws RemoteException
    {
        // Run the search with an "unlimited filter"
        final PagerFilter unlimitedFilter = PagerFilter.getUnlimitedFilter();
        return getIssues(user, filterId, unlimitedFilter.getStart(), unlimitedFilter.getMax());
    }

    public RemoteIssue[] getIssues(User user, String filterId, int offSet, int maxNumResults) throws RemoteException
    {
        final JiraServiceContext ctx = new JiraServiceContextImpl(user);
        final SearchRequest loadedSR = searchRequestService.getFilter(ctx, new Long(filterId));

        if (loadedSR == null)
        {
            throw new RemoteValidationException("Could not find search request with id: " + filterId);
        }

        try
        {
            final PagerFilter pagerFilter = new PagerFilter(maxNumResults);
            pagerFilter.setStart(offSet);

            // Check the global search result limits and throw an exception if it is not cool.
            validateNumResultsRespectGlobalConstraints(user, loadedSR, pagerFilter);

            List issues = searchProvider.search(loadedSR.getQuery(), user, pagerFilter).getIssues();

            return convertIssueObjectsToRemoteIssues(issues, user);
        }
        catch (SearchException e)
        {
            throw new RemoteException("Error occurred during searching: ", e);
        }
    }

    public long getIssueCountForFilter(User user, String filterId) throws RemoteException
    {
        // Get the SearchRequest
        final JiraServiceContext ctx = new JiraServiceContextImpl(user);
        final SearchRequest searchRequest = searchRequestService.getFilter(ctx, new Long(filterId));

        if (searchRequest == null)
        {
            throw new RemoteValidationException("Could not find search request with id: " + filterId);
        }

        try
        {
            return searchProvider.searchCount(searchRequest.getQuery(), user);
        }
        catch (SearchException e)
        {
            throw new RemoteException("Error occurred during searching: ", e);
        }
    }

    private RemoteIssue[] convertIssueObjectsToRemoteIssues(List issues, User user) throws RemoteException
    {
        RemoteIssue[] remoteIssues = new RemoteIssue[issues.size()];
        int i = 0;
        for (final Object issueObject : issues)
        {
            Issue issue = (Issue) issueObject;
            IssueRetriever.IssueInfo issueInfo = issueRetriever.retrieveIssue(issue, user);
            remoteIssues[i] = new RemoteIssue(issueInfo.getIssue(), issueInfo.getParentIssue(), customFieldManager, attachmentManager, soapUtilsBean);
            i++;
        }
        return remoteIssues;
    }

    public RemoteIssue[] getIssuesFromTextSearch(User user, String searchTerms) throws RemoteException
    {
        // Run the search with an "unlimited filter"
        final PagerFilter unlimitedFilter = PagerFilter.getUnlimitedFilter();
        return getIssuesFromTextSearch(user, searchTerms, unlimitedFilter.getStart(), unlimitedFilter.getMax());
    }

    public RemoteIssue[] getIssuesFromTextSearch(User user, String searchTerms, int offSet, int maxNumResults) throws RemoteException
    {
        JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder();
        addFreeTextCondition(jqlQueryBuilder.where(), searchTerms);
        SearchRequest searchRequest = makeRequest(user, jqlQueryBuilder);

        try
        {
            final PagerFilter pagerFilter = new PagerFilter(maxNumResults);
            pagerFilter.setStart(offSet);
            return validateAndSearch(user, searchRequest, pagerFilter);
        }
        catch (SearchException e)
        {
            throw new RemoteException("Error occurred during searching: ", e);
        }
    }

    private RemoteIssue[] validateAndSearch(final User user, final SearchRequest searchRequest, final PagerFilter pagerFilter)
            throws RemoteException, SearchException
    {
        // Check the global search result limits and throw an exception if it is not cool.
        validateNumResultsRespectGlobalConstraints(user, searchRequest, pagerFilter);

        // Validate the SearchRequest
        final MessageSet messageSet = theRealSearchService.validateQuery(user, searchRequest.getQuery());
        if (messageSet.hasAnyErrors())
        {
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addErrorMessages(messageSet.getErrorMessages());
            throw new RemoteValidationException("Query validation failed:", errorCollection);
        }

        List issues = theRealSearchService.search(user, searchRequest.getQuery(), pagerFilter).getIssues();
        return convertIssueObjectsToRemoteIssues(issues, user);
    }

    public RemoteIssue[] getIssuesFromTextSearchWithProject(User user, String[] projectKeys, String searchTerms, int maxNumResults)
            throws RemoteException
    {
        JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder();
        addFreeTextCondition(jqlQueryBuilder.where(), searchTerms);

        if (projectKeys != null && projectKeys.length > 0)
        {
            final List<Long> projectIds = convertKeysToIds(projectKeys);
            jqlQueryBuilder.where().and().project().inNumbers(projectIds);
        }

        SearchRequest searchRequest = makeRequest(user, jqlQueryBuilder);

        return issueSearchRequest(user, searchRequest, maxNumResults);
    }

    private RemoteIssue[] issueSearchRequest(final User user, final SearchRequest searchRequest, final int maxNumResults)
            throws RemoteException
    {
        try
        {
            PagerFilter pager = new PagerFilter();
            pager.setMax(maxNumResults);

            return validateAndSearch(user, searchRequest, pager);
        }
        catch (SearchException e)
        {
            throw new RemoteException("Error occurred during searching: ", e);
        }
    }

    public RemoteIssue[] getIssuesFromJqlSearch(final User user, final String jqlSearch, final int maxNumResults) throws RemoteException
    {
        final com.atlassian.jira.bc.issue.search.SearchService.ParseResult parseResult = theRealSearchService.parseQuery(user, jqlSearch);
        if (!parseResult.isValid())
        {
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addErrorMessages(parseResult.getErrors().getErrorMessages());

            throw new RemoteValidationException("Parsing failed:", errorCollection);
        }

        SearchRequest searchRequest = new SearchRequest(parseResult.getQuery());
        searchRequest.setOwnerUserName((user == null) ? null : user.getName());

        return issueSearchRequest(user, searchRequest, maxNumResults);
    }

    private static JqlClauseBuilder addFreeTextCondition(JqlClauseBuilder builder, String searchTerms)
    {
        builder.sub();
        builder.addStringCondition("text", Operator.LIKE, searchTerms);
        builder.endsub();
        return builder;
    }

    private static SearchRequest makeRequest(User user, JqlQueryBuilder builder)
    {
        SearchRequest searchRequest = new SearchRequest(builder.buildQuery());
        searchRequest.setOwnerUserName((user == null) ? null : user.getName());

        return searchRequest;
    }

    private List<Long> convertKeysToIds(String[] projectKeys)
    {
        if (projectKeys == null || projectKeys.length == 0)
        {
            return Collections.emptyList();
        }

        List<Long> list = new ArrayList<Long>();
        for (String projectKey : projectKeys)
        {
            list.add(projectManager.getProjectObjByKey(projectKey).getId());
        }
        return list;
    }

    private void validateNumResultsRespectGlobalConstraints(final User user, final SearchRequest loadedSR, final PagerFilter pagerFilter)
            throws RemoteException
    {
        // Lets do a check for the global max limit to see if we are trying to return more results than we should
        final Authorizer.Result authorizationResult = requestAuthorizer.isSearchRequestAuthorized(user, loadedSR, new SearchRequestParamsImpl(new HashMap(), pagerFilter));
        if (!authorizationResult.isOK())
        {
            throw new RemoteException("Error occurred during searching: '" + authorizationResult.getReason() + "'.");
        }
    }

}
