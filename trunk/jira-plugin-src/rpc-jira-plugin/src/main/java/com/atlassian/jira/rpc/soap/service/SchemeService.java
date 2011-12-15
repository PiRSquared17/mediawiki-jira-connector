package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.soap.beans.RemoteEntity;
import com.atlassian.jira.rpc.soap.beans.RemotePermission;
import com.atlassian.jira.rpc.soap.beans.RemotePermissionScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteScheme;

/**
 * SchemeService delivers JIRA schemes.
 */
public interface SchemeService
{

    public RemoteScheme[] getNotificationSchemes(User admin) throws RemotePermissionException, RemoteException;

    public RemotePermissionScheme[] getPermissionSchemes(User admin) throws RemotePermissionException, RemoteException;

    public RemoteScheme[] getIssueSecuritySchemes(User admin) throws RemotePermissionException, RemoteException;

    public RemotePermission[] getAllPermissions(User admin) throws RemotePermissionException, RemoteException;

    public RemotePermissionScheme createPermissionScheme(User admin, String name, String description) throws RemotePermissionException, RemoteException, RemoteValidationException;

    public RemotePermissionScheme addPermissionTo(User admin, RemotePermissionScheme permissionScheme, RemotePermission remotePermission, RemoteEntity remoteEntity) throws RemotePermissionException, RemoteException, RemoteValidationException;

    public RemotePermissionScheme deletePermissionFrom(User admin, RemotePermissionScheme permissionSchemeName, RemotePermission remotePermission, RemoteEntity remoteEntity) throws RemotePermissionException, RemoteException, RemoteValidationException;

    public void deletePermissionScheme(User admin, String permissionSchemeName) throws RemotePermissionException, RemoteException, RemoteValidationException;
}
