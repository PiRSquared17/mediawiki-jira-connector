package com.atlassian.jira.rpc.soap.service;

import com.atlassian.core.user.GroupUtils;
import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.soap.beans.RemoteEntity;
import com.atlassian.jira.rpc.soap.beans.RemotePermission;
import com.atlassian.jira.rpc.soap.beans.RemotePermissionScheme;
import com.atlassian.jira.rpc.soap.util.RemoteEntityFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.Group;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

/**
 * A simple helper class that SchemeService and ProejctService can use to populate a RemotePermissionScheme.
 */
public class ServiceHelper
{
    private static final Logger log = Logger.getLogger(ServiceHelper.class);
    
    private PermissionManager permissionManager;
    private PermissionSchemeManager permissionSchemeManager;
    private final SchemePermissions schemePermissions;
    private final RemoteEntityFactory remoteEntityFactory;
    private final UserManager userManager;

    public ServiceHelper(PermissionManager permissionManager,
            PermissionSchemeManager permissionSchemeManager,
            SchemePermissions schemePermissions,
            final RemoteEntityFactory remoteEntityFactory,
            UserManager userManager)
    {
        this.permissionManager = permissionManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.schemePermissions = schemePermissions;
        this.remoteEntityFactory = remoteEntityFactory;
        this.userManager = userManager;
    }

    protected RemotePermissionScheme populateSchemePermissions(User admin, GenericValue permissionScheme) throws RemoteException, GenericEntityException, EntityNotFoundException
    {
        RemotePermissionScheme remotePermissionScheme;
        //iterate over all permissions to create return value
        RemotePermission[] allPermissions = this.getAllPermissions(admin);
        remotePermissionScheme = new RemotePermissionScheme(permissionScheme);
        for (int i = 0; i < allPermissions.length; i++)
        {
            RemotePermission remotePermission = allPermissions[i];
            List entityMappings = permissionSchemeManager.getEntities(permissionScheme, remotePermission.getPermission());
            this.populatePermissionEntityMappings(entityMappings, remotePermissionScheme, remotePermission);
        }
        return remotePermissionScheme;
    }

    protected void populatePermissionEntityMappings(List entityMappings, RemotePermissionScheme remotePermissionScheme, RemotePermission permission) throws EntityNotFoundException
    {
        //add entity mappings for this permission if there are.
        if (entityMappings.size() > 0)
        {
            RemoteEntity[] remoteEntities = new RemoteEntity[entityMappings.size()];
            for (int j = 0; j < remoteEntities.length; j++)
            {
                GenericValue entityMapping = (GenericValue) entityMappings.get(j);
                if ("group".equals(entityMapping.getString("type")))
                {
                    Group group = GroupUtils.getGroup(entityMapping.getString("parameter"));
                    if (group != null)
                    {
                        remoteEntities[j] = remoteEntityFactory.createGroup(group);
                    }
                    else
                    {
                        remoteEntities[j] = null;
                        log.info("Group permission mapping for: " + permission.getName() + " is allowed for Anyone which is represented by a null group.");
                    }
                }
                if ("user".equals(entityMapping.getString("type")))
                {
                    User user = userManager.getUser(entityMapping.getString("parameter"));
                    if (user != null)
                    {
                        remoteEntities[j] = remoteEntityFactory.createUser(user);
                    }
                    else
                    {
                        remoteEntities[j] = null;
                        log.info("User permission mapping for: " + permission.getName() + " is being represented by a null User.");
                    }
                }
            }
            remotePermissionScheme.addPermissionMapping(permission, remoteEntities);
        }
    }

    public RemotePermission[] getAllPermissions(User admin) throws RemotePermissionException, RemoteException
    {
        if (!(permissionManager.hasPermission(Permissions.ADMINISTER, admin)))
        {
            throw new RemotePermissionException("You do not have permission to get permissions.");
        }

        Map permissions = schemePermissions.getSchemePermissions();
        int size = permissions.size();
        Object[] keys = permissions.keySet().toArray();
        Object[] values = permissions.values().toArray();
        RemotePermission[] remotePermissions = new RemotePermission[size];
        for (int i = 0; i < size; i++)
        {
            remotePermissions[i] = new RemotePermission(Long.valueOf("" + keys[i]), ((Permission) values[i]).getName());
        }

        return remotePermissions;
    }

}
