package com.atlassian.jira.rpc.xmlrpc;

import com.atlassian.jira.rpc.soap.beans.RemoteComment;
import com.atlassian.jira.rpc.soap.beans.RemoteComponent;
import com.atlassian.jira.rpc.soap.beans.RemoteCustomFieldValue;
import com.atlassian.jira.rpc.soap.beans.RemoteFilter;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.atlassian.jira.rpc.soap.beans.RemoteIssueType;
import com.atlassian.jira.rpc.soap.beans.RemotePriority;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import com.atlassian.jira.rpc.soap.beans.RemoteResolution;
import com.atlassian.jira.rpc.soap.beans.RemoteServerInfo;
import com.atlassian.jira.rpc.soap.beans.RemoteStatus;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;
import com.atlassian.jira.rpc.soap.beans.RemoteVersion;

import java.util.Hashtable;
import java.util.Vector;

/**
 * The XmlRpcService provides an XML-RPC interface into JIRA. All available methods are documented here.
 * For the latest on the plugin, visit http://confluence.atlassian.com/display/JIRAEXT/JIRA+RPC+plugin
 */
public interface XmlRpcService
{
    /**
     * Logs the user into JIRA. The security token which is returned is used in all subsequent method calls.
     *
     * @param username username of the person logged in in as
     * @param password the appropriate password
     * @return A string which is a security token to be used in all subsequent calls
     * @throws Exception
     */
    String login(String username, String password) throws Exception;

    /**
     * Logs the user out of JIRA
     *
     * @param token
     * @return whether the logging out was successful or not
     */
    boolean logout(String token);


    /**
     * Returns the Server information such as baseUrl, version, edition, buildDate, buildNumber.
     *
     * @param token
     * @return Hashtable with fields from {@link RemoteServerInfo}.
     */
    Hashtable getServerInfo(String token);

    /**
     * Returns a list of projects available to the user
     *
     * @param token
     * @return Vector of Hashtables with fields from {@link RemoteProject}
     * @throws Exception
     */
    Vector getProjectsNoSchemes(String token) throws Exception;

    /**
     * Returns all versions available in the specified project
     *
     * @param token
     * @param projectKey The key of project
     * @return Vector of Hashtables with fields from {@link RemoteVersion}
     * @throws Exception
     */
    Vector getVersions(String token, String projectKey) throws Exception;

    /**
     * Returns all components available in the specified project
     *
     * @param token
     * @param projectKey The key of the project
     * @return Vector of Hashtables with fields from {@link RemoteComment}
     * @throws Exception
     */
    Vector getComponents(String token, String projectKey) throws Exception;

    /**
     * Returns all visible (non-sub task) issue types for the specified project id
     *
     * @param token
     * @param projectId
     * @return Vector of Hashtables with fields from {@link RemoteIssueType}
     * @throws Exception
     */
    Vector getIssueTypesForProject(String token, String projectId) throws Exception;

    /**
     * Returns all visible sub task issue types for the specified project id.
     *
     * @param token
     * @param projectId
     * @return Vector of Hashtables with fields from {@link RemoteIssueType}
     * @throws Exception
     */
    Vector getSubTaskIssueTypesForProject(String token, String projectId) throws Exception;

    /**
     * Returns all visible issue types in the system
     *
     * @param token
     * @return Vector of Hashtables with fields from {@link RemoteIssueType}
     * @throws Exception
     */
    Vector getIssueTypes(String token) throws Exception;

    /**
     * Returns all visible subtask issue types in the system
     *
     * @param token
     * @return Vector of Hashtables with fields from {@link RemoteIssueType}
     * @throws Exception
     */
    Vector getSubTaskIssueTypes(String token) throws Exception;

    /**
     * Returns all priorities in the system
     *
     * @param token
     * @return Vector of Hashtables with fields from {@link RemotePriority}
     * @throws Exception
     */
    Vector getPriorities(String token) throws Exception;

    /**
     * Returns all statuses in the system
     *
     * @param token
     * @return Vector of Hashtables with fields from {@link RemoteStatus}
     * @throws Exception
     */
    Vector getStatuses(String token) throws Exception;

    /**
     * Returns all resolutions in the system
     *
     * @param token
     * @return Vector of Hashtables with fields from {@link RemoteResolution}
     * @throws Exception
     */
    Vector getResolutions(String token) throws Exception;

    /**
     * Returns a user's information given a username
     *
     * @param token
     * @param username the username of the user being retrieved
     * @return Vector of Hashtables with fields from {@link RemoteUser}
     * @throws Exception
     */
    Hashtable getUser(String token, String username) throws Exception;

    /**
     * Gets all favourite filters available for the currently logged in user
     *
     * @param token
     * @return Vector of Hashtables with fields from {@link RemoteFilter}
     * @throws Exception
     * @deprecated since v3.13.  Please use {@link #getFavouriteFilters(String)}
     */
    Vector getSavedFilters(String token) throws Exception;

    /**
     * Gets all favourite filters available for the currently logged in user
     *
     * @param token
     * @return Vector of Hashtables with fields from {@link RemoteFilter}
     * @throws Exception
     */
    Vector getFavouriteFilters(String token) throws Exception;

    /**
     * Gets an issue from a given issue key.
     * The Hashtable returned have nested Hashtables for keys, components, affectsVersions, fixVersions and customFieldValues.
     *
     * @param token
     * @param issueKey the key of an issue (e.g. JRA-111)
     * @return Hashtable with fields from {@link RemoteIssue}.
     *         This has nested Hashtables for components {@link RemoteComponent}, affectsVersions, fixVersions {@link RemoteVersion} and customFieldValues {@link RemoteCustomFieldValue}.
     * @throws Exception
     */
    Hashtable getIssue(String token, String issueKey) throws Exception;

    /**
     * Creates an issue in JIRA from a Hashtable object. The Hashtable must be in same structure that's returned by
     * {@link #createIssue(String, Hashtable)}. That is components, affectsVersions, fixVersions and customFieldValues must
     * be blank or be Hashtables with the appropriate data structures. Issues must not have the fields id, key or reporter set,
     * in addition to any standard rules of issue creation (e.g. permissions, not null summary)
     *
     * @param token
     * @param rIssueStruct Hashtable of issue fields with the appropriate structure for {@link RemoteIssue}.
     * @return Hashtable with fields for the generated issue.
     * @throws Exception
     */
    Hashtable createIssue(String token, Hashtable rIssueStruct) throws Exception;

    /**
     * Updates an issue in JIRA from a Hashtable object. The Hashtable must be in same structure that's returned by
     * {@link #createIssue(String, Hashtable)}. That is components, affectsVersions, fixVersions and customFieldValues must
     * be blank or be Hashtables with the appropriate data structures. Issues must not have the fields id, key or reporter set,
     * in addition to any standard rules of issue creation (e.g. permissions, not null summary)
     *
     * @param token
     * @param fieldValues Hashtable of issue fields with the appropriate structure for {@link RemoteIssue}.
     * @return Hashtable with fields for the generated issue.
     * @throws Exception
     */
    Hashtable updateIssue(String token, String issueKey, Hashtable fieldValues) throws Exception;

    /**
     * Adds a comment to an issue
     *
     * @param token
     * @param issueKey he key of an issue (e.g. JRA-111)
     * @param comment  the text string for the comment
     * @throws Exception
     */
    boolean addComment(String token, String issueKey, String comment) throws Exception;

    /**
     * Executes a saved filter
     *
     * @param token
     * @param filterId id of the saved filter
     * @return Vector of Hashtables representing the issue
     */
    Vector getIssuesFromFilter(String token, String filterId) throws Exception;

    /**
     * Find issues using a free text search
     *
     * @param token
     * @param searchTerms The terms to search for
     * @return Vector of Hashtables representing the issue
     */
    Vector getIssuesFromTextSearch(String token, String searchTerms) throws Exception;

    /**
     * Find issues using a free text search, limited to certain projects
     *
     * @param token
     * @param projectKeys
     * @param searchTerms
     * @return
     * @throws Exception
     */
    Vector getIssuesFromTextSearchWithProject(String token, Vector projectKeys, String searchTerms, int maxNumResults) throws Exception;

    /**
     * Returns all comments associated with the issue
     *
     * @param token
     * @param issueKey the key of an issue (e.g. JRA-111)
     * @return Vector of Hashtables with fields from {@link RemoteComment}
     * @throws Exception
     */
    Vector getComments(String token, String issueKey) throws Exception;

}
