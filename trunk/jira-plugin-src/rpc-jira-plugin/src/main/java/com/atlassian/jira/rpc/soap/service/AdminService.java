package com.atlassian.jira.rpc.soap.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.soap.beans.RemoteConfiguration;
import com.atlassian.jira.rpc.soap.beans.RemoteField;

public interface AdminService
{
    RemoteField[] getCustomFields(User user) throws RemotePermissionException;

    void refreshCustomFields(User user) throws RemotePermissionException;

    RemoteConfiguration getConfiguration(User user) throws RemotePermissionException;
}
