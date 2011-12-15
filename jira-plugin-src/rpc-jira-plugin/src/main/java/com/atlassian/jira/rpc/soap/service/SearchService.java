package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;

public interface SearchService
{
    RemoteIssue[] getIssues(User user, String filterId) throws RemoteException;

    RemoteIssue[] getIssues(User user, String filterId, int offSet, int maxNumResults) throws RemoteException;

    long getIssueCountForFilter(User user, String filterId) throws RemoteException;

    RemoteIssue[] getIssuesFromTextSearch(User user, String searchTerms) throws RemoteException;

    RemoteIssue[] getIssuesFromTextSearch(User user, String searchTerms, int offSet, int maxNumResults) throws RemoteException;

    RemoteIssue[] getIssuesFromTextSearchWithProject(User user, String[] projectKeys, String searchTerms, int maxNumResults) throws RemoteException;

    RemoteIssue[] getIssuesFromJqlSearch(User user, String jqlSearch, int maxNumResults) throws RemoteException;
}
