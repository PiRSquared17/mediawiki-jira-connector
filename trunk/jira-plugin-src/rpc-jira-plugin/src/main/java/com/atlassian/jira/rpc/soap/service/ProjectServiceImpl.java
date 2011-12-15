package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import static com.atlassian.jira.avatar.Avatar.Type.PROJECT;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.soap.beans.RemoteAvatar;
import com.atlassian.jira.rpc.soap.beans.RemoteComponent;
import com.atlassian.jira.rpc.soap.beans.RemotePermissionScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import com.atlassian.jira.rpc.soap.beans.RemoteScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteSecurityLevel;
import com.atlassian.jira.rpc.soap.beans.RemoteVersion;
import com.atlassian.jira.rpc.soap.util.SoapUtils;
import com.atlassian.jira.rpc.soap.util.RemoteEntityFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Base64InputStreamConsumer;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ProjectServiceImpl implements ProjectService
{
    private final PermissionManager permissionManager;

    private final ApplicationProperties applicationProperties;
    private final NotificationSchemeManager notificationSchemeManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final com.atlassian.jira.bc.project.ProjectService projectService;
    private final ProjectComponentManager projectComponentManager;
    private final VersionService versionService;
    private final OutlookDateManager outlookDateManager;
    private final UserManager userManager;
    private AvatarManager avatarManager;
    private final RemoteEntityFactory remoteEntityFactory;

    private static final Logger log = Logger.getLogger(ProjectServiceImpl.class);
    private static final String AVATAR_DEFAULT_BASE_FILENAME = "soapCreatedAvatar";
    private static final String BASE_64_TEXT_ENCODING = "UTF-8";

    public ProjectServiceImpl(final PermissionManager permissionManager,
            final ApplicationProperties applicationProperties,
            final NotificationSchemeManager notificationSchemeManager,
            final PermissionSchemeManager permissionSchemeManager,
            final IssueSecuritySchemeManager issueSecuritySchemeManager,
            final IssueSecurityLevelManager issueSecurityLevelManager,
            final com.atlassian.jira.bc.project.ProjectService projectService,
            final ProjectComponentManager projectComponentManager,
            final VersionService versionService,
            final OutlookDateManager outlookDateManager,
            final UserManager userManager,
            final AvatarManager avatarManager,
            final RemoteEntityFactory remoteEntityFactory)
    {
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
        this.notificationSchemeManager = notificationSchemeManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.projectService = projectService;
        this.projectComponentManager = projectComponentManager;
        this.versionService = versionService;
        this.outlookDateManager = outlookDateManager;
        this.avatarManager = avatarManager;
        this.userManager = userManager;
        this.remoteEntityFactory = remoteEntityFactory;
    }

    public RemoteProject[] getProjects(User user, boolean addSchemes) throws RemoteException
    {
        try
        {
            Collection<GenericValue> projects = permissionManager.getProjects(Permissions.BROWSE, user);
            RemoteProject[] remoteProjects = SoapUtils.getProjects(projects, applicationProperties);

            if (addSchemes)
            {
                List<GenericValue> projs = new ArrayList<GenericValue>(projects);

                for (int i = 0; i < remoteProjects.length; i++)
                {
                    this.addRemoteSchemes(user, remoteProjects[i], projs.get(i));
                }
            }

            return remoteProjects;
        }
        catch (GenericEntityException e)
        {
            throw new RemoteException("Error getting projects: " + e, e);
        }
    }

    public RemoteProject getProjectById(User user, Long projectId) throws RemoteException
    {
        Project project = retrieveProjectById(projectId, user);
        return new RemoteProject(project, applicationProperties);
    }

    public RemoteProject getProjectWithSchemesById(User user, Long projectId) throws RemoteException
    {
        try
        {
            Project project = retrieveProjectById(projectId, user);
            final RemoteProject remoteProject = new RemoteProject(project, applicationProperties);
            addRemoteSchemes(user, remoteProject, project.getGenericValue());
            return remoteProject;
        }
        catch (GenericEntityException e)
        {
            throw new RemoteException("Error getting schemes for project with id: " + projectId, e);
        }
    }

    public RemoteProject getProjectByKey(User user, String projectKey) throws RemoteException
    {
        Project project = retrieveProjectByKey(projectKey, user);
        return new RemoteProject(project, applicationProperties);
    }

    public RemoteComponent[] getComponents(User user, String projectKey) throws RemoteException
    {
        try
        {
            Project project = retrieveProjectByKey(projectKey, user);
            Collection components = projectComponentManager.findAllForProject(project.getId());
            return SoapUtils.convertComponentsToRemoteObject(components);
        }
        catch (DataAccessException e)
        {
            throw new RemoteException("Error retrieving components: " + e, e);
        }
    }

    public RemoteVersion[] getVersions(User user, String projectKey) throws RemoteException
    {
        try
        {
            Project project = retrieveProjectByKey(projectKey, user);
            VersionService.VersionsResult result = versionService.getVersionsByProject(user, project);
            if (!result.isValid())
            {
                throw new RemoteValidationException("Error retrieving versions: ", result.getErrorCollection());
            }
            return SoapUtils.convertVersionsToRemoteObject(result.getVersions());
        }
        catch (DataAccessException e)
        {
            throw new RemoteException("Error retrieving versions: " + e, e);
        }
    }

    public RemoteProject createProject(User user, RemoteProject rProject) throws RemoteException
    {
        //Note: The permissionScheme should never be null, but just in case.
        final Long permissionSchemeId = rProject.getPermissionScheme() == null ? null : rProject.getPermissionScheme().getId();
        final Long notificationSchemeId = rProject.getNotificationScheme() == null ? null : rProject.getNotificationScheme().getId();
        final Long issueSecuritySchemeId = rProject.getIssueSecurityScheme() == null ? null : rProject.getIssueSecurityScheme().getId();

        com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult result =
                projectService.validateCreateProject(user, rProject.getName(), rProject.getKey(), rProject.getDescription(),
                        rProject.getLead(), rProject.getProjectUrl(), null);

        com.atlassian.jira.bc.project.ProjectService.UpdateProjectSchemesValidationResult schemesResult =
                projectService.validateUpdateProjectSchemes(user, permissionSchemeId, notificationSchemeId, issueSecuritySchemeId);

        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorCollection(result.getErrorCollection());
        errors.addErrorCollection(schemesResult.getErrorCollection());

        if (errors.hasAnyErrors())
        {
            throw new RemoteValidationException("Cannot create project: ", errors);
        }

        final Project newProject = projectService.createProject(result);
        projectService.updateProjectSchemes(schemesResult, newProject);

        final RemoteProject remoteProject = new RemoteProject(newProject, applicationProperties);
        remoteProject.setNotificationScheme(rProject.getNotificationScheme());
        remoteProject.setPermissionScheme(rProject.getPermissionScheme());
        remoteProject.setIssueSecurityScheme(rProject.getIssueSecurityScheme());

        return remoteProject;
    }

    public RemoteProject createProject(User user, String key, String name, String description, String url, String lead,
            RemotePermissionScheme permissionScheme, RemoteScheme notificationScheme,
            RemoteScheme issueSecurityScheme) throws RemoteException
    {
        //create new project stub
        RemoteProject rProject = new RemoteProject();
        rProject.setKey(key);
        rProject.setName(name);
        rProject.setDescription(description);
        rProject.setUrl(applicationProperties.getString(APKeys.JIRA_BASEURL) + "/browse/" + key);
        rProject.setProjectUrl(url);
        rProject.setLead(lead);
        rProject.setPermissionScheme(permissionScheme);
        rProject.setNotificationScheme(notificationScheme);
        rProject.setIssueSecurityScheme(issueSecurityScheme);

        return this.createProject(user, rProject);
    }

    public RemoteProject updateProject(User user, RemoteProject rProject) throws RemoteException
    {
        //Note: The permissionScheme should never be null, but just in case.
        final Long permissionSchemeId = rProject.getPermissionScheme() == null ? null : rProject.getPermissionScheme().getId();
        final Long notificationSchemeId = rProject.getNotificationScheme() == null ? null : rProject.getNotificationScheme().getId();
        final Long issueSecuritySchemeId = rProject.getIssueSecurityScheme() == null ? null : rProject.getIssueSecurityScheme().getId();

        com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult result =
                projectService.validateUpdateProject(user, rProject.getName(), rProject.getKey(), rProject.getDescription(),
                        rProject.getLead(), rProject.getProjectUrl(), null);

        com.atlassian.jira.bc.project.ProjectService.UpdateProjectSchemesValidationResult schemesResult =
                projectService.validateUpdateProjectSchemes(user, permissionSchemeId, notificationSchemeId, issueSecuritySchemeId);

        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorCollection(result.getErrorCollection());
        errors.addErrorCollection(schemesResult.getErrorCollection());

        if (errors.hasAnyErrors())
        {
            throw new RemoteValidationException("Cannot update project: ", errors);
        }

        final Project updatedProject = projectService.updateProject(result);
        projectService.updateProjectSchemes(schemesResult, updatedProject);

        final RemoteProject remoteProject = new RemoteProject(updatedProject, applicationProperties);
        remoteProject.setNotificationScheme(rProject.getNotificationScheme());
        remoteProject.setPermissionScheme(rProject.getPermissionScheme());
        remoteProject.setIssueSecurityScheme(rProject.getIssueSecurityScheme());

        return remoteProject;
    }

    public void deleteProject(User user, String projectKey) throws RemoteException
    {
        com.atlassian.jira.bc.project.ProjectService.DeleteProjectValidationResult result =
                projectService.validateDeleteProject(user, projectKey);
        if (!result.isValid())
        {
            throw new RemoteValidationException("Error removing project: ", result.getErrorCollection());
        }
        com.atlassian.jira.bc.project.ProjectService.DeleteProjectResult deleteResult = projectService.deleteProject(user, result);
        if (!deleteResult.isValid())
        {
            throw new RemoteValidationException("Error removing project: ", deleteResult.getErrorCollection());
        }
    }


    void addRemoteSchemes(User user, RemoteProject remoteProject, GenericValue project)
            throws GenericEntityException, RemoteException
    {
        List notificationSchemes = notificationSchemeManager.getSchemes(project);
        if (notificationSchemes != null)
        {
            Iterator it = notificationSchemes.iterator();
            if (it.hasNext())
            {
                GenericValue notificationScheme = (GenericValue) it.next();
                remoteProject.setNotificationScheme(new RemoteScheme(notificationScheme, "notification"));
            }
        }

        List permissionSchemes = permissionSchemeManager.getSchemes(project);
        if (permissionSchemes != null)
        {
            Iterator it = permissionSchemes.iterator();
            if (it.hasNext())
            {
                GenericValue permissionScheme = (GenericValue) it.next();
                SchemePermissions schemePermissions = new SchemePermissions();
                ServiceHelper serviceHelper = new ServiceHelper(permissionManager, permissionSchemeManager, schemePermissions, remoteEntityFactory, userManager);
                RemotePermissionScheme scheme = null;
                try
                {
                    scheme = serviceHelper.populateSchemePermissions(user, permissionScheme);
                }
                catch (Exception e)
                {
                    scheme = new RemotePermissionScheme(permissionScheme);
                }
                remoteProject.setPermissionScheme(scheme);
            }
        }

        List issueSecuritySchemes = issueSecuritySchemeManager.getSchemes(project);
        if (issueSecuritySchemes != null)
        {
            Iterator it = issueSecuritySchemes.iterator();
            if (it.hasNext())
            {
                GenericValue issueSecurityScheme = (GenericValue) it.next();
                remoteProject.setIssueSecurityScheme(new RemoteScheme(issueSecurityScheme, "issueSecurity"));
            }
        }
    }

    Project retrieveProjectById(Long id, User user) throws RemoteException
    {
        final com.atlassian.jira.bc.project.ProjectService.GetProjectResult result = projectService.getProjectById(user, id);
        if (!result.isValid())
        {
            throw new RemoteValidationException("Error retrieving project with id '" + id + "':", result.getErrorCollection());
        }
        else
        {
            return result.getProject();
        }
    }

    /**
     * Retrieve a single project, throwing an exception if the user has no permissions.
     */
    Project retrieveProjectByKey(String projectKey, User user)
            throws RemoteException
    {
        try
        {
            final com.atlassian.jira.bc.project.ProjectService.GetProjectResult result = projectService.getProjectByKey(user, projectKey);
            if (!result.isValid())
            {
                throw new RemoteValidationException("Error retrieving project with key '" + projectKey + "':", result.getErrorCollection());
            }
            else
            {
                return result.getProject();
            }
        }
        catch (DataAccessException e)
        {
            throw new RemoteException("Error retrieving project: " + projectKey, e);
        }
    }

    public RemoteVersion createVersion(User user, String projectKey, RemoteVersion remoteVersion) throws RemoteException
    {
        final Project project = retrieveProjectByKey(projectKey, user);

        final I18nBean i18nBean = new I18nBean(user);
        final OutlookDate outlookDate = outlookDateManager.getOutlookDate(i18nBean.getLocale());
        final String releaseDate = (remoteVersion.getReleaseDate()) != null ? outlookDate.formatDatePicker(remoteVersion.getReleaseDate()) : null;

        // Workout the sequence number
        final Long scheduleAfterVersion = getScheduleAfter(user, project, remoteVersion);
        VersionService.CreateVersionValidationResult result = versionService.validateCreateVersion(user, project, remoteVersion.getName(), releaseDate, null, scheduleAfterVersion);
        if (!result.isValid())
        {
            throw new RemoteValidationException("createVersion validation error: ", result.getErrorCollection());
        }

        try
        {
            Version version = versionService.createVersion(user, result);

            // Release the version or archive the version if this is set
            if (remoteVersion.isReleased())
            {
                VersionService.ReleaseVersionValidationResult releaseResult = versionService.validateReleaseVersion(user, version, remoteVersion.getReleaseDate());
                if (!releaseResult.isValid())
                {
                    throw new RemoteValidationException("Release version: ", releaseResult.getErrorCollection());
                }
                version = versionService.releaseVersion(releaseResult);
            }
            else if (remoteVersion.isArchived())
            {
                VersionService.ArchiveVersionValidationResult archiveResult = versionService.validateArchiveVersion(user, version);
                if (!archiveResult.isValid())
                {
                    throw new RemoteValidationException("Archive version: ", archiveResult.getErrorCollection());
                }
                version = versionService.archiveVersion(archiveResult);
            }

            return new RemoteVersion(version.getGenericValue());
        }
        catch (Exception e)
        {
            throw new RemoteException("Error creating version", e);
        }
    }

    private Version getVersionByProjectAndName(User user, String projectKey, String versionName) throws RemoteException
    {
        Project project = retrieveProjectByKey(projectKey, user);

        VersionService.VersionResult versionResult = versionService.getVersionByProjectAndName(user, project, versionName);
        if (!versionResult.isValid())
        {
            throw new RemoteValidationException("Version not found: ", versionResult.getErrorCollection());
        }
        return versionResult.getVersion();
    }

    public void releaseVersion(User user, String projectKey, RemoteVersion remoteVersion) throws RemoteException
    {
        Version version = getVersionByProjectAndName(user, projectKey, remoteVersion.getName());
        if (remoteVersion.isReleased())
        {
            VersionService.ReleaseVersionValidationResult result = versionService.validateReleaseVersion(user, version, remoteVersion.getReleaseDate());
            if (!result.isValid())
            {
                throw new RemoteValidationException("Release version: ", result.getErrorCollection());
            }
            versionService.releaseVersion(result);
        }
        else
        {
            VersionService.ReleaseVersionValidationResult result = versionService.validateUnreleaseVersion(user, version, remoteVersion.getReleaseDate());
            if (!result.isValid())
            {
                throw new RemoteValidationException("Unrelease version: ", result.getErrorCollection());
            }
            versionService.unreleaseVersion(result);
        }
    }

    public void archiveVersion(User user, String projectKey, String versionName, boolean archive) throws RemoteException
    {
        Version version = getVersionByProjectAndName(user, projectKey, versionName);
        if (archive)
        {
            VersionService.ArchiveVersionValidationResult result = versionService.validateArchiveVersion(user, version);
            if (!result.isValid())
            {
                throw new RemoteValidationException("Archive version: ", result.getErrorCollection());
            }
            versionService.archiveVersion(result);
        }
        else
        {
            VersionService.ArchiveVersionValidationResult result = versionService.validateUnarchiveVersion(user, version);
            if (!result.isValid())
            {
                throw new RemoteValidationException("Unarchive version: ", result.getErrorCollection());
            }
            versionService.unarchiveVersion(result);
        }
    }

    public RemoteSecurityLevel[] getSecurityLevels(final User user, final String projectKey)
            throws RemoteException
    {
        final Project project = retrieveProjectByKey(projectKey, user);
        if (!permissionManager.hasPermission(Permissions.SET_ISSUE_SECURITY, project, user))
        {
            return new RemoteSecurityLevel[0];
        }
        final List<GenericValue> usersSecurityLevels;
        try
        {
            usersSecurityLevels = issueSecurityLevelManager.getUsersSecurityLevels(project.getGenericValue(), user);
        }
        catch (GenericEntityException e)
        {
            throw new RemoteException("Error getting security levels for project '" + projectKey + "'" + e.getMessage(), e);
        }
        RemoteSecurityLevel[] remoteSecurityLevels = new RemoteSecurityLevel[usersSecurityLevels.size()];
        for (int i = 0; i < usersSecurityLevels.size(); i++)
        {
            remoteSecurityLevels[i] = new RemoteSecurityLevel(usersSecurityLevels.get(i));
        }
        return remoteSecurityLevels;
    }

    public RemoteAvatar[] getProjectAvatars(final User user, final String projectKey, final boolean includeSystemAvatars, final String size) throws RemoteException, RemotePermissionException
    {
        final boolean large = "large".equalsIgnoreCase(size);
        final Project project = retrieveProjectByKey(projectKey, user);
        final ArrayList<Avatar> avatars = new ArrayList<Avatar>();
        if (includeSystemAvatars)
        {
            avatars.addAll(avatarManager.getAllSystemAvatars(Avatar.Type.PROJECT));
        }
        avatars.addAll(avatarManager.getCustomAvatarsForOwner(PROJECT, project.getId().toString()));

        RemoteAvatar[] remoteAvatars = new RemoteAvatar[avatars.size()];
        for (int i = 0; i < avatars.size(); i++)
        {
            remoteAvatars[i] = createRemoteAvatar(avatars.get(i), large, new Base64InputStreamConsumer(true));
        }
        return remoteAvatars;
    }

    public void setProjectAvatar(final User user, final String projectKey, final String contentType, final String base64ImageData)
            throws RemoteException
    {
        final Project project = retrieveProjectForAdministration(user, projectKey);
        final String owner = project.getId().toString();

        try
        {
            byte[] decoded = Base64.decodeBase64(base64ImageData.getBytes(BASE_64_TEXT_ENCODING));
            ByteArrayInputStream bais = new ByteArrayInputStream(decoded);
            final Avatar toCreate = AvatarImpl.createCustomAvatar(AVATAR_DEFAULT_BASE_FILENAME, contentType, PROJECT, owner);
            Avatar createdAvatar = avatarManager.create(toCreate, bais, AvatarManager.LARGE.originSelection());
            IOUtil.shutdownStream(bais);
            final Long avatarId = createdAvatar.getId();
            final com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult result = validateSetAvatar(user, project, avatarId);
            if (result.isValid())
            {
                projectService.updateProject(result);
            }
            else
            {
                // This shouldn't happen since we checked the permission criteria up front.
                log.error("Problem setting avatar on project, removing SOAP created avatar");
                avatarManager.delete(createdAvatar.getId(), true);
                throw new RemoteException("Error setting Avatar on project: " + result.getErrorCollection());
            }
        }
        catch (IOException e)
        {
            // This shouldn't happen, only if a BAIS throws an IOException or if UTF-8 is unsupported.
            throw new RemoteException(e.getMessage());
        }
    }

    public void setProjectAvatar(final User user, final String projectKey, final Long avatarId) throws RemoteException
    {
        Project project = retrieveProjectForAdministration(user, projectKey);
        final com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult result = validateSetAvatar(user, project, avatarId);
        if (result.isValid())
        {
            projectService.updateProject(result);
        }
        else
        {
            throw new RemoteException("Error setting Avatar on project: " + result.getErrorCollection());
        }
    }

    public RemoteAvatar getProjectAvatar(final User user, final String projectKey, final String size) throws RemoteException, RemotePermissionException
    {
        boolean large = "large".equalsIgnoreCase(size);
        final Project project = retrieveProjectByKey(projectKey, user);
        return createRemoteAvatar(project.getAvatar(), large, new Base64InputStreamConsumer(true));
    }

    public void deleteProjectAvatar(final User user, final long avatarId) throws RemoteException
    {
        final Avatar avatar = avatarManager.getById(avatarId);
        // only when the avtatar is system, the owner is null, but we check either since we cannot proceed either way
        if (avatar == null || avatar.isSystemAvatar() || avatar.getOwner() == null)
        {
            throw new RemoteException("No such custom Avatar with id " + avatarId);
        }
        Project project = retrieveProjectForAdministration(user, Long.valueOf(avatar.getOwner()));

        final boolean deletingTheCurrentAvatar = project.getAvatar().getId() == avatarId;
        if (deletingTheCurrentAvatar)
        {
            // deleting the current one need to switch current avatar to default
            final Long defaultAvatarId = avatarManager.getDefaultAvatarId(Avatar.Type.PROJECT);
            final com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult updateProjectValidationResult = validateSetAvatar(user, project, defaultAvatarId);

            if (updateProjectValidationResult.isValid())
            {
                projectService.updateProject(updateProjectValidationResult);
                avatarManager.delete(avatarId, true);
            }
        }
        else
        {
            avatarManager.delete(avatarId, true);
        }
    }

    RemoteAvatar createRemoteAvatar(final Avatar a, final boolean large, final Base64InputStreamConsumer data)
    {
        final long id = a.getId();
        final String contentType = a.getContentType();
        final String type = a.getAvatarType().getName();
        try
        {
            if (large)
            {
                avatarManager.readLargeAvatarData(a, data);
            }
            else
            {
                avatarManager.readSmallAvatarData(a, data);
            }
        }
        catch (IOException e)
        {
            log.error("Exception trying to get Avatar image data, continuing without it", e);
        }
        return new RemoteAvatar(id, contentType, a.getOwner(), a.isSystemAvatar(), type, data.getEncoded());
    }

    com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult validateSetAvatar(final User user, final Project project, final Long avatarId)
    {
        Long effectiveAvatarId = avatarId == null ? avatarManager.getDefaultAvatarId(PROJECT) : avatarId;
        return projectService.validateUpdateProject(
                user,
                project.getName(),
                project.getKey(),
                project.getDescription(),
                project.getLeadUserName(),
                project.getUrl(),
                project.getAssigneeType(),
                effectiveAvatarId);
    }

    private Project retrieveProjectForAdministration(final User user, final long projectId) throws RemoteException
    {
        final Project project = retrieveProjectById(projectId, user);
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user)
            && !permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user))
        {
            throw new RemotePermissionException("No permission to administer the project");
        }
        return project;
    }

    private Project retrieveProjectForAdministration(final User user, final String projectKey)
            throws RemoteException
    {
        final Project project = retrieveProjectByKey(projectKey, user);
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user)
            && !permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user))
        {
            throw new RemotePermissionException("No permission to administer the project");
        }
        return project;
    }

    private Long getScheduleAfter(User user, Project project, RemoteVersion remoteVersion)
            throws RemoteValidationException
    {
        final Long sequence = remoteVersion.getSequence();
        if (sequence != null && sequence != -1)
        {
            int seq = (int) sequence.longValue() - 1;
            VersionService.VersionsResult result = versionService.getVersionsByProject(user, project);
            if (!result.isValid())
            {
                throw new RemoteValidationException("Error retrieving versions: ", result.getErrorCollection());
            }
            final List<Version> versions = new ArrayList(result.getVersions());
            if (seq > versions.size())
            {
                return null;
            }
            else if (seq < 1)
            {
                return -1L;
            }
            else
            {
                Version v = (Version) versions.get(seq - 1);
                return v.getId();
            }
        }
        else
        {
            return sequence;
        }
    }
}
