package com.atlassian.jira.rpc.soap.service;

import com.atlassian.core.user.GroupUtils;
import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.soap.beans.RemoteEntity;
import com.atlassian.jira.rpc.soap.beans.RemoteGroup;
import com.atlassian.jira.rpc.soap.beans.RemotePermission;
import com.atlassian.jira.rpc.soap.beans.RemotePermissionScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;
import com.atlassian.jira.rpc.soap.util.RemoteEntityFactory;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.opensymphony.user.EntityNotFoundException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * SchemeServiceImpl
 */
public class SchemeServiceImpl implements SchemeService
{
    private final PermissionManager permissionManager;
    private final NotificationSchemeManager notificationSchemeManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final SchemePermissions schemePermissions;
    private final UserManager userManager;
    private final ServiceHelper serviceHelper;

    public SchemeServiceImpl(PermissionManager permissionManager,
            NotificationSchemeManager notificationSchemeManager,
            PermissionSchemeManager permissionSchemeManager,
            IssueSecuritySchemeManager issueSecuritySchemeManager,
            SchemePermissions schemePermissions,
            UserManager userManager,
            final RemoteEntityFactory remoteEntityFactory)
    {
        this.permissionManager = permissionManager;
        this.notificationSchemeManager = notificationSchemeManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.schemePermissions = schemePermissions;
        this.userManager = userManager;
        this.serviceHelper = new ServiceHelper(permissionManager, permissionSchemeManager, schemePermissions, remoteEntityFactory, userManager);
    }

    public RemoteScheme[] getNotificationSchemes(User user) throws RemotePermissionException, RemoteException
    {
        if (!(permissionManager.hasPermission(Permissions.ADMINISTER, user)))
        {
            throw new RemotePermissionException("You do not have permission to get NotificationSchemes.");
        }
        try
        {
            return this.populateRemoteSchemes(user, notificationSchemeManager.getSchemes(), "notification");
        }
        catch (GenericEntityException e)
        {
            throw new RemoteException(e);
        }
    }

    public RemotePermissionScheme[] getPermissionSchemes(User user) throws RemotePermissionException, RemoteException
    {
        if (!(permissionManager.hasPermission(Permissions.ADMINISTER, user)))
        {
            throw new RemotePermissionException("You do not have permission to get PermissionSchemes.");
        }
        try
        {
            return (RemotePermissionScheme[]) this.populateRemoteSchemes(user, permissionSchemeManager.getSchemes(), "permission");
        }
        catch (GenericEntityException e)
        {
            throw new RemoteException(e);
        }
    }

    public RemoteScheme[] getIssueSecuritySchemes(User user) throws RemotePermissionException, RemoteException
    {
        if (!(permissionManager.hasPermission(Permissions.ADMINISTER, user)))
        {
            throw new RemotePermissionException("You do not have permission to get SecuritySchemes.");
        }
        try
        {
            return this.populateRemoteSchemes(user, issueSecuritySchemeManager.getSchemes(), "issueSecurity");
        }
        catch (GenericEntityException e)
        {
            throw new RemoteException(e);
        }
    }

    public RemotePermissionScheme createPermissionScheme(User admin, String permissionSchemeName, String description) throws RemotePermissionException, RemoteException, RemoteValidationException
    {
        if (!(permissionManager.hasPermission(Permissions.ADMINISTER, admin)))
        {
            throw new RemotePermissionException("You do not have permission to create permission schemes");
        }

        //validate name
        if (permissionSchemeName == null)
        {
            throw new RemoteValidationException("unable to create permission scheme, name cannot be null");
        }
        else
        {
            try
            {
                GenericValue existingScheme = permissionSchemeManager.getScheme(permissionSchemeName);
                if (existingScheme != null)
                {
                    throw new RemoteValidationException("unable to create permission scheme, a scheme for this name already exists: " + permissionSchemeName);
                }
            }
            catch (GenericEntityException e)
            {
                //expected
            }
        }

        //create permission scheme
        GenericValue scheme = null;
        try
        {
            scheme = permissionSchemeManager.createScheme(permissionSchemeName, description);
        }
        catch (GenericEntityException e)
        {
            throw new RemoteException("unable to create permission scheme, cause: " + e.getMessage(), e);
        }
        return new RemotePermissionScheme(scheme);
    }

    public void deletePermissionScheme(User admin, String permissionSchemeName) throws RemotePermissionException, RemoteException, RemoteValidationException
    {
        if (!(permissionManager.hasPermission(Permissions.ADMINISTER, admin)))
        {
            throw new RemotePermissionException("You do not have permission to delete permission schemes");
        }

        GenericValue permissionScheme = null;

        //validate name
        if (permissionSchemeName == null)
        {
            throw new RemoteValidationException("unable to delete permission scheme, name cannot be null");
        }
        else
        {
            try
            {
                permissionScheme = permissionSchemeManager.getScheme(permissionSchemeName);
                if (permissionScheme == null)
                {
                    throw new RemoteValidationException("unable to delete permission scheme, a scheme for this name does not exist: " + permissionSchemeName);
                }
            }
            catch (GenericEntityException e)
            {
                throw new RemoteValidationException("unable to delete permission scheme, a scheme for this name does not exist: " + permissionSchemeName);
            }
        }

        try
        {
            //validate it's not the default scheme
            if (permissionSchemeManager.getDefaultScheme() != null &&
                permissionSchemeManager.getDefaultScheme().equals(permissionScheme))
            {
                throw new RemoteValidationException("you cannot delete the default permission scheme");
            }

            //If there are projects already attached then reattach to the default scheme
            List projects = permissionSchemeManager.getProjects(permissionScheme);
            for (int i = 0; i < projects.size(); i++)
            {
                GenericValue project = (GenericValue) projects.get(i);
                permissionSchemeManager.removeSchemesFromProject(project);
                permissionSchemeManager.addDefaultSchemeToProject(project);
            }
            permissionSchemeManager.deleteScheme(permissionScheme.getLong("id"));
        }
        catch (GenericEntityException e)
        {
            throw new RemoteException("unable to delete permission scheme, cause: " + e.getMessage(), e);
        }

    }

    public RemotePermissionScheme addPermissionTo(User admin, RemotePermissionScheme remotePermissionScheme, RemotePermission remotePermission, RemoteEntity remoteEntity) throws RemotePermissionException, RemoteException, RemoteValidationException
    {
        if (!(permissionManager.hasPermission(Permissions.ADMINISTER, admin)))
        {
            throw new RemotePermissionException("You do not have permission to add permissions to schemes");
        }

        this.validateRemotePermissionScheme(remotePermissionScheme);
        this.validateRemotePermission(remotePermission);

        //validate remoteEntity
        Group group = null;
        User user = null;
        if (remoteEntity == null)
        {
            throw new RemoteValidationException("unable to validate, remote entity cannot be null");
        }
        else
        {
            //one will be null
            user = this.validateRemoteUser(remoteEntity, user);
            group = this.validateRemoteGroup(remoteEntity, group);
        }

        //create permission for scheme
        try
        {
            GenericValue permissionScheme = permissionSchemeManager.getScheme(remotePermissionScheme.getId());
            String entityType = user == null ? "group" : "user";
            String entityName = user == null ? group.getName() : user.getName();

            //test permission doesn't already exist
            List permissionExists = permissionSchemeManager.getEntities(permissionScheme, remotePermission.getPermission(), entityType, entityName);
            if (!(permissionExists.size() > 0))
            {
                SchemeEntity schemeEntity = new SchemeEntity(entityType, entityName, remotePermission.getPermission());
                permissionSchemeManager.createSchemeEntity(permissionScheme, schemeEntity);
                return serviceHelper.populateSchemePermissions(admin, permissionScheme);
            }
            else
            {
                throw new RemoteValidationException("unable to create permission, permission for that entity already exists");
            }
        }
        catch (Exception e)
        {
            throw new RemoteException("unable to add permission to group, cause: " + e.getMessage(), e);
        }
    }

    public RemotePermissionScheme deletePermissionFrom(User admin, RemotePermissionScheme remotePermissionScheme, RemotePermission remotePermission, RemoteEntity remoteEntity) throws RemotePermissionException, RemoteException, RemoteValidationException
    {
        if (!(permissionManager.hasPermission(Permissions.ADMINISTER, admin)))
        {
            throw new RemotePermissionException("You do not have permission to delete permissions from schemes");
        }

        this.validateRemotePermissionScheme(remotePermissionScheme);
        this.validateRemotePermission(remotePermission);

        //validate remoteEntity
        Group group = null;
        User user = null;
        if (remoteEntity == null)
        {
            throw new RemoteValidationException("unable to validate, remote entity cannot be null");
        }
        else
        {
            //one will be null
            user = this.validateRemoteUser(remoteEntity, user);
            group = this.validateRemoteGroup(remoteEntity, group);
        }

        //delete permission from scheme
        try
        {
            GenericValue permissionScheme = permissionSchemeManager.getScheme(remotePermissionScheme.getId());
            String entityType = user == null ? "group" : "user";
            String entityName = user == null ? group.getName() : user.getName();

            //test permission exists
            List permissionExists = permissionSchemeManager.getEntities(permissionScheme, remotePermission.getPermission(), entityType, entityName);
            if (!(permissionExists.size() > 0))
            {
                throw new RemoteValidationException("unable to delete permission from scheme, no permission of this kind exists for this remote entity:"
                                                    + remotePermission + ", " + remoteEntity);
            }

            GenericValue permission = (GenericValue) permissionExists.get(0);
            permissionSchemeManager.deleteEntity(permission.getLong("id"));

            //update RemotePermissionScheme and return
            remotePermissionScheme = new RemotePermissionScheme(permissionScheme);
            return serviceHelper.populateSchemePermissions(admin, permissionScheme);
        }
        catch (Exception e)
        {
            throw new RemoteException("unable to delete permission from scheme, cause: " + e.getMessage(), e);
        }
    }

    protected Group validateRemoteGroup(RemoteEntity remoteEntity, Group group) throws RemoteValidationException
    {
        if (remoteEntity instanceof RemoteGroup)
        {
            group = GroupUtils.getGroup(((RemoteGroup) remoteEntity).getName());
            if (group == null)
            {
                throw new RemoteValidationException("unable to validate, group does not exist" + group);
            }
        }
        return group;
    }

    protected User validateRemoteUser(RemoteEntity remoteEntity, User user) throws RemoteValidationException
    {
        if (remoteEntity instanceof RemoteUser)
        {
            final RemoteUser remoteUser = (RemoteUser)remoteEntity;
            try
            {
                user = userManager.getUser(remoteUser.getName());
            }
            catch (Exception e)
            {
                throw new RemoteValidationException("unable to validate, user '" + remoteUser.getName() + "' does not exist: " + e.getMessage(), e);
            }
        }
        return user;
    }

    protected void validateRemotePermission(RemotePermission remotePermission) throws RemoteValidationException
    {
        //validate remotePermission
        if (remotePermission == null)
        {
            throw new RemoteValidationException("unable to validate, permission cannot be null");
        }
        else
        {
            String name = schemePermissions.getPermissionName(Integer.valueOf("" + remotePermission.getPermission()));
            if (name == null)
            {
                throw new RemoteValidationException("unable to validate, permission does not exist for: " + remotePermission.getName());
            }
        }
    }

    protected void validateRemotePermissionScheme(RemotePermissionScheme remotePermissionScheme) throws RemoteValidationException
    {
        //validate RemotePermissionScheme
        if (remotePermissionScheme == null)
        {
            throw new RemoteValidationException("unable to validate, remotePermissionScheme cannot be null");
        }
        else
        {
            try
            {
                GenericValue scheme = permissionSchemeManager.getScheme(remotePermissionScheme.getName());
                if (scheme == null)
                {
                    throw new RemoteValidationException("unable to validate, remotePermissionScheme does not exist for: " + scheme);
                }
            }
            catch (GenericEntityException e)
            {
                throw new RemoteValidationException("unable to validate, remotePermissionScheme does not exist for: " + e.getMessage(), e);
            }
        }
    }

    protected RemoteScheme[] populateRemoteSchemes(User user, List schemes, String type)
            throws RemotePermissionException, RemoteException, GenericEntityException
    {
        RemoteScheme[] remoteSchemes = null;
        if ("permission".equals(type))
        {
            remoteSchemes = new RemotePermissionScheme[schemes.size()];
            for (int i = 0; i < schemes.size(); i++)
            {
                try
                {
                    remoteSchemes[i] = serviceHelper.populateSchemePermissions(user, (GenericValue) schemes.get(i));
                }
                catch (EntityNotFoundException e)
                {
                    remoteSchemes = new RemotePermissionScheme[0];
                }
            }
        }
        else
        {
            remoteSchemes = new RemoteScheme[schemes.size()];
            for (int i = 0; i < schemes.size(); i++)
            {
                remoteSchemes[i] = new RemoteScheme((GenericValue) schemes.get(i), type);
            }
        }
        return remoteSchemes;
    }

    public RemotePermission[] getAllPermissions(User admin)
            throws RemotePermissionException, RemoteException
    {
        return serviceHelper.getAllPermissions(admin);
    }
}