package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import com.atlassian.jira.rpc.soap.beans.RemoteProjectRole;
import com.atlassian.jira.rpc.soap.beans.RemoteProjectRoleActors;
import com.atlassian.jira.rpc.soap.beans.RemoteRoleActors;
import com.atlassian.jira.rpc.soap.beans.RemoteScheme;

/**
 * Must be used to access all ProjectRole functionality.
 */
public interface ProjectRoleService
{
    /**
     * Get all the ProjectRoles available in JIRA. Currently this list is global.
     *
     * @return The global list of project roles in JIRA
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public RemoteProjectRole[] getProjectRoles(User currentUser) throws RemoteException;

    /**
     * Will return the project role based off the passed in <code>id</code>, and checking the <code>currentUser</code>
     * has the correct permissions to perform the operation.
     *
     * @param currentUser
     * @param id
     * @return the ProjectRole for the given id
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public RemoteProjectRole getProjectRole(User currentUser, Long id) throws RemoteException;

    /**
     * Will create the project role based off the passed in <code>projectRole.getName()</code>,
     * <code>projectRole.getDescription()</code> and checking the <code>currentUser</code> has the correct permissions
     * to perform the create operation.
     *
     * @param currentUser
     * @param projectRole can not be null and will contain the name and description for the project role to create
     * @return the ProjectRole object that was created
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public RemoteProjectRole createProjectRole(User currentUser, RemoteProjectRole projectRole) throws RemoteException;

    /**
     * Will tell you if a role name exists or not.
     *
     * @param name the name of the project role to check
     * @return true if unique, false if one already exists with that name
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public boolean isProjectRoleNameUnique(User currentUser, String name) throws RemoteException;

    /**
     * Will delete the project role based off the passed in <code>projectRole</code> and checking the
     * <code>currentUser</code> has the correct permissions to perform the delete operation. This will also delete all
     * ProjectRoleActor associations that it is the parent of. If the confirm flag is false then this method will
     * check to see if the role is associated with any permission or notification schemes and if so it will throw
     * an exception informing you which schemes and it will not perform the delete. To force a delete pass the confirm
     * flag in as true.
     *
     * @param currentUser
     * @param projectRole
     * @param confirm
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     * @throws RemoteException thrown if the confirm flag is false and the role is used by a notification or
     * permission scheme.
     */
    public void deleteProjectRole(User currentUser, RemoteProjectRole projectRole, boolean confirm) throws RemoteException;

    /**
     * Will add project role actor associations based off the passed in <code>actors</code> and checking the
     * <code>currentUser</code> has the correct permissions to perform the update operation.
     *
     * @param currentUser
     * @param actors      is a list of strings that they RoleActor impl should be able to handle
     * @param projectRole is the project role to associate with
     * @param project     is the project to associate with
     * @param actorType   is a type that defines the type of role actor to instantiate (ex./ UserRoleActor.TYPE ("atlassian-user-role-actor"),
     *                    GroupRoleActor.TYPE ("atlassian-group-role-actor"))
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public void addActorsToProjectRole(User currentUser, String[] actors, RemoteProjectRole projectRole, RemoteProject project, String actorType) throws RemoteException;

    /**
     * Will remove project role actor associations based off the passed in <code>actors</code> and checking the
     * <code>currentUser</code> has the correct permissions to perform the update operation.
     *
     * @param currentUser
     * @param actors      is a list of strings that the RoleActor impl should be able to handle
     * @param projectRole is the project role to associate with
     * @param project     is the project to associate with
     * @param actorType   is a type that defines the type of role actor to instantiate (ex./ UserRoleActor.TYPE ("atlassian-user-role-actor"),
     *                    GroupRoleActor.TYPE ("atlassian-group-role-actor"))
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public void removeActorsFromProjectRole(User currentUser, String[] actors, RemoteProjectRole projectRole, RemoteProject project, String actorType) throws RemoteException;

    /**
     * Will update the project role based off the passed in <code>projectRole</code> and checking the
     * <code>currentUser</code> has the correct permissions to perform the update operation.
     *
     * @param currentUser
     * @param projectRole
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public void updateProjectRole(User currentUser, RemoteProjectRole projectRole) throws RemoteException;

    /**
     * Will return the project role actors based off the passed in <code>projectRole</code> and <code>project</code>
     * checking the <code>currentUser</code> has the correct permissions to perform the get operation.
     *
     * @param currentUser
     * @param projectRole
     * @param project
     * @return the ProjectRoleActor representing the projectRole and project
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public RemoteProjectRoleActors getProjectRoleActors(User currentUser, RemoteProjectRole projectRole, RemoteProject project) throws RemoteException;

    /**
     * Will return the project role actors based off the passed in <code>projectRole</code> checking the
     * <code>currentUser</code> has the correct permissions to perform the get operation.
     *
     * @param currentUser
     * @param projectRole
     * @return the remote default role actors
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public RemoteRoleActors getDefaultRoleActors(User currentUser, RemoteProjectRole projectRole) throws RemoteException;

    /**
     * Will add default role actor associations based off the passed in <code>actors</code> and checking the
     * <code>currentUser</code> has the correct permissions to perform the update operation.
     *
     * @param currentUser
     * @param actors      is a list of strings that the RoleActor impl should be able to handle
     * @param projectRole is the project role to associate with
     * @param type        is a type that defines the type of role actor to instantiate (ex./ UserRoleActor.TYPE,
     *                    GroupRoleActor.TYPE)
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public void addDefaultActorsToProjectRole(User currentUser, String[] actors, RemoteProjectRole projectRole, String type) throws RemoteException;

    /**
     * Will remove default role actor associations based off the passed in <code>actors</code> and checking the
     * <code>currentUser</code> has the correct permissions to perform the update operation.
     *
     * @param currentUser
     * @param actors      is a list of strings that they RoleActor impl should be able to handle
     * @param projectRole is the project role to associate with
     * @param actorType   is a type that defines the type of role actor to instantiate (ex./ UserRoleActor.TYPE,
     *                    GroupRoleActor.TYPE)
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public void removeDefaultActorsFromProjectRole(User currentUser, String[] actors, RemoteProjectRole projectRole, String actorType) throws RemoteException;

    /**
     * Will remove all role actors with the specified name and the specified type. This method should be used to clean
     * up after the actual subject of the role actor has been deleted (ex. deleting a user from the system).
     *
     * @param currentUser
     * @param name        this is the name that the role actor is stored under (ex. username of 'admin', group name of
     *                    'jira-users')
     * @param type        this is the role type parameter, (ex. GroupRoleActor.TYPE, UserRoleActor.TYPE)
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public void removeAllRoleActorsByNameAndType(User currentUser, String name, String type) throws RemoteException;

    /**
     * Will remove all role actors associated with the specified project. This method should be used to clean up just
     * before the actual project has been deleted (ex. deleting a project from the system).
     *
     * @param currentUser
     * @param project
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public void removeAllRoleActorsByProject(User currentUser, RemoteProject project) throws RemoteException;

    /**
     * Will get all notification scheme's that the specified projectRole is currently used in.
     *
     * @param currentUser
     * @param projectRole
     * @return a collection of schemes
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public RemoteScheme[] getAssociatedNotificationSchemes(User currentUser, RemoteProjectRole projectRole) throws RemoteException;

    /**
     * Will get all permission scheme's that the specified projectRole is currently used in.
     *
     * @param currentUser
     * @param projectRole
     * @return a collection of schemes
     * @throws com.atlassian.jira.rpc.exception.RemoteValidationException thrown if
     * and invalid parameter is passed or if the currentUser does not have permission
     * to perform the operation.
     */
    public RemoteScheme[] getAssociatedPermissionSchemes(User currentUser, RemoteProjectRole projectRole) throws RemoteException;

}
