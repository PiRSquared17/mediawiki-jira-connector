package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import com.atlassian.jira.rpc.soap.beans.RemoteProjectRole;
import com.atlassian.jira.rpc.soap.beans.RemoteProjectRoleActors;
import com.atlassian.jira.rpc.soap.beans.RemoteRoleActor;
import com.atlassian.jira.rpc.soap.beans.RemoteRoleActors;
import com.atlassian.jira.rpc.soap.beans.RemoteScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;
import com.atlassian.jira.rpc.soap.util.RemoteEntityFactory;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ProjectRoleServiceImpl implements ProjectRoleService
{
    private com.atlassian.jira.bc.projectroles.ProjectRoleService projectRoleService;
    private ProjectManager projectManager;
    private ProjectFactory projectFactory;
    private final RemoteEntityFactory remoteEntityFactory;

    public ProjectRoleServiceImpl(com.atlassian.jira.bc.projectroles.ProjectRoleService projectRoleService, ProjectManager projectManager, ProjectFactory projectFactory, final RemoteEntityFactory remoteEntityFactory)
    {
        this.projectRoleService = projectRoleService;
        this.projectManager = projectManager;
        this.projectFactory = projectFactory;
        this.remoteEntityFactory = remoteEntityFactory;
    }

    public RemoteProjectRole[] getProjectRoles(User currentUser) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        Collection remoteRoles = new ArrayList();
        Collection realRoles = projectRoleService.getProjectRoles(currentUser, errorCollection);

        checkAndThrowErrors("Error getting project role", errorCollection);

        for (Iterator iterator = realRoles.iterator(); iterator.hasNext();)
        {
            ProjectRole projectRole = (ProjectRole) iterator.next();
            remoteRoles.add(new RemoteProjectRole(projectRole.getId(), projectRole.getName(), projectRole.getDescription()));
        }

        return (RemoteProjectRole[]) remoteRoles.toArray(new RemoteProjectRole[remoteRoles.size()]);
    }

    public RemoteProjectRole getProjectRole(User currentUser, Long id) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole realRole = projectRoleService.getProjectRole(currentUser, id, errorCollection);

        checkAndThrowErrors("Error getting project role", errorCollection);

        return new RemoteProjectRole(realRole.getId(), realRole.getName(), realRole.getDescription());
    }

    public RemoteProjectRole createProjectRole(User currentUser, RemoteProjectRole projectRole) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole realRole = getProjectRoleFromRemoteProjectRole(projectRole);

        realRole = projectRoleService.createProjectRole(currentUser, realRole, errorCollection);

        checkAndThrowErrors("Error creating project role", errorCollection);

        return new RemoteProjectRole(realRole.getId(), realRole.getName(), realRole.getDescription());
    }

    public boolean isProjectRoleNameUnique(User currentUser, String name) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        boolean isUnique = projectRoleService.isProjectRoleNameUnique(currentUser, name, errorCollection);

        checkAndThrowErrors("Error checking unique role name", errorCollection);

        return isUnique;
    }

    public void deleteProjectRole(User currentUser, RemoteProjectRole projectRole, boolean confirm) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole realRole = getProjectRoleFromRemoteProjectRole(projectRole);

        // If they are not passing the force flag then make sure there are no associations, throw up an exception
        // if there are.
        if (!confirm)
        {
            checkIfRoleHasAssociations(currentUser, realRole, errorCollection, projectRole);
        }

        projectRoleService.deleteProjectRole(currentUser, realRole, errorCollection);

        checkAndThrowErrors("Error deleting project role", errorCollection);
    }

    private void checkIfRoleHasAssociations(User currentUser, ProjectRole realRole, SimpleErrorCollection errorCollection, RemoteProjectRole projectRole) throws RemoteException
    {
        Collection associatedSchemes = new ArrayList();
        associatedSchemes.addAll(projectRoleService.getAssociatedNotificationSchemes(currentUser, realRole, errorCollection));
        associatedSchemes.addAll(projectRoleService.getAssociatedPermissionSchemes(currentUser, realRole, errorCollection));

        if (!associatedSchemes.isEmpty())
        {
            StringBuffer message = new StringBuffer("Project Role: ");
            message.append(projectRole.getName());
            message.append(" is associated with the following scheme(s): ");
            for (Iterator iterator = associatedSchemes.iterator(); iterator.hasNext();)
            {
                GenericValue scheme = (GenericValue) iterator.next();
                message.append(scheme.getString("name"));
                if (iterator.hasNext())
                {
                    message.append(", ");
                }
            }
            message.append(". To force deletion of this role make the confirm parameter true.");
            throw new RemoteException(message.toString());
        }
    }

    public void addActorsToProjectRole(User currentUser, String[] actors, RemoteProjectRole projectRole, RemoteProject project, String actorType) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        Project realProject = getProjectFromRemoteProject(project);
        ProjectRole realRole = getProjectRoleFromRemoteProjectRole(projectRole);

        projectRoleService.addActorsToProjectRole(currentUser, Arrays.asList(actors), realRole, realProject, actorType, errorCollection);

        checkAndThrowErrors("Error adding actors to project role", errorCollection);
    }

    public void removeActorsFromProjectRole(User currentUser, String[] actors, RemoteProjectRole projectRole, RemoteProject project, String actorType) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        Project realProject = getProjectFromRemoteProject(project);
        ProjectRole realRole = getProjectRoleFromRemoteProjectRole(projectRole);

        projectRoleService.removeActorsFromProjectRole(currentUser, Arrays.asList(actors), realRole, realProject, actorType, errorCollection);

        checkAndThrowErrors("Error removing actors from project role", errorCollection);
    }

    public void updateProjectRole(User currentUser, RemoteProjectRole projectRole) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole realRole = getProjectRoleFromRemoteProjectRole(projectRole);
        projectRoleService.updateProjectRole(currentUser, realRole, errorCollection);

        checkAndThrowErrors("Error updating project role", errorCollection);
    }

    public RemoteProjectRoleActors getProjectRoleActors(User currentUser, RemoteProjectRole projectRole, RemoteProject project) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole realRole = getProjectRoleFromRemoteProjectRole(projectRole);
        Project realProject = getProjectFromRemoteProject(project);
        ProjectRoleActors projectRoleActors = projectRoleService.getProjectRoleActors(currentUser, realRole, realProject, errorCollection);

        checkAndThrowErrors("Error getting project role actors", errorCollection);

        return convertProjectRoleActorsToRemote(projectRoleActors, projectRole, project);
    }

    public RemoteRoleActors getDefaultRoleActors(User currentUser, RemoteProjectRole projectRole) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole realRole = getProjectRoleFromRemoteProjectRole(projectRole);
        DefaultRoleActors projectRoleActors = projectRoleService.getDefaultRoleActors(currentUser, realRole, errorCollection);

        checkAndThrowErrors("Error getting default role actors", errorCollection);

        return convertRoleActorsToRemote(projectRoleActors, projectRole);
    }

    public void addDefaultActorsToProjectRole(User currentUser, String[] actorNames, RemoteProjectRole projectRole, String type) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole realRole = getProjectRoleFromRemoteProjectRole(projectRole);

        projectRoleService.addDefaultActorsToProjectRole(currentUser, Arrays.asList(actorNames), realRole, type, errorCollection);

        checkAndThrowErrors("Error adding default actors to project role", errorCollection);
    }

    public void removeDefaultActorsFromProjectRole(User currentUser, String[] actors, RemoteProjectRole projectRole, String actorType) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole realRole = getProjectRoleFromRemoteProjectRole(projectRole);

        projectRoleService.removeDefaultActorsFromProjectRole(currentUser, Arrays.asList(actors), realRole, actorType, errorCollection);

        checkAndThrowErrors("Error removing default actors from project role", errorCollection);
    }

    public void removeAllRoleActorsByNameAndType(User currentUser, String name, String type) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        projectRoleService.removeAllRoleActorsByNameAndType(currentUser, name, type, errorCollection);

        checkAndThrowErrors("Error removing role actors by name and type", errorCollection);
    }

    public void removeAllRoleActorsByProject(User currentUser, RemoteProject project) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        Project realProject = getProjectFromRemoteProject(project);

        projectRoleService.removeAllRoleActorsByProject(currentUser, realProject, errorCollection);

        checkAndThrowErrors("Error removing role actors by project", errorCollection);
    }

    public RemoteScheme[] getAssociatedNotificationSchemes(User currentUser, RemoteProjectRole projectRole) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole realRole = getProjectRoleFromRemoteProjectRole(projectRole);
        Collection notificationSchemes = projectRoleService.getAssociatedNotificationSchemes(currentUser, realRole, errorCollection);
        return convertSchemesToRemote(notificationSchemes, "notification");
    }

    private RemoteScheme[] convertSchemesToRemote(Collection notificationSchemes, String type)
    {
        List remoteSchemes = new ArrayList();
        for (Iterator iterator = notificationSchemes.iterator(); iterator.hasNext();)
        {
            GenericValue scheme = (GenericValue) iterator.next();

            remoteSchemes.add(new RemoteScheme(scheme, type));
        }
        return (RemoteScheme[]) remoteSchemes.toArray(new RemoteScheme[remoteSchemes.size()]);
    }

    public RemoteScheme[] getAssociatedPermissionSchemes(User currentUser, RemoteProjectRole projectRole) throws RemoteException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRole realRole = getProjectRoleFromRemoteProjectRole(projectRole);
        Collection permissionSchemes = projectRoleService.getAssociatedPermissionSchemes(currentUser, realRole, errorCollection);
        return convertSchemesToRemote(permissionSchemes, "permission");
    }

    private RemoteProjectRoleActors convertProjectRoleActorsToRemote(ProjectRoleActors projectRoleActors, RemoteProjectRole projectRole, RemoteProject project)
    {
        RemoteRoleActor[] remoteRoleActorsArr = populateRemoteRoleActors(projectRoleActors, projectRole);
        return new RemoteProjectRoleActors(project, projectRole, remoteRoleActorsArr);
    }

    private RemoteRoleActors convertRoleActorsToRemote(DefaultRoleActors defaultRoleActors, RemoteProjectRole projectRole)
    {
        RemoteRoleActor[] remoteRoleActorsArr = populateRemoteRoleActors(defaultRoleActors, projectRole);
        return new RemoteRoleActors(projectRole, remoteRoleActorsArr);
    }

    private RemoteRoleActor[] populateRemoteRoleActors(DefaultRoleActors projectRoleActors, RemoteProjectRole projectRole)
    {
        List remoteRoleActors = new ArrayList();

        // Create a RemoteRoleActor for each RoleActor
        for (Iterator iterator = projectRoleActors.getRoleActors().iterator(); iterator.hasNext();)
        {
            RoleActor roleActor = (RoleActor) iterator.next();
            remoteRoleActors.add(new RemoteRoleActor(projectRole, roleActor.getDescriptor(), roleActor.getType(), roleActor.getParameter(), convertRealUsersToRemoteUsers(roleActor.getUsers())));
        }

        return (RemoteRoleActor[]) remoteRoleActors.toArray(new RemoteRoleActor[remoteRoleActors.size()]);
    }

    private RemoteUser[] convertRealUsersToRemoteUsers(Set<? extends User> users)
    {
        List<RemoteUser> remoteUsers = new ArrayList<RemoteUser>();
        for (final User user : users)
        {
            remoteUsers.add(remoteEntityFactory.createUser(user));
        }
        return remoteUsers.toArray(new RemoteUser[remoteUsers.size()]);
    }

    private ProjectRole getProjectRoleFromRemoteProjectRole(RemoteProjectRole projectRole) throws RemoteException
    {
        if (projectRole == null)
        {
            throw new RemoteException("RemoteProjectRole can not be null");
        }
        return new ProjectRoleImpl(projectRole.getId(), projectRole.getName(), projectRole.getDescription());
    }

    private Project getProjectFromRemoteProject(RemoteProject project) throws RemoteException
    {
        if (project == null)
        {
            throw new RemoteException("RemoteProject can not be null");
        }

        // Lookup the GV for the project so we can create the object
        try
        {
            GenericValue projectGV = projectManager.getProject(new Long(project.getId()));

            return projectFactory.getProject(projectGV);
        }
        catch (Exception e)
        {
            throw new RemoteValidationException("Could not resolve a project for the RemoteProject with id: " + project.getId());
        }
    }

    private void checkAndThrowErrors(String from, ErrorCollection errorCollection) throws RemoteException
    {
        if (errorCollection.hasAnyErrors())
        {
            throw new RemoteValidationException(from, errorCollection);
        }
    }

}
