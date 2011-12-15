package com.atlassian.jira.rpc.soap.beans;

import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RemotePermissionScheme maps permissions against an array of RemoteGroups
 */
public class RemotePermissionScheme extends RemoteScheme
{
    protected RemotePermissionMapping[] permissionMappings;

    public RemotePermissionScheme()
    {
        permissionMappings = new RemotePermissionMapping[0];
    }

    public RemotePermissionScheme(GenericValue scheme)
    {
        this.id = scheme.getLong("id");
        this.name = scheme.getString("name");
        this.description = scheme.getString("description");
        this.type = "permission";
        this.permissionMappings = new RemotePermissionMapping[0];
    }

    public void addPermissionMapping(RemotePermission remotePermission, RemoteEntity[] remoteEntities)
    {
        List mappingsList = null;
        if (permissionMappings.length > 0)
        {
            mappingsList = new ArrayList(Arrays.asList(permissionMappings));
        }
        else
        {
            mappingsList = new ArrayList();
        }
        mappingsList.add(new RemotePermissionMapping(remotePermission, remoteEntities));
        this.permissionMappings = (RemotePermissionMapping[]) (mappingsList.toArray(permissionMappings));
    }

    public void removePermissionMapping(RemotePermissionMapping remotePermissionMapping)
    {
        List mappingsList = new ArrayList(Arrays.asList(permissionMappings));
        mappingsList.remove(remotePermissionMapping);
        this.permissionMappings = (RemotePermissionMapping[]) (mappingsList.toArray(permissionMappings));
    }

    public RemotePermissionMapping[] getPermissionMappings()
    {
        return permissionMappings;
    }

    public void setPermissionMappings(RemotePermissionMapping[] permissionMappings)
    {
        this.permissionMappings = permissionMappings;
    }
}
