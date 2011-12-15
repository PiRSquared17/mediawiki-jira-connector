package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.soap.beans.RemoteConfiguration;
import com.atlassian.jira.rpc.soap.beans.RemoteField;
import com.atlassian.jira.rpc.soap.util.SoapUtilsBean;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import java.util.List;

public class AdminServiceImpl implements AdminService
{
    private final CustomFieldManager customFieldManager;
    private final SoapUtilsBean soapUtilsBean;
    private final PermissionManager permissionManager;
    private final ApplicationProperties applicationProperties;

    public AdminServiceImpl(CustomFieldManager customFieldManager, SoapUtilsBean soapUtilsBean, PermissionManager permissionManager, ApplicationProperties applicationProperties)
    {
        this.customFieldManager = customFieldManager;
        this.soapUtilsBean = soapUtilsBean;
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
    }

    public RemoteField[] getCustomFields(User user) throws RemotePermissionException
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            throw new RemotePermissionException("Remote custom fields can only be retrieved by an administrator.");
        }

        final List customFields = customFieldManager.getCustomFieldObjects();
        return soapUtilsBean.convertFieldsToRemoteFields(customFields);
    }

    public void refreshCustomFields(User user) throws RemotePermissionException
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            throw new RemotePermissionException("Custom fields reset can only be performed by an administrator.");
        }

        customFieldManager.refresh();
    }

    public RemoteConfiguration getConfiguration(User user) throws RemotePermissionException
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            throw new RemotePermissionException("Configuration properties can only be retrieved by an administrator.");
        }
        return new RemoteConfiguration(applicationProperties);
    }
}
