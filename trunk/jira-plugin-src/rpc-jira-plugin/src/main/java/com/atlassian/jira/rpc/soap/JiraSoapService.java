package com.atlassian.jira.rpc.soap;

import com.atlassian.jira.rpc.exception.RemoteAuthenticationException;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.soap.beans.RemoteAttachment;
import com.atlassian.jira.rpc.soap.beans.RemoteAvatar;
import com.atlassian.jira.rpc.soap.beans.RemoteComment;
import com.atlassian.jira.rpc.soap.beans.RemoteComponent;
import com.atlassian.jira.rpc.soap.beans.RemoteConfiguration;
import com.atlassian.jira.rpc.soap.beans.RemoteEntity;
import com.atlassian.jira.rpc.soap.beans.RemoteField;
import com.atlassian.jira.rpc.soap.beans.RemoteFieldValue;
import com.atlassian.jira.rpc.soap.beans.RemoteFilter;
import com.atlassian.jira.rpc.soap.beans.RemoteGroup;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.atlassian.jira.rpc.soap.beans.RemoteIssueType;
import com.atlassian.jira.rpc.soap.beans.RemoteNamedObject;
import com.atlassian.jira.rpc.soap.beans.RemotePermission;
import com.atlassian.jira.rpc.soap.beans.RemotePermissionScheme;
import com.atlassian.jira.rpc.soap.beans.RemotePriority;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import com.atlassian.jira.rpc.soap.beans.RemoteProjectRole;
import com.atlassian.jira.rpc.soap.beans.RemoteProjectRoleActors;
import com.atlassian.jira.rpc.soap.beans.RemoteResolution;
import com.atlassian.jira.rpc.soap.beans.RemoteRoleActors;
import com.atlassian.jira.rpc.soap.beans.RemoteScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteSecurityLevel;
import com.atlassian.jira.rpc.soap.beans.RemoteServerInfo;
import com.atlassian.jira.rpc.soap.beans.RemoteStatus;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;
import com.atlassian.jira.rpc.soap.beans.RemoteVersion;
import com.atlassian.jira.rpc.soap.beans.RemoteWorklog;

import java.util.Date;

/**
 * This interface represents the methods that can be invoked remotely via SOAP.
 * <p/>
 * In general they take a security token that is given out via the {JiraSoapService#login}
 * method to ensure that the user making a method call has the right permissions to make that call.
 * <p/>
 * However there is an exception to this rule.  If your HTTP request has been authenticated via another mechanism,
 * such as trusted application links, OAUTH or Basic Auth, then the token parameter can be ignored since the
 * user has already been validated and set in place.
 */
public interface JiraSoapService
{
    String getAttachmentIdFromIssueKeyAndName(String token, String issueKey, String attachmentName) throws RemoteException;
	
    /**
     * This will authenticate the user in JIRA and returned a authentication token that can then be used on all other
     * SOAP methods.
     *
     * @param username the JIRA user name to authenticate
     * @param password the password of the JIRA user
     * @return a authentication token that can then be used on further SOAP calls.
     * @throws RemoteException               If there was some problem preventing the operation from working.
     * @throws RemoteAuthenticationException If the username and password is an invalid combination
     */
    String login(String username, String password) throws RemoteException, RemoteAuthenticationException;

    /**
     * Cleans up an authentication token that was previously created with a call to {@link JiraSoapService#login(String, String)}
     *
     * @param token the SOAP authentication token
     * @return true if the logout succeeded
     */
    boolean logout(String token);

    /**
     * Returns information about the server JIRA is running on including build number and base URL.
     *
     * @param token the SOAP authentication token.
     * @return a {@link RemoteServerInfo} object
     */
    RemoteServerInfo getServerInfo(String token);

    /**
     * Returns the Project with the matching id (if the user has permission to browse it).
     *
     * @param token     the SOAP authentication token.
     * @param projectId the id of the requested project
     * @return the RemoteProject object specified by the key, if it exists and the user has the BROWSE permission for
     *         it
     * @throws RemotePermissionException     if the User does not have permission to BROWSE the project.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteProject getProjectById(String token, Long projectId)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Returns the Project with the matching id (if the user has permission to browse it) with notification, issue
     * security and permission schemes attached.
     *
     * @param token     the SOAP authentication token.
     * @param projectId the id of the requested project
     * @return the RemoteProject object specified by the key, if it exists and the user has the BROWSE permission for
     *         it
     * @throws RemotePermissionException     if the User does not have permission to BROWSE the project.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteException               if something dramatic happens during this operation
     */
    RemoteProject getProjectWithSchemesById(String token, Long projectId) throws RemoteException;

    /**
     * Returns the Project with the matching key (if the user has permission to browse it).
     *
     * @param token      the SOAP authentication token.
     * @param projectKey the key of the requested project
     * @return the RemoteProject object specified by the key, if it exists and the user has the BROWSE permission for
     *         it
     * @throws RemotePermissionException     if the User does not have permission to BROWSE the project.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteException               if something dramatic happens during this operation
     */
    RemoteProject getProjectByKey(String token, String projectKey)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Returns an array of all the versions for the specified project key.
     *
     * @param token      the SOAP authentication token.
     * @param projectKey the key of the requested project
     * @return an array of {@link RemoteVersion} objects
     * @throws RemoteException               If there was some problem preventing the operation from working.
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteVersion[] getVersions(String token, String projectKey)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Returns an array of all the components for the specified project key.
     *
     * @param token      the SOAP authentication token.
     * @param projectKey the key of the requested project
     * @return an array of {@link RemoteComponent} objects
     * @throws RemoteException               If there was some problem preventing the operation from working.
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteComponent[] getComponents(String token, String projectKey)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Returns an array of all the (non-sub task) issue types for the specified project id.
     *
     * @param token     the SOAP authentication token.
     * @param projectId id of the project
     * @return an array of {@link RemoteIssueType} objects
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteIssueType[] getIssueTypesForProject(String token, String projectId)
            throws RemotePermissionException, RemoteAuthenticationException;

    /**
     * Returns an array of all the sub task issue types for the specified project id.
     *
     * @param token     the SOAP authentication token.
     * @param projectId id of the project
     * @return an array of {@link RemoteIssueType} objects
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteIssueType[] getSubTaskIssueTypesForProject(String token, String projectId)
            throws RemotePermissionException, RemoteAuthenticationException;

    /**
     * Returns an array of all the issue types for all projects in JIRA.
     *
     * @param token the SOAP authentication token.
     * @return an array of {@link RemoteIssueType} objects
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteIssueType[] getIssueTypes(String token) throws RemotePermissionException, RemoteAuthenticationException;

    /**
     * Returns an array of all the sub task issue types in JIRA.
     *
     * @param token the SOAP authentication token.
     * @return an array of {@link RemoteIssueType} objects
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteIssueType[] getSubTaskIssueTypes(String token)
            throws RemotePermissionException, RemoteAuthenticationException;

    /**
     * Returns an array of all the issue priorities in JIRA.
     *
     * @param token the SOAP authentication token.
     * @return an array of {@link RemotePriority} objects
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemotePriority[] getPriorities(String token) throws RemotePermissionException, RemoteAuthenticationException;

    /**
     * Returns an array of all the issue statuses in JIRA.
     *
     * @param token the SOAP authentication token.
     * @return an array of {@link RemoteStatus} objects
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteStatus[] getStatuses(String token) throws RemotePermissionException, RemoteAuthenticationException;

    /**
     * Returns an array of all the issue resolutions in JIRA.
     *
     * @param token the SOAP authentication token.
     * @return an array of {@link RemoteResolution} objects
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteResolution[] getResolutions(String token) throws RemotePermissionException, RemoteAuthenticationException;

    /**
     * Returns information about a user defined to JIRA.
     *
     * @param token    the SOAP authentication token.
     * @param username the user name to look up
     * @return a {@link RemoteUser} or null if it cant be found
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteUser getUser(String token, String username) throws RemotePermissionException, RemoteAuthenticationException;

    /**
     * Creates a user in JIRA with the specified user details.
     *
     * @param token    the SOAP authentication token.
     * @param username the user name to create
     * @param password the password for the new user
     * @param fullName the full name of the new user
     * @param email    the email of the new user
     * @return the newly created {@link RemoteUser}
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the arguments and their properties are incomplete or malformed.
     */
    RemoteUser createUser(String token, String username, String password, String fullName, String email)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteValidationException, RemoteException;

    /**
     * Deletes a user in JIRA with the specified username.
     *
     * @param token    the SOAP authentication token.
     * @param username the user name to delete
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the arguments and their properties are incomplete or malformed.
     */
    void deleteUser(String token, String username)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * Updates the details about a user
     *
     * @param token the SOAP authentication token.
     * @param user  the ruser details to update
     * @return the new represensentation of the user
     * @throws RemotePermissionException     If the ruser is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the arguments and their properties are incomplete or malformed.
     */
    RemoteUser updateUser(String token, RemoteUser user)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteValidationException, RemoteException;


    /**
     * Sets the password for a specified user
     *
     * @param token       the SOAP authentication token.
     * @param user        the ruser to update
     * @param newPassword the new password for that user
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the arguments and their properties are incomplete or malformed.
     */
    void setUserPassword(String token, RemoteUser user, String newPassword)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteValidationException, RemoteException;

    /**
     * Find the group with the specified name in JIRA.
     *
     * @param token     the SOAP authentication token.
     * @param groupName the name of the group to find.
     * @return a {@link RemoteGroup} object for the found group or null if it cant be found.
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the arguments and their properties are incomplete or malformed.
     */
    RemoteGroup getGroup(String token, String groupName)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * Creates a group with the given name optionally adding the given user to it.
     *
     * @param token     the SOAP authentication token.
     * @param groupName the name of the group to create.
     * @param firstUser the user to add to the group (if null, no user will be added).
     * @return the group.
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If there is a problem with the group name, such as if the group name
     *                                       exists.
     */
    RemoteGroup createGroup(String token, String groupName, RemoteUser firstUser)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * Adds the specified user to the specified group
     *
     * @param token the SOAP authentication token.
     * @param group the name of the group to add the user to.
     * @param ruser the name of the user to add
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If there is a problem with the operation.
     */
    void addUserToGroup(String token, RemoteGroup group, RemoteUser ruser)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * Removes the specified user to the specified group
     *
     * @param token the SOAP authentication token.
     * @param group the name of the group to remove the user from.
     * @param ruser the name of the user to remove
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If there is a problem with the operation.
     */
    void removeUserFromGroup(String token, RemoteGroup group, RemoteUser ruser)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * Updates the details of a group
     *
     * @param token the SOAP authentication token.
     * @param group the to be updated group
     * @return the new roup information
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If there is a problem with the operation.
     */
    RemoteGroup updateGroup(String token, RemoteGroup group)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * Deletes the specified group by name
     *
     * @param token         the SOAP authentication token.
     * @param groupName     the name of the group to delete.
     * @param swapGroupName identifies the group to change comment and worklog visibility to.
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If there is a problem with the operation.
     */
    void deleteGroup(String token, String groupName, String swapGroupName)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * This retrieves a list of the currently logged in user's favourite fitlers.
     *
     * @param token the SOAP authentication token.
     * @return a list of the currently logged in user's favourite fitlers.
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If there is a problem with the group name, such as if the group name
     *                                       exists.
     * @deprecated since v3.13.  Please use {@link #getFavouriteFilters(String)}
     */
    RemoteFilter[] getSavedFilters(String token)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * This retreives a list of the currently logged in user's favourite fitlers.
     *
     * @param token the SOAP authentication token.
     * @return a list of the currently logged in user's favourite fitlers.
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If there is a problem with the group name, such as if the group name
     *                                       exists.
     */
    RemoteFilter[] getFavouriteFilters(String token)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Returns information about the specific issue as identified by the issue key
     *
     * @param token    the SOAP authentication token.
     * @param issueKey the key of the issue to return
     * @return a representation of the issue
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteException               if the issue does not exist or your dont have permission to see it
     */
    RemoteIssue getIssue(String token, String issueKey)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Returns information about the specific issue as identified by the issue id
     *
     * @param token   the SOAP authentication token.
     * @param issueId the id of the issue to return
     * @return a representation of the issue
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteException               if the issue does not exist or your dont have permission to see it
     */
    RemoteIssue getIssueById(String token, String issueId)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Given an issue key, this method returns the resolution date for this issue.  If the issue hasn't been resolved
     * yet, this method will return null.
     * <p/>
     * If the no issue with the given key exists a RemoteException will be thrown.
     *
     * @param token    the SOAP authentication token
     * @param issueKey the key of the issue
     * @return The resolution date of the issue. May be null
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @since 4.0
     */
    Date getResolutionDateByKey(String token, String issueKey)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Given an issue id, this method returns the resolution date for this issue.  If the issue hasn't been resolved
     * yet, this method will return null.
     * <p/>
     * If the no issue with the given id exists a RemoteException will be thrown.
     *
     * @param token   the SOAP authentication token
     * @param issueId the id of the issue
     * @return The resolution date of the issue. May be null
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @since 4.0
     */
    Date getResolutionDateById(String token, Long issueId)
            throws RemotePermissionException, RemoteAuthenticationException,
            RemoteException;

    /**
     * Gets a specific comment by comment id
     *
     * @param token the SOAP authentication token
     * @param id    the id of the comment to retrieve
     * @return the comment with the specified id
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteException               if the comment does not exist or your dont have permission to see it
     */
    RemoteComment getComment(String token, Long id) throws RemoteException;

    /**
     * Gets the comments for an issue
     *
     * @param token    the SOAP authentication token
     * @param issueKey the key of the issue to get comments for
     * @return the comment with the specified id
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteException               if the issue does not exist or your dont have permission to see it
     */
    RemoteComment[] getComments(String token, String issueKey)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * This will create an issue based on the passed in details.
     *
     * If you are updating Select or Multi-Select custom fields, note that you should be passing in the Option Ids,
     * as opposed to the actual Option values. If you pass in the Option values, we will attempt to do an automatic
     * conversion from Option value to an Option Id if we find that none of the values passed in can be parsed to an
     * Option Id for the field. 
     *
     * In the case where an Option has a value that is also a valid Option Id for the field, and the supplied value
     * matches this Option value, we assume the supplied value is an OptionId.
     *
     * If any automatic conversion fails, the original values are passed through and cause an exception with the valid
     * Option Ids for the field to be thrown. 
     *
     * @param token  the SOAP authentication token
     * @param rIssue the new issue details to create
     * @return the newly created issue
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the data cannot be validated
     * @throws RemoteException               If the issue does not exist or your dont have permission to see it
     */
    RemoteIssue createIssue(String token, RemoteIssue rIssue)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * This will create an issue based on the passed in details and use the security level
     *
     * See also {@link #createIssue(String, com.atlassian.jira.rpc.soap.beans.RemoteIssue)}
     *
     * @param token           the SOAP authentication token
     * @param rIssue          the new issue details to create
     * @param securityLevelId the id of the required security level
     * @return the newly created issue
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the data cannot be validated
     * @throws RemoteException               If the issue does not exist or your dont have permission to see it
     */
    RemoteIssue createIssueWithSecurityLevel(String token, RemoteIssue rIssue, Long securityLevelId)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * This will create an issue based on the passed in details and make it a child (eg subtask) of the specified parent issue.
     *
     * See also {@link #createIssue(String, com.atlassian.jira.rpc.soap.beans.RemoteIssue)}
     *
     * @param token  the SOAP authentication token
     * @param rIssue the new issue details to create
     * @param parentIssueKey the key of the parent issue
     * @return the newly created issue
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the data cannot be validated
     * @throws RemoteException               If the issue does not exist or your dont have permission to see it
     */
    RemoteIssue createIssueWithParent(String token, RemoteIssue rIssue, String parentIssueKey)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * This will create an issue based on the passed in details and use the security level and make it a child (eg subtask) of the specified parent issue.
     *
     * See also {@link #createIssue(String, com.atlassian.jira.rpc.soap.beans.RemoteIssue)}
     *
     * @param token           the SOAP authentication token
     * @param rIssue          the new issue details to create
     * @param parentIssueKey the key of the parent issue
     * @param securityLevelId the id of the required security level
     * @return the newly created issue
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the data cannot be validated
     * @throws RemoteException               If the issue does not exist or your dont have permission to see it
     */
    RemoteIssue createIssueWithParentWithSecurityLevel(String token, RemoteIssue rIssue, String parentIssueKey, Long securityLevelId)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * Add attachments to an issue.
     * <p/>
     * This method accepts the data of the attachments as byte arrays. This is known to cause problems when the
     * attachments are above a certain size. For more information, please see <a href="http://jira.atlassian.com/browse/JRA-11693">JRA-11693</a>.
     *
     * @param token       the SOAP authentication token.
     * @param issueKey    the issue to attach to
     * @param fileNames   an array of filenames; each element names an attachment to be uploaded
     * @param attachments an array of byte arrays; each element contains the data of the attachment to be uploaded
     * @return true if attachments were successfully added; if the operation was not successful, an exception would be
     *         thrown
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the data cannot be validated
     * @deprecated since v4.0 please use {@link #addBase64EncodedAttachmentsToIssue(String, String, String[],
     *             String[])}
     */
    boolean addAttachmentsToIssue(String token, String issueKey, String[] fileNames, byte[][] attachments)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * An alternative mechanism for adding attachments to an issue. This method accepts the data of the attachments as
     * Base64 encoded strings instead of byte arrays. This is to combat the XML message bloat created by Axis when
     * SOAP-ifying byte arrays.
     * <p/>
     * For more information, please see <a href="http://jira.atlassian.com/browse/JRA-11693">JRA-11693</a>.
     *
     * @param token                       the SOAP authentication token.
     * @param issueKey                    the issue to attach to
     * @param fileNames                   an array of filenames; each element names an attachment to be uploaded
     * @param base64EncodedAttachmentData an array of Base 64 encoded Strings; each element contains the data of the
     *                                    attachment to be uploaded
     * @return true if attachments were successfully added; if the operation was not successful, an exception would be
     *         thrown
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the data cannot be validated
     * @see #addAttachmentsToIssue(String, String, String[], byte[][])
     */
    boolean addBase64EncodedAttachmentsToIssue(String token, String issueKey, String[] fileNames, String[] base64EncodedAttachmentData)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * Returns the attachments that are associated with an issue
     *
     * @param token    the SOAP authentication token.
     * @param issueKey the issue to find attachments for
     * @return the attachments of the issue
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the data cannot be validated
     * @throws RemoteException               If the issue does not exist or your dont have permission to see it
     */
    RemoteAttachment[] getAttachmentsFromIssue(String token, String issueKey)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * This will delete an issue with the specified issue key
     *
     * @param token    the SOAP authentication token
     * @param issueKey the issue to delete
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteException               If the issue does not exist or your dont have permission to see it
     */
    void deleteIssue(String token, String issueKey)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Adds a comment to the specified issue
     *
     * @param token         the SOAP authentication token
     * @param issueKey      the key of the issue to add a comment to
     * @param remoteComment the new comment to add
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteException               If the issue does not exist or your dont have permission to see it
     */
    void addComment(String token, String issueKey, RemoteComment remoteComment)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Returns true if the user has permission to edit a comment
     *
     * @param token         the SOAP authentication token
     * @param remoteComment the comment to edit
     * @return true if the user has permission to make this edit
     * @throws RemoteException if something dramatic happens during this operation
     */
    boolean hasPermissionToEditComment(String token, RemoteComment remoteComment) throws RemoteException;

    /**
     * Allows a comment to be edited
     *
     * @param token         the SOAP authentication token
     * @param remoteComment the new comment details
     * @return the updated comment details
     * @throws RemoteException if something dramatic happens during this operation
     */
    RemoteComment editComment(String token, RemoteComment remoteComment) throws RemoteException;

    /**
     * This will update an issue with new values.
     * <p/>
     * NOTE : You cannot update the 'status' field of the issue via this method.  You must instead call :
     * <p/>
     * {@link #progressWorkflowAction(String, String, String, com.atlassian.jira.rpc.soap.beans.RemoteFieldValue[])}
     * <p/>
     * to progress the issue status into a new workflow state.
     *
     * If you are updating Select or Multi-Select custom fields, note that you should be passing in the Option Ids,
     * as opposed to the actual Option values. If you pass in the Option values, we will attempt to do an automatic
     * conversion from Option value to an Option Id if we find that none of the values passed in can be parsed to an
     * Option Id for the field. 
     *
     * In the case where an Option has a value that is also a valid Option Id for the field, and the supplied value
     * matches this Option value, we assume the supplied value is an OptionId.
     *
     * If any automatic conversion fails, the original values are passed through and cause an exception with the valid
     * Option Ids for the field to be thrown. 
     *
     * @param token        the SOAP authentication token.
     * @param issueKey     the issue to update.
     * @param actionParams the list of issue fields to change
     * @return the updated RemoteIssue
     * @throws RemoteException if the issue cannot be updated
     */
    RemoteIssue updateIssue(String token, String issueKey, RemoteFieldValue[] actionParams) throws RemoteException;

    /**
     * This will progress an issue through a workflow.
     *
     * @param token          the SOAP authentication token.
     * @param issueKey       the issue to update.
     * @param actionIdString the workflow action to progress to
     * @param actionParams   the list of issue fields to change in this workflow step
     * @return the updated RemoteIssue
     * @throws RemoteException if the issue cannot be updated
     */
    RemoteIssue progressWorkflowAction(String token, String issueKey, String actionIdString, RemoteFieldValue[] actionParams)
            throws RemoteException;

    /**
     * Returns the fields that are shown during an issue creation.
     *
     * @param token    the SOAP authentication token.
     * @param projectKey the project to create the issue in.
     * @param issueTypeId the type of issue to create
     * @return the fields that would be shown during the create operation
     * @throws RemoteException if something dramatic happens during this operation
     */
    RemoteField[] getFieldsForCreate(String token, String projectKey, Long issueTypeId) throws RemoteException;

    /**
     * Returns the fields that are shown during an issue edit.
     *
     * @param token    the SOAP authentication token.
     * @param issueKey the issue to update.
     * @return the fields that would be shown during the edit operation
     * @throws RemoteException if something dramatic happens during this operation
     */
    RemoteField[] getFieldsForEdit(String token, String issueKey) throws RemoteException;

    /**
     * Returns the available actions that can be applied to an issue.
     *
     * @param token    the SOAP authentication token.
     * @param issueKey the issue to get actions for.
     * @return the available actions that can be applied to an issue
     * @throws RemoteException if something dramatic happens during this operation
     */
    RemoteNamedObject[] getAvailableActions(String token, String issueKey) throws RemoteException;

    /**
     * Returns the fields that are shown during an issue action.
     *
     * @param token          the SOAP authentication token.
     * @param issueKey       the issue to update.
     * @param actionIdString the id of issue action to be executed
     * @return the fields that would be shown during the issue action operation
     * @throws RemoteException if something dramatic happens during this operation
     */
    RemoteField[] getFieldsForAction(String token, String issueKey, String actionIdString) throws RemoteException;

    /**
     * Creates a project based on the passed in details.
     *
     * @param token               the SOAP authentication token
     * @param key                 the new project key
     * @param name                the new project name
     * @param description         the description of the new project
     * @param url                 the new project URL
     * @param lead                the user who is trhe project lead
     * @param permissionScheme    the permission to use for the new project
     * @param notificationScheme  the notification scheme to use on the new project
     * @param issueSecurityScheme the issue security scheme to use on the new project
     * @return the newly created project
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the data cannot be validated
     * @throws RemoteException               If something dramatic happened during the execution of the operation
     */
    RemoteProject createProject(String token, String key, String name, String description, String url, String lead, RemotePermissionScheme permissionScheme, RemoteScheme notificationScheme, RemoteScheme issueSecurityScheme)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * Creates a project based on the passed in details.
     *
     * @param token    the SOAP authentication token
     * @param rproject the object representation of the new project
     * @return the newly created project
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the data cannot be validated
     * @throws RemoteException               If something dramatic happened during the execution of the operation
     */
    RemoteProject createProjectFromObject(String token, RemoteProject rproject)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * Updates a project based on the passed in details.
     *
     * @param token    the SOAP authentication token
     * @param rProject the object representation of the updated project
     * @return the updated project
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteValidationException     If the data cannot be validated
     * @throws RemoteException               If something dramatic happened during the execution of the operation
     */
    RemoteProject updateProject(String token, RemoteProject rProject)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException, RemoteValidationException;

    /**
     * This will delete a project with the specified project key
     *
     * @param token      the SOAP authentication token
     * @param projectKey the project to delete
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     * @throws RemoteException               If something dramatic happened during the execution of the operation
     */
    void deleteProject(String token, String projectKey)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Returns all the custom fields available
     *
     * @param token the SOAP authentication token.
     * @return all the custom fields available
     * @throws RemoteException If something dramatic happened during the execution of the operation
     */
    RemoteField[] getCustomFields(String token) throws RemoteException;

    /**
     * Addsa a new version to the specified project.
     *
     * @param token         the SOAP authentication token.
     * @param projectKey    the key of the project to add the new version to
     * @param remoteVersion the new version details
     * @return the newly added version
     * @throws RemoteException If something dramatic happened during the execution of the operation
     */
    RemoteVersion addVersion(String token, String projectKey, RemoteVersion remoteVersion) throws RemoteException;

    /**
     * Refreshes the internal representation of all the custom fields available
     *
     * @param token the SOAP authentication token.
     * @throws RemoteException If something dramatic happened during the execution of the operation
     */
    void refreshCustomFields(String token) throws RemoteException;

    // Search methods

    /**
     * Returns issues that match the saved filter specified by the filterId.
     * <p/>
     * This method will return no more than the maxNumResults.
     * <p/>
     * It will start the result set at the provided off set.
     * <p/>
     * This method also respects the jira.search.views.max.limit and jira.search.views.max.unlimited.group JIRA
     * properties which will override the max number of results returned.
     * <p/>
     * If the jira.search.views.max.limit property is set and you are not in a group specified by
     * jira.search.views.max.unlimited.group then the number of results returned will be constrained by the value of
     * jira.search.views.max.limit if it is less than the specified maxNumResults.
     *
     * @param token    the SOAP authentication token.
     * @param filterId identifies the saved filter to use for the search.
     * @return issues matching the saved filter
     * @throws RemoteException If there was some problem preventing the operation from working.
     * @deprecated use {@link #getIssuesFromFilterWithLimit(String, String, int, int)} instead
     */
    RemoteIssue[] getIssuesFromFilter(String token, String filterId) throws RemoteException;

    /**
     * Returns issues containing searchTerms.
     * <p/>
     * Note: this is a fuzzy search, returned in order of 'relevance', so the results are only generally useful for
     * human consumption.
     * <p/>
     * This method also respects the jira.search.views.max.limit and jira.search.views.max.unlimited.group JIRA
     * properties which will override the max number of results returned.
     * <p/>
     * If the jira.search.views.max.limit property is set and you are not in a group specified by
     * jira.search.views.max.unlimited.group then the number of results returned will be constrained by the value of
     * jira.search.views.max.limit if it is less than the specified maxNumResults.
     *
     * @param token       the SOAP authentication token.
     * @param searchTerms search terms
     * @return issues matching the search terms
     * @throws RemoteException If there was some problem preventing the operation from working.
     * @see #getIssuesFromJqlSearch(String, String, int)
     * @deprecated use {@link #getIssuesFromTextSearchWithLimit(String, String, int, int)}  instead
     */
    RemoteIssue[] getIssuesFromTextSearch(String token, String searchTerms) throws RemoteException;

    /**
     * Returns issues that match the saved filter specified by the filterId.
     * <p/>
     * This method will return no more than the maxNumResults.
     * <p/>
     * It will start the result set at the provided off set.
     * <p/>
     * This method also respects the jira.search.views.max.limit and jira.search.views.max.unlimited.group JIRA
     * properties which will override the max number of results returned.
     * <p/>
     * If the jira.search.views.max.limit property is set and you are not in a group specified by
     * jira.search.views.max.unlimited.group then the number of results returned will be constrained by the value of
     * jira.search.views.max.limit if it is less than the specified maxNumResults.
     *
     * @param token         the SOAP authentication token.
     * @param filterId      identifies the saved filter to use for the search.
     * @param offSet        the place in the result set to use as the first result returned
     * @param maxNumResults the maximum number of results that this method will return.
     * @return issues matching the saved filter
     * @throws RemoteException If there was some problem preventing the operation from working.
     */
    RemoteIssue[] getIssuesFromFilterWithLimit(String token, String filterId, int offSet, int maxNumResults)
            throws RemoteException;

    /**
     * Returns issues containing searchTerms.
     * <p/>
     * Note: this is a fuzzy search, returned in order of 'relevance', so the results are only generally useful for
     * human consumption.
     * <p/>
     * This method will return no more than the maxNumResults.
     * <p/>
     * It will start the result set at the provided off set.
     * <p/>
     * This method also respects the jira.search.views.max.limit and jira.search.views.max.unlimited.group JIRA
     * properties which will override the max number of results returned.
     * <p/>
     * If the jira.search.views.max.limit property is set and you are not in a group specified by
     * jira.search.views.max.unlimited.group then the number of results returned will be constrained by the value of
     * jira.search.views.max.limit if it is less than the specified maxNumResults.
     *
     * @param token         the SOAP authentication token.
     * @param searchTerms   search terms
     * @param offSet        the place in the result set to use as the first result returned
     * @param maxNumResults the maximum number of results that this method will return.
     * @return issues matching the search terms
     * @throws RemoteException If there was some problem preventing the operation from working.
     * @see #getIssuesFromJqlSearch(String, String, int)
     */
    RemoteIssue[] getIssuesFromTextSearchWithLimit(String token, String searchTerms, int offSet, int maxNumResults)
            throws RemoteException;

    /**
     * Returns issues containing searchTerms that are within the specified projects.
     * <p/>
     * Note: this is a fuzzy search, returned in order of 'relevance', so the results are only generally useful for
     * human consumption.
     * <p/>
     * This method will return no more than the maxNumResults.
     * <p/>
     * This method also respects the jira.search.views.max.limit and jira.search.views.max.unlimited.group JIRA
     * properties which will override the max number of results returned.
     * <p/>
     * If the jira.search.views.max.limit property is set and you are not in a group specified by
     * jira.search.views.max.unlimited.group then the number of results returned will be constrained by the value of
     * jira.search.views.max.limit if it is less than the specified maxNumResults.
     *
     * @param token         the SOAP authentication token.
     * @param projectKeys   an array of project keys to search within
     * @param searchTerms   search terms
     * @param maxNumResults the maximum number of results that this method will return.
     * @return issues matching the search terms
     * @throws RemoteException If there was some problem preventing the operation from working.
     * @see #getIssuesFromJqlSearch(String, String, int)
     */
    RemoteIssue[] getIssuesFromTextSearchWithProject(String token, String[] projectKeys, String searchTerms, int maxNumResults)
            throws RemoteException;

    /**
     * Execute a specified JQL query and return the resulting issues.
     * <p/>
     * This method also respects the jira.search.views.max.limit and jira.search.views.max.unlimited.group JIRA
     * properties which will override the max number of results returned.
     * <p/>
     * If the jira.search.views.max.limit property is set and you are not in a group specified by
     * jira.search.views.max.unlimited.group then the number of results returned will be constrained by the value of
     * jira.search.views.max.limit if it is less than the specified maxNumResults.
     *
     * @param token         the SOAP authentication token
     * @param jqlSearch     JQL query string to execute
     * @param maxNumResults the maximum number of results that this method will return
     * @return issues matching the JQL query
     * @throws RemoteException If there was a JQL parse error or an error occurs during the search
     */
    RemoteIssue[] getIssuesFromJqlSearch(String token, String jqlSearch, int maxNumResults) throws RemoteException;

    /**
     * Returns an array of all the Projects defined in JIRA.
     *
     * @param token the SOAP authentication token.
     * @return an array of {@link com.atlassian.jira.rpc.soap.beans.RemoteProject} objects.
     * @throws RemoteException               If there was some problem preventing the operation from working.
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteProject[] getProjectsNoSchemes(String token)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Returns the count of issues that would be returned for a given filter
     *
     * @param token    the SOAP authentication token.
     * @param filterId the id of the search filter
     * @return the count of matching issues
     * @throws RemoteException if something dramatic happens during this operation
     */
    long getIssueCountForFilter(String token, String filterId) throws RemoteException;


    /**
     * Returns information about the current configuration of JIRA.
     *
     * @param token the SOAP authentication token.
     * @return a {@link com.atlassian.jira.rpc.soap.beans.RemoteConfiguration} object which contains information about
     *         the current configuration of JIRA.
     * @throws RemoteException               If there was some problem preventing the operation from working.
     * @throws RemotePermissionException     If the user is not permitted to perform this operation in this context.
     * @throws RemoteAuthenticationException If the token is invalid or the SOAP session has timed out
     */
    RemoteConfiguration getConfiguration(String token)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    /**
     * Adds a worklog to the given issue and sets the issue's remaining estimate field to the given value. The issue's
     * time spent field will be increased by the amount in the remoteWorklog.getTimeSpent().
     * <p/>
     * The following fields of remoteWorklog are ignored: <ul> <li>id: generated by the system.</li> <li>author: derived
     * from the authenticated user.</li> <li>updatedAuthor: derived from the authenticated user.</li> <li>created:
     * derived from the current date.</li> <li>updated: derived from the current date.</li> </ul>
     *
     * @param token                the SOAP authentication token.
     * @param issueKey             the key of the issue.
     * @param remoteWorklog        the worklog to add.
     * @param newRemainingEstimate the new value for the issue's remaining estimate as a duration string, eg 1d 2h.
     * @return Created worklog with the id set or null if no worklog was created.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the user is not permitted to perform this operation in this context.
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     */
    RemoteWorklog addWorklogWithNewRemainingEstimate(String token, String issueKey, RemoteWorklog remoteWorklog, String newRemainingEstimate)
            throws RemoteException, RemotePermissionException, RemoteValidationException;

    /**
     * Adds a worklog to the given issue. The issue's time spent field will be increased by the amount in the
     * remoteWorklog.getTimeSpent().
     * <p/>
     * The following fields of remoteWorklog are ignored: <ul> <li>id: generated by the system.</li> <li>author: derived
     * from the authenticated user.</li> <li>updatedAuthor: derived from the authenticated user.</li> <li>created:
     * derived from the current date.</li> <li>updated: derived from the current date.</li> </ul>
     *
     * @param token         the SOAP authentication token.
     * @param issueKey      the key of the issue.
     * @param remoteWorklog the worklog to add.
     * @return Created worklog with the id set or null if no worklog was created.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the user is not permitted to perform this operation in this context.
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     */
    RemoteWorklog addWorklogAndAutoAdjustRemainingEstimate(String token, String issueKey, RemoteWorklog remoteWorklog)
            throws RemoteException, RemotePermissionException, RemoteValidationException;

    /**
     * Adds a worklog to the given issue but leaves the issue's remaining estimate field unchanged. The issue's time
     * spent field will be increased by the amount in the remoteWorklog.getTimeSpent().
     * <p/>
     * The following fields of remoteWorklog are ignored: <ul> <li>id: generated by the system.</li> <li>author: derived
     * from the authenticated user.</li> <li>updatedAuthor: derived from the authenticated user.</li> <li>created:
     * derived from the current date.</li> <li>updated: derived from the current date.</li> </ul>
     *
     * @param token         the SOAP authentication token.
     * @param issueKey      the key of the issue.
     * @param remoteWorklog the worklog to add.
     * @return Created worklog with the id set or null if no worklog was created.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the user is not permitted to perform this operation in this context.
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     */
    RemoteWorklog addWorklogAndRetainRemainingEstimate(String token, String issueKey, RemoteWorklog remoteWorklog)
            throws RemoteException, RemotePermissionException, RemoteValidationException;

    /**
     * Returns all worklogs for the given issue.
     *
     * @param token    the SOAP authentication token.
     * @param issueKey the key of the issue.
     * @return all the worklogs of the issue.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the user is not permitted to perform this operation in this context.
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     */
    RemoteWorklog[] getWorklogs(String token, String issueKey)
            throws RemoteException, RemotePermissionException, RemoteValidationException;

    /**
     * Deletes the worklog with the given id and sets the remaining estimate field on the isssue to the given value. The
     * time spent field of the issue is reduced by the time spent amount on the worklog being deleted.
     *
     * @param token                the SOAP authentication token.
     * @param worklogId            the id of the worklog to delete.
     * @param newRemainingEstimate the new value for the issue's remaining estimate as a duration string, eg 1d 2h.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the user is not permitted to perform this operation in this context.
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     */
    void deleteWorklogWithNewRemainingEstimate(String token, String worklogId, String newRemainingEstimate)
            throws RemoteException, RemotePermissionException, RemoteValidationException;

    /**
     * Deletes the worklog with the given id and updates the remaining estimate field on the isssue by increasing it by
     * the time spent amount on the worklog being deleted. The time spent field of the issue is reduced by the time
     * spent amount on the worklog being deleted.
     *
     * @param token     the SOAP authentication token.
     * @param worklogId the id of the worklog to delete.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the user is not permitted to perform this operation in this context.
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     */
    void deleteWorklogAndAutoAdjustRemainingEstimate(String token, String worklogId)
            throws RemoteException, RemotePermissionException, RemoteValidationException;

    /**
     * Deletes the worklog with the given id but leaves the remaining estimate field on the isssue unchanged. The time
     * spent field of the issue is reduced by the time spent amount on the worklog being deleted.
     *
     * @param token     the SOAP authentication token.
     * @param worklogId the id of the worklog to delete.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the user is not permitted to perform this operation in this context.
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     */
    void deleteWorklogAndRetainRemainingEstimate(String token, String worklogId)
            throws RemoteException, RemotePermissionException, RemoteValidationException;

    /**
     * Modifies the worklog with the id of the given worklog, updating its fields to match the given worklog and sets
     * the remaining estimate field on the relevant issue to the given value. The time spent field of the issue is
     * changed by subtracting the previous value of the worklog's time spent amount and adding the new value in the
     * given worklog.
     * <p/>
     * The following fields of remoteWorklog are ignored: <ul> <li>author: unchanged.</li> <li>updatedAuthor: derived
     * from the authenticated user.</li> <li>created: unchanged.</li> <li>updated: derived from the current date.</li>
     * </ul>
     *
     * @param token                the SOAP authentication token.
     * @param remoteWorklog        the worklog to update.
     * @param newRemainingEstimate the new value for the issue's remaining estimate as a duration string, eg 1d 2h.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the user is not permitted to perform this operation in this context.
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     */
    void updateWorklogWithNewRemainingEstimate(String token, RemoteWorklog remoteWorklog, String newRemainingEstimate)
            throws RemoteException, RemotePermissionException, RemoteValidationException;

    /**
     * Modifies the worklog with the id of the given worklog, updating its fields to match the given worklog and changes
     * the remaining estimate field on the relevant issue to the value obtained by adding the previous time spent amount
     * of the worklog and subtracting the new time spent amount. The time spent field of the issue is changed by
     * subtracting the previous value of the worklog's time spent amount and adding the new value in the given worklog.
     * <p/>
     * The following fields of remoteWorklog are ignored: <ul> <li>author: unchanged.</li> <li>updatedAuthor: derived
     * from the authenticated user.</li> <li>created: unchanged.</li> <li>updated: derived from the current date.</li>
     * </ul>
     *
     * @param token         the SOAP authentication token.
     * @param remoteWorklog the worklog to update.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the user is not permitted to perform this operation in this context.
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     */
    void updateWorklogAndAutoAdjustRemainingEstimate(String token, RemoteWorklog remoteWorklog)
            throws RemoteException, RemotePermissionException, RemoteValidationException;

    /**
     * Modifies the worklog with the id of the given worklog, updating its fields to match the given worklog but leaves
     * the remaining estimate field on the relevant issue unchanged. The time spent field of the issue is changed by
     * subtracting the previous value of the worklog's time spent amount and adding the new value in the given worklog.
     * <p/>
     * The following fields of remoteWorklog are ignored: <ul> <li>author: unchanged.</li> <li>updatedAuthor: derived
     * from the authenticated user.</li> <li>created: unchanged.</li> <li>updated: derived from the current date.</li>
     * </ul>
     *
     * @param token         the SOAP authentication token.
     * @param remoteWorklog the worklog to update.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the user is not permitted to perform this operation in this context.
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     */
    void updateWorklogAndRetainRemainingEstimate(String token, RemoteWorklog remoteWorklog)
            throws RemoteException, RemotePermissionException, RemoteValidationException;

    /**
     * Determines if the user has the permission to add worklogs to the specified issue, that timetracking is enabled in
     * JIRA and that the specified issue is in an editable workflow state.
     *
     * @param token    the SOAP authentication token.
     * @param issueKey the key of the issue.
     * @return true if the user has permission to create a worklog on the specified issue, false otherwise
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     */
    boolean hasPermissionToCreateWorklog(String token, String issueKey)
            throws RemoteException, RemoteValidationException;

    /**
     * Determine whether the current user has the permission to delete the supplied worklog, that timetracking is
     * enabled in JIRA and that the associated issue is in an editable workflow state.
     * <p/>
     * This method will return true if the user is a member of the worklog's group/role level (if specified)
     * <b>AND</b><br/> <ul> <li>The user has the WORKLOG_DELETE_ALL permission; <b>OR</b></li> <li>The user is the
     * worklog author and has the WORKLOG_DELETE_OWN permission</li> </ul> and false otherwise.
     *
     * @param token     the SOAP authentication token.
     * @param worklogId the id of the worklog wishes to delete.
     * @return true if the user has permission to delete the supplied worklog, false otherwise
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     */
    boolean hasPermissionToDeleteWorklog(String token, String worklogId)
            throws RemoteException, RemoteValidationException;

    /**
     * Determine whether the current user has the permission to update the supplied worklog, that timetracking is
     * enabled in JIRA and that the associated issue is in an editable workflow state.
     * <p/>
     * This method will return true if the user is a member of the worklog's group/role level (if specified)
     * <b>AND</b><br/> <ul> <li>The user has the WORKLOG_EDIT_ALL permission; <b>OR</b></li> <li>The user is the worklog
     * author and has the WORKLOG_EDIT_OWN permission</li> </ul> and false otherwise.
     *
     * @param token     the SOAP authentication token.
     * @param worklogId the ide of the worklog wishes to update.
     * @return true if the user has permission to update the supplied worklog, false otherwise
     * @throws RemoteValidationException If the arguments and their properties are incomplete or malformed.
     * @throws RemoteException           If there was some problem preventing the operation from working.
     */
    boolean hasPermissionToUpdateWorklog(String token, String worklogId)
            throws RemoteException, RemoteValidationException;

    /**
     * Returns the current security level for given issue
     *
     * @param token    the SOAP authentication token
     * @param issueKey the issue key
     * @return issue security level
     * @throws RemoteException           If there was some problem preventing the operation from working.
     * @throws RemotePermissionException If the issue key is invalid or the user is not permitted to see this issue.
     * @since v3.13
     */
    RemoteSecurityLevel getSecurityLevel(String token, String issueKey)
            throws RemoteException, RemotePermissionException;


    /**
     * Returns an array of all security levels for a given project.
     *
     * @param token      the SOAP authentication token
     * @param projectKey the key for the project
     * @return array of RemoteSecurityLevels for the project
     * @throws RemoteException           If the project key is invalid
     * @throws RemotePermissionException If you do not have Browse permission for the project
     * @since v3.13
     */
    RemoteSecurityLevel[] getSecurityLevels(String token, String projectKey)
            throws RemoteException, RemotePermissionException;

    /**
     * Retrieves avatars for the given project. If the includeSystemAvatars parameter is true, this will include both
     * system (built-in) avatars as well as custom (user-supplied) avatars for that project, otherwise it will include
     * only the custom avatars. Project browse permission is required.
     *
     * @param token                the SOAP authentication token
     * @param projectKey           the key for the project.
     * @param includeSystemAvatars if false, only custom avatars will be included in the returned array.
     * @return the avatars for the project, possibly empty.
     * @throws RemoteException           If the project key is invalid.
     * @throws RemotePermissionException If you do not have Browse permission for the project.
     * @since 4.0
     */
    RemoteAvatar[] getProjectAvatars(String token, String projectKey, boolean includeSystemAvatars)
            throws RemoteException, RemotePermissionException;

    /**
     * Creates a new custom avatar for the given project and sets it to be current for the project. The image data must
     * be provided as base64 encoded data and should be 48 pixels square. If the image is larger, the top left 48 pixels
     * are taken, if it is smaller it is upscaled to 48 pixels. The small version of the avatar image (16 pixels) is
     * generated automatically. Project administration permission is required.
     *
     * @param token           the SOAP authentication token
     * @param projectKey      the key for the project.
     * @param contentType     the MIME type of the image provided, e.g. image/gif, image/jpeg, image/png.
     * @param base64ImageData a base 64 encoded image, 48 pixels square.
     * @throws RemoteException           If the project key is invalid.
     * @throws RemotePermissionException If you do not have project admin permission for the project.
     */
    void setNewProjectAvatar(String token, String projectKey, String contentType, String base64ImageData)
            throws RemoteException, RemotePermissionException;

    /**
     * Sets the current avatar for the given project to that with the given id. Project administration permission is
     * required.
     *
     * @param token      the SOAP authentication token
     * @param projectKey the key for the project.
     * @param avatarId   the id of an existing avatar to use for the project or null for the default avatar.
     * @throws RemoteException           If the project key is invalid.
     * @throws RemotePermissionException If you do not have project admin permission for the project.
     */
    void setProjectAvatar(String token, String projectKey, Long avatarId)
            throws RemoteException, RemotePermissionException;

    /**
     * Retrieves the current avatar for the given project. Project browse permission is required.
     *
     * @param token      the SOAP authentication token
     * @param projectKey the key for the project.
     * @return the current avatar for the project.
     * @throws RemoteException           If the project key is invalid.
     * @throws RemotePermissionException If you do not have Browse permission for the project.
     * @since 4.0
     */
    RemoteAvatar getProjectAvatar(String token, String projectKey) throws RemoteException, RemotePermissionException;

    /**
     * Deletes the given custom Avatar from the system. System avatars cannot be deleted. Project administration
     * permission is required.
     *
     * @param token    the SOAP authentication token
     * @param avatarId id of the custom avatar to delete.
     * @throws RemoteException           if there is no avatar with the given id, or if it is a system avatar.
     * @throws RemotePermissionException If you do not have administer permission for the project.
     * @since 4.0
     */
    void deleteProjectAvatar(String token, long avatarId) throws RemoteException;

    RemoteScheme[] getNotificationSchemes(String token)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    RemotePermissionScheme[] getPermissionSchemes(String token)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    RemoteScheme[] getSecuritySchemes(String token)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException;

    RemotePermission[] getAllPermissions(String token)
            throws RemotePermissionException, RemoteException, RemoteAuthenticationException;

    RemotePermissionScheme createPermissionScheme(String token, String name, String description)
            throws RemotePermissionException, RemoteException, RemoteAuthenticationException, RemoteValidationException;

    RemotePermissionScheme addPermissionTo(String token, RemotePermissionScheme permissionScheme, RemotePermission permission, RemoteEntity remoteEntity)
            throws RemotePermissionException, RemoteException, RemoteValidationException, RemoteAuthenticationException;

    RemotePermissionScheme deletePermissionFrom(String token, RemotePermissionScheme permissionSchemeName, RemotePermission permission, RemoteEntity remoteEntity)
            throws RemotePermissionException, RemoteException, RemoteAuthenticationException, RemoteValidationException;

    void deletePermissionScheme(String token, String permissionSchemeName)
            throws RemotePermissionException, RemoteException, RemoteAuthenticationException, RemoteValidationException;

    RemoteProjectRole[] getProjectRoles(String token) throws RemoteException;

    RemoteProjectRole getProjectRole(String token, Long id) throws RemoteException;

    RemoteProjectRole createProjectRole(String token, RemoteProjectRole projectRole) throws RemoteException;

    boolean isProjectRoleNameUnique(String token, String name) throws RemoteException;

    void deleteProjectRole(String token, RemoteProjectRole projectRole, boolean confirm) throws RemoteException;

    void addActorsToProjectRole(String token, String[] actors, RemoteProjectRole projectRole, RemoteProject project, String actorType)
            throws RemoteException;

    void removeActorsFromProjectRole(String token, String[] actors, RemoteProjectRole projectRole, RemoteProject project, String actorType)
            throws RemoteException;

    void updateProjectRole(String token, RemoteProjectRole projectRole) throws RemoteException;

    RemoteProjectRoleActors getProjectRoleActors(String token, RemoteProjectRole projectRole, RemoteProject project)
            throws RemoteException;

    RemoteRoleActors getDefaultRoleActors(String token, RemoteProjectRole projectRole) throws RemoteException;

    void addDefaultActorsToProjectRole(String token, String[] actors, RemoteProjectRole projectRole, String type)
            throws RemoteException;

    void removeDefaultActorsFromProjectRole(String token, String[] actors, RemoteProjectRole projectRole, String actorType)
            throws RemoteException;

    void removeAllRoleActorsByNameAndType(String token, String name, String type) throws RemoteException;

    void removeAllRoleActorsByProject(String token, RemoteProject project) throws RemoteException;

    RemoteScheme[] getAssociatedNotificationSchemes(String token, RemoteProjectRole projectRole) throws RemoteException;

    RemoteScheme[] getAssociatedPermissionSchemes(String token, RemoteProjectRole projectRole) throws RemoteException;

    void releaseVersion(String token, String projectKey, RemoteVersion version) throws RemoteException;

    void archiveVersion(String token, String projectKey, String versionName, boolean archive) throws RemoteException;

}
