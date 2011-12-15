package com.atlassian.jira.rpc.soap;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.auth.TokenManager;
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
import com.atlassian.jira.rpc.soap.service.AdminService;
import com.atlassian.jira.rpc.soap.service.IssueConstantsService;
import com.atlassian.jira.rpc.soap.service.IssueService;
import com.atlassian.jira.rpc.soap.service.ProjectRoleService;
import com.atlassian.jira.rpc.soap.service.ProjectService;
import com.atlassian.jira.rpc.soap.service.SchemeService;
import com.atlassian.jira.rpc.soap.service.SearchService;
import com.atlassian.jira.rpc.soap.service.UserService;
import com.atlassian.jira.soap.axis.JiraSoapTokenResolver;
import org.apache.axis.encoding.Base64;

import java.util.Date;


public class JiraSoapServiceImpl implements JiraSoapService, JiraSoapTokenResolver
{
    private TokenManager tokenManager;
    private final ProjectService projectService;
    private final IssueService issueService;
    private final UserService userService;
    private final SchemeService schemeService;
    private final AdminService adminService;
    private final SearchService searchService;
    private final ProjectRoleService projectRoleService;
    private final IssueConstantsService issueConstantsService;

    public JiraSoapServiceImpl(TokenManager tokenManager, ProjectService projectService, IssueService issueService, UserService userService, SchemeService schemeService, AdminService adminService, SearchService searchService, ProjectRoleService projectRoleService, IssueConstantsService issueConstantsService)
    {
        this.tokenManager = tokenManager;
        this.projectService = projectService;
        this.issueService = issueService;
        this.schemeService = schemeService;
        this.userService = userService;
        this.adminService = adminService;
        this.searchService = searchService;
        this.projectRoleService = projectRoleService;
        this.issueConstantsService = issueConstantsService;
    }
	
	public String getAttachmentIdFromIssueKeyAndName(String token, String issueKey, String attachmentName) throws RemoteException
	{
        User user = tokenManager.retrieveUser(token);
		
		RemoteAttachment[] remoteAttachmentArray = issueService.getAttachmentsFromIssue(user, issueKey);
		
		String attachmentId = "FALSE";
		
		for (RemoteAttachment rmAttm : remoteAttachmentArray) {
			String attachmentNameFromSoap = rmAttm.getFilename();
			
			if (attachmentNameFromSoap.equals(attachmentName)) {
				attachmentId = rmAttm.getId();
			}
		}

//		String attachmentId = (remoteAttachmentArray[0]).getId();
		
		return attachmentId;
	}

    public void setTokenManager(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    /**
     * This ability to resolve a token into a user name is used by the JIRA logging and is not exposed on the SOAP
     * service interface itself but rather via {@link com.atlassian.jira.soap.axis.JiraSoapTokenResolver}.
     *
     * @param token the given out previously via {@link #login(String, String)}
     * @return the user name behind that token or null if the token is not valid
     */
    public String resolveTokenToUserName(final String token)
    {
        try
        {
            User user = tokenManager.retrieveUserNoPermissionCheck(token);
            return user == null ? null : user.getName();
        }
        catch (RemoteAuthenticationException e)
        {
            return null;
        }
        catch (RemotePermissionException e)
        {
            return null;
        }
    }

    /**
     * This is called to work out which parameter the token is given a method name
     *
     * @param operationName the name of the SOAP operation
     * @return the parameter index of the user token
     */
    public int getTokenParameterIndex(final String operationName)
    {
        if ("login".equals(operationName))
        {
            return -1;
        }
        return 0;
    }

    public String login(String username, String password) throws RemoteException
    {
        return tokenManager.login(username, password);
    }

    public boolean logout(String token)
    {
        return tokenManager.logout(token);
    }

    public RemoteServerInfo getServerInfo(String token)
    {
        return new RemoteServerInfo();
    }

    // PROJECT SERVICE METHODS
    public RemoteProject[] getProjectsNoSchemes(String token) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return projectService.getProjects(user, false);
    }

    public RemoteProject getProjectById(String token, Long projectId) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return projectService.getProjectById(user, projectId);
    }

    public RemoteProject getProjectWithSchemesById(String token, Long projectId) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return projectService.getProjectWithSchemesById(user, projectId);
    }

    public RemoteProject getProjectByKey(String token, String projectKey) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return projectService.getProjectByKey(user, projectKey);
    }

    public RemoteVersion addVersion(String token, String projectKey, RemoteVersion remoteVersion) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return projectService.createVersion(user, projectKey, remoteVersion);
    }

    public RemoteVersion[] getVersions(String token, String projectKey) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return projectService.getVersions(user, projectKey);
    }

    public RemoteComponent[] getComponents(String token, String projectKey) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return projectService.getComponents(user, projectKey);
    }

    // CONSTANTS METHODS
    public RemoteIssueType[] getIssueTypesForProject(String token, String projectId)
            throws RemotePermissionException, RemoteAuthenticationException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueConstantsService.getIssueTypesForProject(user, projectId);
    }

    public RemoteIssueType[] getSubTaskIssueTypesForProject(String token, String projectId)
            throws RemotePermissionException, RemoteAuthenticationException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueConstantsService.getSubTaskIssueTypesForProject(user, projectId);
    }

    public RemoteIssueType[] getIssueTypes(String token) throws RemotePermissionException, RemoteAuthenticationException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueConstantsService.getIssueTypes(user);
    }

    public RemoteIssueType[] getSubTaskIssueTypes(String token)
            throws RemotePermissionException, RemoteAuthenticationException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueConstantsService.getSubTaskIssueTypes(user);
    }

    public RemotePriority[] getPriorities(String token) throws RemotePermissionException, RemoteAuthenticationException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueConstantsService.getPriorities(user);
    }

    public RemoteStatus[] getStatuses(String token) throws RemotePermissionException, RemoteAuthenticationException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueConstantsService.getStatuses(user);
    }

    public RemoteResolution[] getResolutions(String token)
            throws RemotePermissionException, RemoteAuthenticationException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueConstantsService.getResolutions(user);
    }

    // USER METHODS
    public RemoteGroup getGroup(String token, String groupName) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return userService.getGroup(user, groupName);
    }

    public RemoteGroup createGroup(String token, String groupName, RemoteUser firstUser) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return userService.createGroup(user, groupName, firstUser);
    }

    public void addUserToGroup(String token, RemoteGroup group, RemoteUser ruser) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        userService.addUserToGroup(user, group, ruser);
    }

    public void removeUserFromGroup(String token, RemoteGroup group, RemoteUser ruser) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        userService.removeUserFromGroup(user, group, ruser);
    }

    public RemoteGroup updateGroup(String token, RemoteGroup group) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return userService.updateGroup(user, group);
    }

    public void deleteGroup(String token, String groupName, String swapGroupName) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        userService.deleteGroup(user, groupName, swapGroupName);
    }

    public RemoteUser getUser(String token, String username)
            throws RemotePermissionException, RemoteAuthenticationException
    {
        final User user = tokenManager.retrieveUser(token);
        return userService.getUser(user, username);
    }

    public RemoteUser createUser(String token, String username, String password, String fullName, String email)
            throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return userService.createUser(user, username, password, fullName, email);
    }

    public RemoteUser updateUser(String token, RemoteUser ruser) throws RemotePermissionException, RemoteAuthenticationException, RemoteValidationException, RemoteException
    {
        return userService.updateUser(tokenManager.retrieveUser(token), ruser);
    }

    public void setUserPassword(String token, RemoteUser ruser, String newPassword) throws RemotePermissionException, RemoteAuthenticationException, RemoteValidationException, RemoteException
    {
        userService.setUserPassword(tokenManager.retrieveUser(token), ruser, newPassword);
    }

    public void deleteUser(String token, String username) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        userService.deleteUser(user, username);
    }

    public RemoteFilter[] getSavedFilters(String token) throws RemoteException
    {
        return getFavouriteFilters(token);
    }

    public RemoteFilter[] getFavouriteFilters(String token) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return userService.getFavouriteFilters(user);
    }

    public RemoteNamedObject[] getAvailableActions(String token, String issueKey) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.getAvailableActions(user, issueKey);
    }

    public RemoteField[] getFieldsForAction(String token, String issueKey, String actionIdString) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.getFieldsForAction(user, issueKey, actionIdString);
    }


    public RemoteIssue progressWorkflowAction(String token, String issueKey, String actionIdString, RemoteFieldValue[] actionParams)
            throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.progressWorkflowAction(user, issueKey, actionIdString, actionParams);
    }

    // ISSUE METHODS
    public RemoteIssue getIssue(String token, String issueKey) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.getIssue(user, issueKey);
    }

    public Date getResolutionDateByKey(final String token, final String issueKey)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.getResolutionDateByKey(user, issueKey);
    }

    public Date getResolutionDateById(final String token, final Long issueId)
            throws RemotePermissionException, RemoteAuthenticationException, RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.getResolutionDateById(user, issueId);
    }

    public RemoteIssue createIssue(String token, RemoteIssue rIssue) throws RemoteException
    {
        return createIssueWithParentWithSecurityLevel(token,rIssue,null,null);
    }

    public RemoteIssue createIssueWithSecurityLevel(String token, RemoteIssue rIssue, Long securityLevelId)
            throws RemoteException
    {
        return createIssueWithParentWithSecurityLevel(token,rIssue,null,securityLevelId);
    }

    public RemoteIssue createIssueWithParent(String token, RemoteIssue rIssue, String parentIssueKey) throws RemoteException
    {
        return createIssueWithParentWithSecurityLevel(token,rIssue,parentIssueKey,null);
    }

    public RemoteIssue createIssueWithParentWithSecurityLevel(String token, RemoteIssue rIssue, String parentIssueKey, Long securityLevelId) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        try
        {
            return issueService.createIssueWithSecurityLevel(user, parentIssueKey, rIssue, securityLevelId);
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean addAttachmentsToIssue(String token, String issueKey, String[] fileNames, byte[][] attachments)
            throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.addAttachmentsToIssue(user, issueKey, fileNames, attachments);
    }

    public boolean addBase64EncodedAttachmentsToIssue(final String token, final String issueKey, final String[] fileNames, final String[] base64EncodedAttachmentData)
            throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        byte[][] dataBytes = null;
        if (base64EncodedAttachmentData != null)
        {
            dataBytes = new byte[base64EncodedAttachmentData.length][];
            for (int i = 0; i < base64EncodedAttachmentData.length; i++)
            {
                dataBytes[i] = Base64.decode(base64EncodedAttachmentData[i]);
            }
        }
        return issueService.addAttachmentsToIssue(user, issueKey, fileNames, dataBytes);
    }

    public RemoteAttachment[] getAttachmentsFromIssue(String token, String issueKey) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.getAttachmentsFromIssue(user, issueKey);
    }

    public RemoteIssue updateIssue(String token, String issueKey, RemoteFieldValue[] actionParams)
            throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.updateIssue(user, issueKey, actionParams);
    }

    public void deleteIssue(String token, String issueKey) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        issueService.deleteIssue(user, issueKey);
    }

    public RemoteField[] getFieldsForCreate(String token, String projectKey, Long issueTypeId) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.getFieldsForCreate(user, projectKey, issueTypeId);
    }

    public RemoteField[] getFieldsForEdit(String token, String issueKey) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.getFieldsForEdit(user, issueKey);
    }

    public RemoteProject createProject(String token, String key, String name, String description, String url, String lead, RemotePermissionScheme permissionScheme, RemoteScheme notificationScheme, RemoteScheme issueSecurityScheme)
            throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return projectService.createProject(user, key, name, description, url, lead, permissionScheme, notificationScheme, issueSecurityScheme);
    }

    public RemoteProject createProjectFromObject(String token, RemoteProject rproject) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return projectService.createProject(user, rproject);
    }

    public RemoteProject updateProject(String token, RemoteProject rProject) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return projectService.updateProject(user, rProject);
    }


    public void deleteProject(String token, String projectKey) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        projectService.deleteProject(user, projectKey);
    }

    public RemoteComment getComment(String token, Long id) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.getComment(user, id);
    }

    public RemoteComment[] getComments(String token, String issueKey) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.getComments(user, issueKey);
    }

    public void addComment(String token, String issueKey, RemoteComment remoteComment) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        issueService.addComment(user, issueKey, remoteComment);
    }

    public boolean hasPermissionToEditComment(String token, RemoteComment remoteComment) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.hasPermissionToEditComment(user, remoteComment);
    }

    public RemoteComment editComment(String token, RemoteComment remoteComment) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return issueService.editComment(user, remoteComment);
    }

    //SCHEME METHODS
    public RemoteScheme[] getNotificationSchemes(String token) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return schemeService.getNotificationSchemes(user);
    }

    public RemotePermissionScheme[] getPermissionSchemes(String token) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return schemeService.getPermissionSchemes(user);
    }

    public RemoteScheme[] getSecuritySchemes(String token) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return schemeService.getIssueSecuritySchemes(user);
    }

    public RemotePermission[] getAllPermissions(String token) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return schemeService.getAllPermissions(user);
    }

    public RemotePermissionScheme createPermissionScheme(String token, String name, String description)
            throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return schemeService.createPermissionScheme(user, name, description);
    }

    public RemotePermissionScheme addPermissionTo(String token, RemotePermissionScheme permissionScheme, RemotePermission permission, RemoteEntity remoteEntity)
            throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return schemeService.addPermissionTo(user, permissionScheme, permission, remoteEntity);
    }

    public RemotePermissionScheme deletePermissionFrom(String token, RemotePermissionScheme permissionSchemeName, RemotePermission permission, RemoteEntity remoteEntity)
            throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return schemeService.deletePermissionFrom(user, permissionSchemeName, permission, remoteEntity);
    }

    public void deletePermissionScheme(String token, String permissionSchemeName) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        schemeService.deletePermissionScheme(user, permissionSchemeName);
    }


    // General admin methods
    public RemoteField[] getCustomFields(String token) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return adminService.getCustomFields(user);
    }

    public void refreshCustomFields(String token) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        adminService.refreshCustomFields(user);
    }


    // -------------------- Search Methods ------------------------------ //
    public RemoteIssue[] getIssuesFromFilter(String token, String filterId) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return searchService.getIssues(user, filterId);
    }

    public RemoteIssue[] getIssuesFromFilterWithLimit(String token, String filterId, int offSet, int maxNumResults) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return searchService.getIssues(user, filterId, offSet, maxNumResults);
    }

    public RemoteIssue[] getIssuesFromTextSearch(String token, String searchTerms) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return searchService.getIssuesFromTextSearch(user, searchTerms);
    }

    public RemoteIssue[] getIssuesFromTextSearchWithLimit(String token, String searchTerms, int offSet, int maxNumResults) throws RemoteException
    {
        User user = tokenManager.retrieveUser(token);
        return searchService.getIssuesFromTextSearch(user, searchTerms, offSet, maxNumResults);
    }

    public RemoteIssue[] getIssuesFromTextSearchWithProject(String token, String[] projectKeys, String searchTerms, int maxNumResults)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return searchService.getIssuesFromTextSearchWithProject(user, projectKeys, searchTerms, maxNumResults);
    }

    public RemoteIssue[] getIssuesFromJqlSearch(final String token, final String jqlSearch, final int maxNumResults)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return searchService.getIssuesFromJqlSearch(user, jqlSearch, maxNumResults);
    }

    public long getIssueCountForFilter(String token, String filterId) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return searchService.getIssueCountForFilter(user, filterId);
    }

    public RemoteIssue getIssueById(String token, String issueId) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueService.getIssueById(user, issueId);
    }

    public RemoteConfiguration getConfiguration(String token) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return adminService.getConfiguration(user);
    }

    public RemoteProjectRole[] getProjectRoles(String token) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return projectRoleService.getProjectRoles(user);
    }

    public RemoteProjectRole getProjectRole(String token, Long id) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return projectRoleService.getProjectRole(user, id);
    }

    public RemoteProjectRole createProjectRole(String token, RemoteProjectRole projectRole) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return projectRoleService.createProjectRole(user, projectRole);
    }

    public boolean isProjectRoleNameUnique(String token, String name) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return projectRoleService.isProjectRoleNameUnique(user, name);
    }

    public void deleteProjectRole(String token, RemoteProjectRole projectRole, boolean confirm) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectRoleService.deleteProjectRole(user, projectRole, confirm);
    }

    public void addActorsToProjectRole(String token, String[] actors, RemoteProjectRole projectRole, RemoteProject project, String actorType)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectRoleService.addActorsToProjectRole(user, actors, projectRole, project, actorType);
    }

    public void removeActorsFromProjectRole(String token, String[] actors, RemoteProjectRole projectRole, RemoteProject project, String actorType)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectRoleService.removeActorsFromProjectRole(user, actors, projectRole, project, actorType);
    }

    public void updateProjectRole(String token, RemoteProjectRole projectRole) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectRoleService.updateProjectRole(user, projectRole);
    }

    public RemoteProjectRoleActors getProjectRoleActors(String token, RemoteProjectRole projectRole, RemoteProject project)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return projectRoleService.getProjectRoleActors(user, projectRole, project);
    }

    public RemoteRoleActors getDefaultRoleActors(String token, RemoteProjectRole projectRole) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return projectRoleService.getDefaultRoleActors(user, projectRole);
    }

    public void addDefaultActorsToProjectRole(String token, String[] actorNames, RemoteProjectRole projectRole, String type)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectRoleService.addDefaultActorsToProjectRole(user, actorNames, projectRole, type);
    }

    public void removeDefaultActorsFromProjectRole(String token, String[] actors, RemoteProjectRole projectRole, String actorType)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectRoleService.removeDefaultActorsFromProjectRole(user, actors, projectRole, actorType);
    }

    public void removeAllRoleActorsByNameAndType(String token, String name, String type) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectRoleService.removeAllRoleActorsByNameAndType(user, name, type);
    }

    public void removeAllRoleActorsByProject(String token, RemoteProject project) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectRoleService.removeAllRoleActorsByProject(user, project);
    }

    public RemoteScheme[] getAssociatedNotificationSchemes(String token, RemoteProjectRole projectRole)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return projectRoleService.getAssociatedNotificationSchemes(user, projectRole);
    }

    public RemoteScheme[] getAssociatedPermissionSchemes(String token, RemoteProjectRole projectRole)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return projectRoleService.getAssociatedPermissionSchemes(user, projectRole);
    }

    public void releaseVersion(String token, String projectKey, RemoteVersion remoteVersion) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectService.releaseVersion(user, projectKey, remoteVersion);
    }

    public void archiveVersion(String token, String projectKey, String versionName, boolean archive)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectService.archiveVersion(user, projectKey, versionName, archive);
    }

    public RemoteWorklog addWorklogAndAutoAdjustRemainingEstimate(String token, String issueKey, RemoteWorklog remoteWorklog)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueService.addWorklogAndAutoAdjustRemainingEstimate(user, issueKey, remoteWorklog);
    }

    public RemoteWorklog addWorklogAndRetainRemainingEstimate(String token, String issueKey, RemoteWorklog remoteWorklog)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueService.addWorklogAndRetainRemainingEstimate(user, issueKey, remoteWorklog);
    }

    public RemoteWorklog addWorklogWithNewRemainingEstimate(String token, String issueKey, RemoteWorklog remoteWorklog, String newRemainingEstimate)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueService.addWorklogWithNewRemainingEstimate(user, issueKey, remoteWorklog, newRemainingEstimate);
    }

    public RemoteWorklog[] getWorklogs(String token, String issueKey) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueService.getWorklogs(user, issueKey);
    }

    public void deleteWorklogAndAutoAdjustRemainingEstimate(String token, String worklogId) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        issueService.deleteWorklogAndAutoAdjustRemainingEstimate(user, worklogId);
    }

    public void deleteWorklogAndRetainRemainingEstimate(String token, String worklogId) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        issueService.deleteWorklogAndRetainRemainingEstimate(user, worklogId);
    }

    public void deleteWorklogWithNewRemainingEstimate(String token, String worklogId, String newRemainingEstimate)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        issueService.deleteWorklogWithNewRemainingEstimate(user, worklogId, newRemainingEstimate);
    }

    public void updateWorklogAndAutoAdjustRemainingEstimate(String token, RemoteWorklog remoteWorklog)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        issueService.updateWorklogAndAutoAdjustRemainingEstimate(user, remoteWorklog);
    }

    public void updateWorklogAndRetainRemainingEstimate(String token, RemoteWorklog remoteWorklog)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        issueService.updateWorklogAndRetainRemainingEstimate(user, remoteWorklog);
    }

    public void updateWorklogWithNewRemainingEstimate(String token, RemoteWorklog remoteWorklog, String newRemainingEstimate)
            throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        issueService.updateWorklogWithNewRemainingEstimate(user, remoteWorklog, newRemainingEstimate);
    }

    public boolean hasPermissionToCreateWorklog(String token, String issueKey) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueService.hasPermissionToCreateWorklog(user, issueKey);
    }

    public boolean hasPermissionToDeleteWorklog(String token, String worklogId) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueService.hasPermissionToDeleteWorklog(user, worklogId);
    }

    public boolean hasPermissionToUpdateWorklog(String token, String worklogId) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueService.hasPermissionToUpdateWorklog(user, worklogId);
    }

    public RemoteSecurityLevel getSecurityLevel(final String token, final String issueKey)
            throws RemoteException, RemotePermissionException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return issueService.getSecurityLevel(user, issueKey);
    }

    public RemoteSecurityLevel[] getSecurityLevels(final String token, final String projectKey)
            throws RemoteException, RemotePermissionException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return projectService.getSecurityLevels(user, projectKey);
    }

    public RemoteAvatar[] getProjectAvatars(final String token, final String projectKey, final boolean includeSystemAvatars)
            throws RemoteException, RemotePermissionException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return projectService.getProjectAvatars(user, projectKey, includeSystemAvatars, "large");
    }

    public void setNewProjectAvatar(final String token, final String projectKey, final String contentType, final String base64ImageData)
            throws RemoteException, RemotePermissionException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectService.setProjectAvatar(user, projectKey, contentType, base64ImageData);
    }

    public void setProjectAvatar(final String token, final String projectKey, final Long avatarId) throws RemoteException, RemotePermissionException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectService.setProjectAvatar(user, projectKey, avatarId);
    }

    public RemoteAvatar getProjectAvatar(final String token, final String projectKey) throws RemoteException, RemotePermissionException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        return projectService.getProjectAvatar(user, projectKey, "large");
    }

    public void deleteProjectAvatar(final String token, final long avatarId) throws RemoteException
    {
        User user = tokenManager.retrieveUserNoPermissionCheck(token);
        projectService.deleteProjectAvatar(user, avatarId);
    }
}
