package com.atlassian.jira.rpc.soap.service;

import com.atlassian.core.user.GroupUtils;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.group.GroupRemoveUserMapper;
import com.atlassian.jira.bc.group.GroupService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.soap.beans.RemoteFilter;
import com.atlassian.jira.rpc.soap.beans.RemoteGroup;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;
import com.atlassian.jira.rpc.soap.util.RemoteEntityFactory;
import com.atlassian.jira.rpc.soap.util.SoapUtils;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;

public class UserServiceImpl implements UserService
{
    private PermissionManager permissionManager;
    private SearchRequestService searchRequestService;
    private final UserManager userManager;
    private final GroupManager groupManager;
    private ApplicationProperties applicationProperties;
    private final GroupService groupService;
    private com.atlassian.jira.bc.user.UserService userService;
    private final RemoteEntityFactory remoteEntityFactory;
    private final CrowdService crowdService;
    private final GlobalPermissionManager globalPermissionManager;
    private final UserUtil userUtil;


    public UserServiceImpl(SearchRequestService searchRequestService,
                           UserManager userManager,
                           GroupManager groupManager,
                           PermissionManager permissionManager,
                           ApplicationProperties applicationProperties,
                           com.atlassian.jira.bc.user.UserService userService,
                           GroupService groupService,
                           final RemoteEntityFactory remoteEntityFactory, CrowdService crowdService, GlobalPermissionManager globalPermissionManager, UserUtil userUtil)
    {
        this.searchRequestService = searchRequestService;
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
        this.userService = userService;
        this.groupService = groupService;
        this.remoteEntityFactory = remoteEntityFactory;
        this.crowdService = crowdService;
        this.globalPermissionManager = globalPermissionManager;
        this.userUtil = userUtil;
    }

    public RemoteUser getUser(final User currentUser, String username)
    {
        final User foundUser = userManager.getUserObject(username);
        if (foundUser == null)
        {
            return null;
        }
        return remoteEntityFactory.createUser(foundUser);
    }

    public RemoteUser createUser(User remoteUser, String username, String password, String fullName, String email)
            throws RemoteValidationException, RemotePermissionException
    {
        if (username != null)
        {
            username = username.trim();
        }

        final com.atlassian.jira.bc.user.UserService.CreateUserValidationResult result =
                userService.validateCreateUserForAdminPasswordRequired(remoteUser, username, password, password, email, fullName);

        if (!result.isValid())
        {
            throw new RemoteValidationException("Error creating user", result.getErrorCollection());
        }

        try
        {
            User user = userService.createUserWithNotification(result);

            return remoteEntityFactory.createUser(user);
        } catch (PermissionException e)
        {
            throw new RemoteValidationException("cannot create user details, cause: " + e.getMessage(), e);
        } catch (CreateException e)
        {
            throw new RemoteValidationException("cannot create user details, cause: " + e.getMessage(), e);
        }
    }


    public RemoteUser updateUser(User admin, RemoteUser remoteUser) throws RemoteValidationException, RemoteException
    {
        final User updatedUser = canUpdateUser(admin, remoteUser);
        final User updatedUserDetails = updatedTheUserDetails(updatedUser, remoteUser);
        return getUser(admin, updatedUserDetails.getName());
    }

    public RemoteUser setUserPassword(User admin, RemoteUser remoteUser, String newPassword) throws RemoteValidationException, RemoteException
    {
        final User updatedUser = canUpdateUser(admin, remoteUser);
        updatedTheUserPassword(updatedUser, newPassword);
        return getUser(admin, updatedUser.getName());
    }


    private User canUpdateUser(User admin, RemoteUser remoteUser) throws RemoteValidationException
    {
        if (admin != null && admin.getName().equals(remoteUser.getName()))
        {
            // we allow people to update themselves via SOAP always
            return admin;
        }
        if (!isAdministrator(admin) && !isSystemAdministrator(admin))
        {
            throw new RemoteValidationException(
                    format
                            (
                                    "The user: %s does not have permission to update the user: %s",
                                    admin.getName(), remoteUser.getName()
                            )
            );
        }
        User updatedUser = userManager.getUserObject(remoteUser.getName());
        if (updatedUser == null)
        {
            throw new RemoteValidationException
                    (
                            format("No user could be found with the name: %s", remoteUser.getName())
                    );
        }
        if (!isRemoteUserPermittedToEditSelectedUser(admin, updatedUser))
        {
            throw new RemoteValidationException(
                    format
                            (
                                    "The user: %s does not have permission to update the user: %s",
                                    admin.getName(), remoteUser.getName()
                            )
            );
        }
        if (!userManager.canUpdateUser(updatedUser))
        {
            throw new RemoteValidationException
                    (
                            format("The user: %s is in a read-only directory", remoteUser.getName())
                    );
        }
        return updatedUser;
    }

    private User updatedTheUserDetails(User updatedUser, RemoteUser remoteUser) throws RemoteException
    {
        UserTemplate user = new UserTemplate(updatedUser);
        user.setDisplayName(remoteUser.getFullname());
        user.setEmailAddress(remoteUser.getEmail());

        try
        {
            return crowdService.updateUser(user);
        } catch (OperationNotPermittedException e)
        {
            throw new RemoteException(e.toString());
        } catch (OperationFailedException e)
        {
            throw new RemoteException(e.toString());
        } catch (InvalidUserException e)
        {
            throw new RemoteException(e.toString());
        }
    }


    private void updatedTheUserPassword(User updatedUser, String newPassword) throws RemoteException
    {
        try
        {
            userUtil.changePassword(updatedUser, newPassword);
        } catch (UserNotFoundException e)
        {
            throw new RemoteException(e.toString());
        } catch (InvalidCredentialException e)
        {
            throw new RemoteException(e.toString());
        } catch (OperationNotPermittedException e)
        {
            throw new RemoteException(e.toString());
        } catch (PermissionException e)
        {
            throw new RemoteException(e.toString());
        }
    }

    public boolean isRemoteUserPermittedToEditSelectedUser(User admin, User updatedUser)
    {
        return (isSystemAdministrator(admin) || !isSystemAdministrator(updatedUser));
    }

    private boolean isSystemAdministrator(User admin)
    {
        return globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, admin);
    }

    private boolean isAdministrator(User admin)
    {
        return globalPermissionManager.hasPermission(Permissions.ADMINISTER, admin);
    }

    public void deleteUser(User remoteUser, String username) throws RemoteException
    {
        final com.atlassian.jira.bc.user.UserService.DeleteUserValidationResult result =
                userService.validateDeleteUser(remoteUser, username);

        if (!result.isValid())
        {
            throw new RemoteValidationException("Error removing user", result.getErrorCollection());
        }

        try
        {
            userService.removeUser(remoteUser, result);
        } catch (Exception e)
        {
            throw new RemoteException("Unable to delete user, cause: " + e.getMessage(), e);
        }
    }


    public RemoteGroup getGroup(User admin, String groupName) throws RemoteException
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, admin))
        {
            throw new RemotePermissionException("You do not have permission to get a group.");
        }
        if (groupName == null)
        {
            throw new RemoteValidationException("group name cannot be null, needs a value");
        }

        Group group = GroupUtils.getGroup(groupName);
        if (group == null)
        {
            throw new RemoteValidationException("no group found for that groupName: " + groupName);
        }

        return remoteEntityFactory.createGroup(group);
    }

    public RemoteGroup createGroup(User admin, String groupName, RemoteUser firstUser) throws RemoteException
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, admin))
        {
            throw new RemotePermissionException("You do not have permission to create a group.");
        }

        if (applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT))
        {
            throw new RemoteValidationException("Cannot create group, as external user management is enabled. Contact your administrator.");
        }

        if (groupName == null)
        {
            throw new RemoteValidationException("group cannot be created, group name cannot be null, needs a value");
        }

        User osFirstUser = null;
        if (firstUser != null)
        {
            osFirstUser = userManager.getUserObject(firstUser.getName());
            if (osFirstUser == null)
            {
                throw new RemoteValidationException("group cannot be created, first user '" + firstUser.getName() + "' for this group doesn't exist");
            }
        }

        Group group;
        try
        {
            group = groupManager.createGroup(groupName);
            if (osFirstUser != null)
            {
                groupManager.addUserToGroup(osFirstUser, group);
            }
        } catch (Exception e)
        {
            throw new RemoteValidationException("Group '" + groupName + "' cannot be created. " + e.getMessage(), e);
        }

        return remoteEntityFactory.createGroup(group);
    }

    public void addUserToGroup(User admin, RemoteGroup remoteGroup, RemoteUser remoteUser) throws RemoteException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        List<String> groups = EasyList.build(remoteGroup.getName());
        JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(admin, errorCollection);
        if (groupService.validateAddUserToGroup(jiraServiceContext, groups, remoteUser.getName()))
        {
            if (groupService.addUsersToGroups(jiraServiceContext, groups, EasyList.build(remoteUser.getName())))
            {
                return;
            }
        }
        throw new RemoteValidationException("Can not add user '" + remoteUser.getName() + "' to group '"
                                            + remoteGroup.getName() + "'.", errorCollection);
    }

    public void removeUserFromGroup(User admin, RemoteGroup remoteGroup, RemoteUser remoteUser) throws RemoteException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        List groups = EasyList.build(remoteGroup.getName());
        JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(admin, errorCollection);
        if (groupService.validateRemoveUserFromGroups(jiraServiceContext, groups, remoteUser.getName()))
        {
            GroupRemoveUserMapper groupRemoveUserMapper = new GroupRemoveUserMapper();
            groupRemoveUserMapper.register(remoteUser.getName(), remoteGroup.getName());
            if (groupService.removeUsersFromGroups(jiraServiceContext, groupRemoveUserMapper))
            {
                return;
            }
        }
        throw new RemoteValidationException("Can not remove user '" + remoteUser.getName() + "' from group '"
                + remoteGroup.getName() + "'.", errorCollection);
    }

    public RemoteGroup updateGroup(User admin, RemoteGroup remoteGroup) throws RemoteException
    {

        ErrorCollection errorCollection = new SimpleErrorCollection();
        JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(admin, errorCollection);

        Group group = getGroup(remoteGroup);

        // Find the users we want to add to the group
        Collection<String> userNamesToAdd = getUsersToAddToGroup(remoteGroup, group);

        // NOTE: we have to do the add operations before we try to do the removes since you may be adding yourself to
        // an admin group and then trying to remove yourself from a different admin group. The remove validation will
        // fail if you have not first been added to a different admin group
        addUsersToGroup(remoteGroup, jiraServiceContext, userNamesToAdd, errorCollection);

        removeUsersFromGroup(group, remoteGroup, jiraServiceContext, errorCollection);

        return remoteEntityFactory.createGroup(group);
    }

    private void removeUsersFromGroup(Group group, RemoteGroup remoteGroup, JiraServiceContextImpl jiraServiceContext, ErrorCollection errorCollection)
            throws RemoteValidationException
    {
        // Setup the params so that we can validate the users to be removed from the group
        GroupRemoveUserMapper groupRemoveUserMapper = getRemoveUserMapper(group, remoteGroup);

        if (groupService.validateRemoveUsersFromGroups(jiraServiceContext, groupRemoveUserMapper))
        {
            if (groupService.removeUsersFromGroups(jiraServiceContext, groupRemoveUserMapper))
            {
                return;
            }
        }
        throw new RemoteValidationException("Errors during update group. Error with removing users from the group.", errorCollection);
    }

    private void addUsersToGroup(RemoteGroup remoteGroup, JiraServiceContextImpl jiraServiceContext, Collection<String> userNamesToAdd, ErrorCollection errorCollection)
            throws RemoteValidationException
    {
        List<String> groupsToJoin = EasyList.build(remoteGroup.getName());
        if (groupService.validateAddUsersToGroup(jiraServiceContext, groupsToJoin, userNamesToAdd).isSuccess())
        {
            groupService.addUsersToGroups(jiraServiceContext, groupsToJoin, userNamesToAdd);
        }

        if (errorCollection.hasAnyErrors())
        {
            throw new RemoteValidationException("Errors during update group. Error with adding users to the group.", errorCollection);
        }
    }

    private GroupRemoveUserMapper getRemoveUserMapper(Group group, RemoteGroup remoteGroup)
    {
        GroupRemoveUserMapper groupRemoveUserMapper = new GroupRemoveUserMapper(EasyList.build(remoteGroup.getName()));
        final Collection<com.atlassian.crowd.embedded.api.User> users = groupManager.getUsersInGroup(group.getName());
        for (com.atlassian.crowd.embedded.api.User user : users)
        {
            String userName = user.getName();
            if (!groupContainsUserWithUsername(userName, remoteGroup))
            {
                groupRemoveUserMapper.register(userName);
            }
        }
        return groupRemoveUserMapper;
    }

    private Collection<String> getUsersToAddToGroup(RemoteGroup remoteGroup, Group group)
    {
        Collection<String> userNamesToAdd = new ArrayList<String>();
        for (int i = 0; i < remoteGroup.getUsers().length; i++)
        {
            RemoteUser remoteUser = remoteGroup.getUsers()[i];
            if (remoteUser != null)
            {
                User user = userManager.getUserObject(remoteUser.getName());
                // Old Group.contains(user) would return false for null user
                if (user == null || !groupManager.isUserInGroup(user, group))
                {
                    userNamesToAdd.add(remoteUser.getName());
                }
            }
        }
        return userNamesToAdd;
    }

    private boolean groupContainsUserWithUsername(String username, RemoteGroup group)
    {
        for (int i = 0; i < group.getUsers().length; i++)
        {
            RemoteUser remoteUser = group.getUsers()[i];
            if (remoteUser.getName() != null && remoteUser.getName().equals(username))
            {
                return true;
            }
        }
        return false;
    }

    private Group getGroup(RemoteGroup remoteGroup) throws RemotePermissionException, RemoteValidationException
    {
        Group group = GroupUtils.getGroup(remoteGroup.getName());
        if (group == null)
        {
            throw new RemoteValidationException("group cannot be updated, because it doesn't exist.");
        }
        return group;
    }

    public void deleteGroup(User admin, String groupName, String swapGroupName) throws RemoteException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(admin, errorCollection);
        if (!groupService.validateDelete(jiraServiceContext, groupName, swapGroupName))
        {
            throw new RemoteValidationException("Error validating group deletion.", errorCollection);
        }

        if (!groupService.delete(jiraServiceContext, groupName, swapGroupName))
        {
            throw new RemoteValidationException("Error deleting group.", errorCollection);
        }
    }

    public RemoteFilter[] getFavouriteFilters(User user) throws RemoteException
    {
        return SoapUtils.getFilters(searchRequestService.getFavouriteFilters(user));
    }
}
