/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 9:52:51 AM
 */
package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.soap.beans.RemoteAvatar;
import com.atlassian.jira.rpc.soap.beans.RemoteComponent;
import com.atlassian.jira.rpc.soap.beans.RemotePermissionScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import com.atlassian.jira.rpc.soap.beans.RemoteScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteSecurityLevel;
import com.atlassian.jira.rpc.soap.beans.RemoteVersion;

public interface ProjectService
{
    RemoteProject[] getProjects(User user, boolean addSchemes) throws RemoteException;

    /**
     * Returns the Project with the matching key (if the user has permission to browse it).
     *
     * @param user       the authenticated User attempting to access the project. Permission checks are run against them.
     * @param projectKey the key of the requested after project
     * @return the Project object specified by the key, if it exists and the user has the BROWSE permission for it
     * @throws RemotePermissionException if the User does not have permission to BROWSE the project.
     */
    RemoteProject getProjectByKey(User user, String projectKey) throws RemotePermissionException, RemoteException;

    /**
     * Returns the Project with the matching id (if the user has permission to browse it).
     *
     * @param user      the authenticated User attempting to access the project. Permission checks are run against them.
     * @param projectId the id of the requested after project
     * @return the Project object specified by the id, if it exists and the user has the BROWSE permission for it
     * @throws RemotePermissionException if the User does not have permission to BROWSE the project.
     */
    RemoteProject getProjectById(User user, Long projectId) throws RemoteException;

    /**
     * Returns the project with the matching id (if the user has permission to browse it) with notification, issue
     * security and permission schemes attached.
     *
     * @param user      the authenticated User attempting to access the project. Permission checks are run against them.
     * @param projectId the id of the requested after project
     * @return the Project object specified by the id, if it exists and the user has the BROWSE permission for it
     * @throws RemotePermissionException if the User does not have permission to BROWSE the project.
     */
    RemoteProject getProjectWithSchemesById(User user, Long projectId) throws RemoteException;

    RemoteComponent[] getComponents(User user, String projectKey) throws RemoteException;

    RemoteVersion[] getVersions(User user, String projectKey) throws RemoteException;

    RemoteProject createProject(User user, String key, String name, String description, String url, String lead, RemotePermissionScheme permissionScheme, RemoteScheme notificationScheme, RemoteScheme issueSecurityScheme)
            throws RemoteException;

    RemoteProject createProject(User user, RemoteProject rProject) throws RemoteException;

    RemoteProject updateProject(User user, RemoteProject rProject) throws RemoteException;

    void deleteProject(User user, String projectKey) throws RemoteException;

    RemoteVersion createVersion(User user, String projectKey, RemoteVersion remoteVersion) throws RemoteException;

    void releaseVersion(User user, String projectKey, RemoteVersion remoteVersion) throws RemoteException;

    void archiveVersion(User user, String projectKey, String versionName, boolean archive) throws RemoteException;

    /**
     * Return an array of Security Levels for a given project
     *
     * @param user       the authenticated User attempting to access the project. Permission checks are run against them.
     * @param projectKey the key of the requested project
     * @return array of RemoteSecurityLevel objects for the project. The Array will be empty if the user does
     *         not have the Set Security Level permission for the project.
     * @throws RemoteException If the project key is invalid or the Security Levels could not be retrieved
     * @throws RemotePermissionException If the User does not have Browse permission for the project
     * @since v3.13
     */
    RemoteSecurityLevel[] getSecurityLevels(User user, String projectKey)
            throws RemoteException, RemotePermissionException;

    /**
     * Retrieves avatars for the given project. If the includeSystemAvatars parameter is true, this will include
     * both system (built-in) avatars as well as custom (user-supplied) avatars for that project, otherwise it will
     * include only the custom avatars.
     *
     * @param user                 the user whos asking, must have BROWSE permission to the project.
     * @param projectKey           the key for the project.
     * @param includeSystemAvatars if false, only custom avatars will be included in the returned array.
     * @param size                 "large" or "small".
     * @return the avatars for the project, possibly empty.
     * @throws RemoteException If the project key is invalid.
     * @throws RemotePermissionException If you do not have Browse permission for the project.
     * @since 4.0
     */
    public RemoteAvatar[] getProjectAvatars(User user, String projectKey, boolean includeSystemAvatars, String size) throws RemoteException, RemotePermissionException;

    /**
     * Creates a new custom avatar for the given project and sets it to be current for the project. The image data must
     * be provided as base64 encoded data and should be 48 pixels square. If the image is larger, the top left 48 pixels
     * are taken, if it is smaller it is upscaled to 48 pixels. The small version of the avatar image (16 pixels) is
     * generated automatically.
     *
     * @param user            the user setting the avatar, must have PROJECT ADMIN permission to the project.
     * @param projectKey      the key for the project.
     * @param contentType     the MIME type of the image provided, e.g. image/gif, image/jpeg, image/png.
     * @param base64ImageData a base 64 encoded image, 48 pixels square.
     * @throws RemoteException If the project key is invalid.
     * @throws RemotePermissionException If you do not have project admin permission for the project.
     */
    void setProjectAvatar(User user, String projectKey, String contentType, String base64ImageData) throws RemoteException;

    /**
     * Sets the current avatar for the given project to that with the given id.
     *
     * @param user       the user setting the avatar, must have PROJECT ADMIN permission to the project.
     * @param projectKey the key for the project.
     * @param avatarId   the id of an existing avatar to use for the project or null for the default avatar.
     * @throws RemoteException If the project key is invalid.
     * @throws RemotePermissionException If you do not have project admin permission for the project.
     */
    void setProjectAvatar(final User user, final String projectKey, final Long avatarId) throws RemoteException;

    /**
     * Retrieves the current Avatar for the given project.
     *
     * @param user       the user whos asking, must have BROWSE permission to the project.
     * @param projectKey the key for the project.
     * @param size       "large" or "small".
     * @return the current avatar for the project.
     * @throws RemoteException If the project key is invalid.
     * @throws RemotePermissionException If you do not have Browse permission for the project.
     * @since 4.0
     */
    public RemoteAvatar getProjectAvatar(User user, String projectKey, String size) throws RemoteException, RemotePermissionException;

    /**
     * Deletes the given custom Avatar from the system. System avatars cannot be deleted.
     *
     * @param user     the user whos asking, must have PROJECT ADMIN permission to the project.
     * @param avatarId id of the custom avatar to delete.
     * @throws RemoteException if there is no avatar with the given id, or if it is a system avatar.
     * @throws RemotePermissionException If you do not have administer permission for the project.
     * @since 4.0
     */
    void deleteProjectAvatar(User user, long avatarId) throws RemoteException;
}