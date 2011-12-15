/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.rpc.xmlrpc;

import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.rpc.RpcUtils;
import com.atlassian.jira.rpc.exception.RemoteAuthenticationException;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.soap.JiraSoapService;
import com.atlassian.jira.rpc.soap.beans.RemoteComment;
import com.atlassian.jira.rpc.soap.beans.RemoteComponent;
import com.atlassian.jira.rpc.soap.beans.RemoteCustomFieldValue;
import com.atlassian.jira.rpc.soap.beans.RemoteFieldValue;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.atlassian.jira.rpc.soap.beans.RemoteVersion;
import com.atlassian.plugin.PluginAccessor;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class JiraXmlRpcService implements XmlRpcService
{
    JiraSoapService soapService;

    public JiraXmlRpcService(PluginAccessor pluginAccessor)
    {
        this.soapService = ((JiraSoapService) pluginAccessor.getPluginModule("com.atlassian.jira.ext.rpc:soap").getModule());
    }

    public String login(String username, String password) throws RemoteException
    {
        return soapService.login(username, password);
    }

    public boolean logout(String token)
    {
        return soapService.logout(token);
    }

    public Hashtable getServerInfo(String token)
    {
        return RpcUtils.makeStruct(soapService.getServerInfo(token));
    }

    public Vector getProjectsNoSchemes(String token) throws RemoteException
    {
        return RpcUtils.makeVector(soapService.getProjectsNoSchemes(token));
    }

    public Vector getVersions(String token, String projectKey) throws RemoteException
    {
        return RpcUtils.makeVector(soapService.getVersions(token, projectKey));
    }

    public Vector getComponents(String token, String projectKey) throws RemoteException
    {
        return RpcUtils.makeVector(soapService.getComponents(token, projectKey));
    }

    public Vector getIssueTypesForProject(String token, String projectId) throws RemoteException
    {
        return RpcUtils.makeVector(soapService.getIssueTypesForProject(token, projectId));
    }

    public Vector getSubTaskIssueTypesForProject(String token, String projectId) throws RemoteException
    {
        return RpcUtils.makeVector(soapService.getSubTaskIssueTypesForProject(token, projectId));
    }

    public Vector getIssueTypes(String token) throws RemotePermissionException, RemoteAuthenticationException
    {
        return RpcUtils.makeVector(soapService.getIssueTypes(token));
    }

    public Vector getSubTaskIssueTypes(String token) throws Exception
    {
        return RpcUtils.makeVector(soapService.getSubTaskIssueTypes(token));
    }

    public Vector getPriorities(String token) throws RemotePermissionException, RemoteAuthenticationException
    {
        return RpcUtils.makeVector(soapService.getPriorities(token));
    }

    public Vector getStatuses(String token) throws RemotePermissionException, RemoteAuthenticationException
    {
        return RpcUtils.makeVector(soapService.getStatuses(token));
    }

    public Vector getResolutions(String token) throws RemotePermissionException, RemoteAuthenticationException
    {
        return RpcUtils.makeVector(soapService.getResolutions(token));
    }

    public Hashtable getUser(String token, String username)
            throws RemotePermissionException, RemoteAuthenticationException
    {
        return RpcUtils.makeStruct(soapService.getUser(token, username));
    }

    public Vector getSavedFilters(String token) throws RemoteException
    {
        return getFavouriteFilters(token);
    }

    public Vector getFavouriteFilters(String token) throws RemoteException
    {
        return RpcUtils.makeVector(soapService.getFavouriteFilters(token));
    }

    public Hashtable getIssue(String token, String issueKey) throws RemoteException
    {
        return makeIssueStruct(soapService.getIssue(token, issueKey));
    }

    public Vector getComments(String token, String issueKey) throws RemoteException
    {
        return RpcUtils.makeVector(soapService.getComments(token, issueKey));
    }

    public Hashtable createIssue(String token, Hashtable rIssueStruct) throws RemoteException
    {
        return makeIssueStruct(soapService.createIssue(token, makeRemoteIssue(rIssueStruct)));
    }

    public Hashtable updateIssue(String token, String issueKey, Hashtable rIssueStruct) throws RemoteException
    {
        return makeIssueStruct(soapService.updateIssue(token, issueKey, makeRemoteFieldValue(rIssueStruct)));
    }

    public boolean addComment(String token, String issueKey, String comment) throws Exception
    {
        soapService.addComment(token, issueKey, new RemoteComment(comment));
        return true;
    }

    public Vector getIssuesFromFilter(String token, String filterId) throws RemoteException
    {
        RemoteIssue[] issuesFromFilter = soapService.getIssuesFromFilter(token, filterId);
        return convertIssuesToVector(issuesFromFilter);
    }

    public Vector getIssuesFromTextSearch(String token, String searchTerms) throws Exception
    {
        RemoteIssue[] issuesFromFilter = soapService.getIssuesFromTextSearch(token, searchTerms);
        return convertIssuesToVector(issuesFromFilter);
    }

    public Vector getIssuesFromTextSearchWithProject(String token, Vector projectKeys, String searchTerms, int maxNumResults)
            throws Exception
    {
        try
        {
            String[] keys = (String[]) projectKeys.toArray(new String[projectKeys.size()]);
            RemoteIssue[] issuesFromFilter = soapService.getIssuesFromTextSearchWithProject(token, keys, searchTerms, maxNumResults);
            return convertIssuesToVector(issuesFromFilter);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    private Vector convertIssuesToVector(RemoteIssue[] issuesFromFilter)
    {
        if (issuesFromFilter != null)
        {
            Vector returnedIsssue = new Vector(issuesFromFilter.length);
            for (int i = 0; i < issuesFromFilter.length; i++)
            {
                RemoteIssue remoteIssue = issuesFromFilter[i];
                try
                {
                    returnedIsssue.add(makeIssueStruct(remoteIssue));
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }

            return returnedIsssue;
        }
        else
        {
            return new Vector();
        }
    }

    private RemoteFieldValue[] makeRemoteFieldValue(Hashtable ht)
    {
        if (ht != null)
        {
            RemoteFieldValue[] fieldValues = new RemoteFieldValue[ht.size()];
            Set entries = ht.entrySet();
            int i = 0;
            for (Iterator iterator = entries.iterator(); iterator.hasNext();)
            {
                Map.Entry entry = (Map.Entry) iterator.next();
                String key = entry.getKey().toString();
                Vector values = (Vector) entry.getValue();
                fieldValues[i] = new RemoteFieldValue(key, makeStringVector(values));
                i++;
            }

            return fieldValues;
        }
        else
        {
            return null;
        }
    }

    private Hashtable makeIssueStruct(RemoteIssue issue)
    {
        try
        {
            // this is a bit of a hack as BeanUtils barfs on our non simple properties
            Vector components = null;
            Vector fixVersions = null;
            Vector affectsVersions = null;

            if (issue.getComponents() != null)
            {
                components = RpcUtils.makeVector(issue.getComponents());
                issue.setComponents(null);
            }
            if (issue.getAffectsVersions() != null)
            {
                affectsVersions = RpcUtils.makeVector(issue.getAffectsVersions());
                issue.setAffectsVersions(null);
            }
            if (issue.getFixVersions() != null)
            {
                fixVersions = RpcUtils.makeVector(issue.getFixVersions());
                issue.setFixVersions(null);
            }

            Vector customFieldValues = null;
            if (issue.getCustomFieldValues() != null)
            {
                customFieldValues = RpcUtils.makeVector(issue.getCustomFieldValues());
                issue.setCustomFieldValues(null);
            }

            // make a Hashtable, removing all null values from the Hashtable
            // note we also don't add class as it's not a real property!
            Hashtable result = new Hashtable(BeanUtils.describe(issue))
            {
                public synchronized Object put(Object key, Object value)
                {
                    if (value == null || key == null || "class".equals(key))
                    {
                        return null;
                    }
                    return super.put(key, value);
                }
            };

            // now handle dependent entities
            if (components != null)
            {
                result.put("components", components);
            }
            if (affectsVersions != null)
            {
                result.put("affectsVersions", affectsVersions);
            }
            if (fixVersions != null)
            {
                result.put("fixVersions", fixVersions);
            }
            if (customFieldValues != null)
            {
                result.put("customFieldValues", customFieldValues);
            }

            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private RemoteIssue makeRemoteIssue(Hashtable rIssueStruct)
    {
        Hashtable workStruct = new Hashtable(rIssueStruct);
        RemoteIssue rIssue = new RemoteIssue();
        try
        {
            // do some funkiness here for components etc as BeanUtils isn't that smart
            if (workStruct.containsKey("components"))
            {
                Vector componentStructs = (Vector) workStruct.get("components");
                RemoteComponent[] components = new RemoteComponent[componentStructs.size()];
                for (int i = 0; i < components.length; i++)
                {
                    components[i] = makeRemoteComponent((Hashtable) componentStructs.get(i));
                }
                rIssue.setComponents(components);
                workStruct.remove("components");
            }

            if (workStruct.containsKey("fixVersions"))
            {
                Vector versionStructs = (Vector) workStruct.get("fixVersions");
                RemoteVersion[] versions = new RemoteVersion[versionStructs.size()];
                for (int i = 0; i < versions.length; i++)
                {
                    versions[i] = makeRemoteVersion((Hashtable) versionStructs.get(i));
                }
                rIssue.setFixVersions(versions);
                workStruct.remove("fixVersions");
            }

            if (workStruct.containsKey("affectsVersions"))
            {
                Vector versionStructs = (Vector) workStruct.get("affectsVersions");
                RemoteVersion[] versions = new RemoteVersion[versionStructs.size()];
                for (int i = 0; i < versions.length; i++)
                {
                    versions[i] = makeRemoteVersion((Hashtable) versionStructs.get(i));
                }
                rIssue.setAffectsVersions(versions);
                workStruct.remove("affectsVersions");
            }

            if (workStruct.containsKey("customFieldValues"))
            {
                Vector structs = (Vector) workStruct.get("customFieldValues");
                RemoteCustomFieldValue[] customFieldValues = new RemoteCustomFieldValue[structs.size()];
                for (int i = 0; i < customFieldValues.length; i++)
                {
                    customFieldValues[i] = makeCustomFieldValue((Hashtable) structs.get(i));
                }
                rIssue.setCustomFieldValues(customFieldValues);
                workStruct.remove("customFieldValues");
            }

            BeanUtils.populate(rIssue, workStruct);
        }
        catch (Exception e)
        {
            throw new InfrastructureException("Could not create RemoteIssue from struct?", e);
        }
        return rIssue;
    }

    private RemoteCustomFieldValue makeCustomFieldValue(Hashtable struct)
    {
        Object id = struct.get("customfieldId");
        Object key = struct.get("key");

        RemoteCustomFieldValue customFieldValue = new RemoteCustomFieldValue();
        customFieldValue.setCustomfieldId(id != null ? id.toString() : null);
        customFieldValue.setKey(key != null ? key.toString() : null);
        customFieldValue.setValues(makeStringVector((Vector) struct.get("values")));
        return customFieldValue;
    }

    /**
     * This method creates a new String array
     *
     * @param vector a vector of objects (not only String objects)
     * @return new String array that represents given vector
     */
    private String[] makeStringVector(Vector vector)
    {
        final String[] strings = new String[vector.size()];
        int i = 0;
        for (Iterator iterator = vector.iterator(); iterator.hasNext();)
        {
            strings[i] = iterator.next().toString();
            i++;
        }
        return strings;
    }

    private RemoteComponent makeRemoteComponent(Hashtable componentStruct)
            throws IllegalAccessException, InvocationTargetException
    {
        RemoteComponent component = new RemoteComponent();
        BeanUtils.populate(component, componentStruct);
        return component;
    }

    private RemoteVersion makeRemoteVersion(Hashtable versionStruct)
            throws IllegalAccessException, InvocationTargetException
    {
        RemoteVersion version = new RemoteVersion();
        BeanUtils.populate(version, versionStruct);
        return version;
    }
}
