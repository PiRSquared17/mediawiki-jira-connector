/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.soap.util;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutStorageException;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.soap.beans.AbstractRemoteEntity;
import com.atlassian.jira.rpc.soap.beans.RemoteAttachment;
import com.atlassian.jira.rpc.soap.beans.RemoteComment;
import com.atlassian.jira.rpc.soap.beans.RemoteComponent;
import com.atlassian.jira.rpc.soap.beans.RemoteCustomFieldValue;
import com.atlassian.jira.rpc.soap.beans.RemoteFilter;
import com.atlassian.jira.rpc.soap.beans.RemoteIssueType;
import com.atlassian.jira.rpc.soap.beans.RemotePriority;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import com.atlassian.jira.rpc.soap.beans.RemoteResolution;
import com.atlassian.jira.rpc.soap.beans.RemoteStatus;
import com.atlassian.jira.rpc.soap.beans.RemoteVersion;
import com.atlassian.jira.util.ErrorCollection;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SoapUtils
{
    public static RemoteProject[] getProjects(Collection projects, ApplicationProperties applicationProperties)
    {
        RemoteProject[] result = new RemoteProject[projects.size()];

        int count = 0;
        for (Iterator iterator = projects.iterator(); iterator.hasNext();)
        {
            GenericValue project = (GenericValue) iterator.next();
            RemoteProject remoteProject = new RemoteProject(project, applicationProperties);
            result[count++] = remoteProject;
        }

        return result;
    }

    public static RemoteVersion[] getVersions(Collection versions)
    {
        RemoteVersion[] result = new RemoteVersion[versions.size()];

        int count = 0;
        for (Iterator iterator = versions.iterator(); iterator.hasNext();)
        {
            Object o = iterator.next();
            GenericValue gv;
            if (o instanceof Version)
            {
                gv = ((Version) o).getGenericValue();
            }
            else
            {
                gv = (GenericValue) o;
            }
            result[count++] = new RemoteVersion(gv);
        }

        return result;
    }

    public static RemoteVersion[] convertVersionsToRemoteObject(Collection versions)
    {
        RemoteVersion[] result = new RemoteVersion[versions.size()];

        int count = 0;
        for (Iterator iterator = versions.iterator(); iterator.hasNext();)
        {
            Version version = (Version) iterator.next();
            result[count++] = new RemoteVersion(version.getId().toString(), version.getName(), version.isReleased()
                    , version.isArchived(), version.getSequence(), version.getReleaseDate());
        }

        return result;
    }


    public static RemoteComponent[] getComponents(Collection components)
    {
        RemoteComponent[] result = new RemoteComponent[components.size()];

        int count = 0;
        for (Iterator iterator = components.iterator(); iterator.hasNext();)
        {
            GenericValue component = (GenericValue) iterator.next();
            result[count++] = new RemoteComponent(component);
        }

        return result;
    }

    public static RemoteComponent[] convertComponentsToRemoteObject(Collection components)
    {
        RemoteComponent[] result = new RemoteComponent[components.size()];

        int count = 0;
        for (Iterator iterator = components.iterator(); iterator.hasNext();)
        {
            ProjectComponent component = (ProjectComponent) iterator.next();
            result[count++] = new RemoteComponent(component.getId().toString(), component.getName());
        }

        return result;
    }

    public static RemoteIssueType[] getIssueTypes(Collection issueTypes)
    {
        RemoteIssueType[] result = new RemoteIssueType[issueTypes.size()];

        SubTaskManager subTaskManager = ComponentManager.getInstance().getSubTaskManager();
        int count = 0;
        for (Iterator iterator = issueTypes.iterator(); iterator.hasNext();)
        {
            GenericValue issueType = (GenericValue) iterator.next();
            result[count++] = new RemoteIssueType(issueType, subTaskManager.isSubTaskIssueType(issueType));
        }

        return result;
    }

    public static RemoteIssueType[] getIssueTypeObjects(Collection issueTypeObjects)
    {
        RemoteIssueType[] result = new RemoteIssueType[issueTypeObjects.size()];

        int count = 0;
        for (Iterator iterator = issueTypeObjects.iterator(); iterator.hasNext();)
        {
            IssueType issueType = (IssueType) iterator.next();
            result[count++] = new RemoteIssueType(issueType.getGenericValue(), issueType.isSubTask());
        }

        return result;
    }

    public static RemoteStatus[] getStatuses(Collection statuses)
    {
        RemoteStatus[] result = new RemoteStatus[statuses.size()];

        int count = 0;
        for (Iterator iterator = statuses.iterator(); iterator.hasNext();)
        {
            GenericValue status = (GenericValue) iterator.next();
            result[count++] = new RemoteStatus(status);
        }

        return result;
    }

    public static RemoteResolution[] getResolutions(Collection resolutions)
    {
        RemoteResolution[] result = new RemoteResolution[resolutions.size()];

        int count = 0;
        for (Iterator iterator = resolutions.iterator(); iterator.hasNext();)
        {
            GenericValue resolution = (GenericValue) iterator.next();
            result[count++] = new RemoteResolution(resolution);
        }

        return result;
    }

    public static RemotePriority[] getPriorities(Collection priorities)
    {
        RemotePriority[] result = new RemotePriority[priorities.size()];

        int count = 0;
        for (Iterator iterator = priorities.iterator(); iterator.hasNext();)
        {
            GenericValue priority = (GenericValue) iterator.next();
            result[count++] = new RemotePriority(priority);
        }

        return result;
    }

    public static RemoteFilter[] getFilters(Collection searchRequests)
    {
        RemoteFilter[] result = new RemoteFilter[searchRequests.size()];

        int count = 0;
        for (Iterator iterator = searchRequests.iterator(); iterator.hasNext();)
        {
            SearchRequest searchRequest = (SearchRequest) iterator.next();
            result[count++] = new RemoteFilter(searchRequest);
        }

        return result;
    }

    public static RemoteComment[] getComments(List comments)
    {
        RemoteComment[] result = new RemoteComment[comments.size()];

        int count = 0;
        for (Iterator iterator = comments.iterator(); iterator.hasNext();)
        {
            Comment comment = (Comment) iterator.next();
            result[count++] = new RemoteComment(comment);
        }

        return result;
    }

    public static RemoteAttachment[] getAttachments(List attachments)
    {
        RemoteAttachment[] result = new RemoteAttachment[attachments.size()];

        int count = 0;
        for (Iterator iterator = attachments.iterator(); iterator.hasNext();)
        {
            Attachment attachment = (Attachment) iterator.next();
            result[count++] = new RemoteAttachment(attachment);
        }

        return result;
    }

    public static RemoteCustomFieldValue[] getCustomFieldValues(List customFields, Issue issue)
    {
        List remoteCustomFieldValues = new ArrayList();
        for (Iterator iterator = customFields.iterator(); iterator.hasNext();)
        {
            CustomField customField = (CustomField) iterator.next();
            final CustomFieldType customFieldType = customField.getCustomFieldType();
            Object cfTransferObjectValue = customField.getValue(issue);

            // Deal with the three types of CF Values
            if (cfTransferObjectValue instanceof CustomFieldParams)
            {
                CustomFieldParams customFieldParams = (CustomFieldParams) cfTransferObjectValue;
                if (!customFieldParams.isEmpty())
                {
                    Set keyValues = customFieldParams.getKeysAndValues().entrySet();
                    for (Iterator iterator1 = keyValues.iterator(); iterator1.hasNext();)
                    {
                        Map.Entry entry = (Map.Entry) iterator1.next();
                        final String key = (String) entry.getKey();
                        String[] valuesForLevel = transformToStringArray((Collection) entry.getValue(), customFieldType);

                        if (key == null || "".equals(key))
                        {
                            remoteCustomFieldValues.add(new RemoteCustomFieldValue(customField.getId(), null, valuesForLevel));
                        }
                        else
                        {
                            remoteCustomFieldValues.add(new RemoteCustomFieldValue(customField.getId(), key, valuesForLevel));
                        }
                    }
                }
            }
            else if (cfTransferObjectValue instanceof Collection)
            {
                String[] values = transformToStringArray((Collection) cfTransferObjectValue, customFieldType);
                remoteCustomFieldValues.add(new RemoteCustomFieldValue(customField.getId(), null, values));
            }
            else if (cfTransferObjectValue != null)
            {
                String[] values = new String[] { getSoapStringValue(customFieldType, cfTransferObjectValue) };
                remoteCustomFieldValues.add(new RemoteCustomFieldValue(customField.getId(), null, values));
            }
        }

        // Convert to Typed array
        RemoteCustomFieldValue[] result = new RemoteCustomFieldValue[remoteCustomFieldValues.size()];
        int count = 0;
        for (Iterator iterator = remoteCustomFieldValues.iterator(); iterator.hasNext();)
        {
            result[count++] = (RemoteCustomFieldValue) iterator.next();
        }

        return result;
    }

    private static String[] transformToStringArray(Collection values, CustomFieldType customFieldType)
    {
        String[] returnValues = new String[values.size()];

        int i = 0;
        for (Iterator iterator = values.iterator(); iterator.hasNext();)
        {
            Object singularObject = iterator.next();
            returnValues[i] = getSoapStringValue(customFieldType, singularObject);
            i++;
        }

        return returnValues;
    }

    private static String getSoapStringValue(final CustomFieldType customFieldType, final Object singularObject)
    {
        String stringValue;
        if (singularObject instanceof Option)
        {
            stringValue = ((Option) singularObject).getValue();
        }
        else
        {
            stringValue = customFieldType.getStringFromSingularObject(singularObject);
        }
        return stringValue;
    }

    public static String[] getRemoteEntityIdsAsString(AbstractRemoteEntity[] entities)
    {
        String[] entityIds = new String[entities.length];

        for (int i = 0; i < entities.length; i++)
        {
            AbstractRemoteEntity entity = entities[i];
            entityIds[i] = (entity.getId());
        }
        return entityIds;
    }

    public static Long[] getRemoteEntityIdsAsLong(AbstractRemoteEntity[] entities)
    {
        if (entities == null)
        {
            return null;
        }
        try
        {
            Long[] entityIds = new Long[entities.length];

            for (int i = 0; i < entities.length; i++)
            {
                AbstractRemoteEntity entity = entities[i];
                entityIds[i] = new Long(entity.getId());
            }
            return entityIds;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    public static List getRemoteEntityIds(final AbstractRemoteEntity[] entities)
    {
        List versionIds = new ArrayList(entities.length);

        for (int i = 0; i < entities.length; i++)
        {
            AbstractRemoteEntity entity = entities[i];
            versionIds.add(new Long(entity.getId()));
        }
        return versionIds;
    }

    public static Timestamp toTimeStamp(Date date)
    {
        return date == null ? null : new Timestamp(date.getTime());
    }

    public static boolean isVisible(GenericValue issue, String fieldname) throws FieldLayoutStorageException
    {
        FieldLayout fieldLayout = ComponentManager.getInstance().getFieldLayoutManager().getFieldLayout(issue);
        return !fieldLayout.getFieldLayoutItem(ComponentManager.getInstance().getFieldManager().getOrderableField(fieldname)).isHidden();
    }


    /**
     * Converts a string into a long with validation exception handling
     *
     * @param longStr the string to comvery
     * @return a Long or null if the string is already null
     * @throws RemoteValidationException if longStr is not a String representing a valid long number
     */
    public static Long toLong(String longStr) throws RemoteValidationException
    {
        if (longStr == null)
        {
            return null;
        }
        return toLongRequired(longStr);
    }

    /**
     * Converts a string  into a long with validation exception handling.  The string is required to be non null and in
     * Long ready form.
     *
     * @param longStr long value as String
     * @return a Long Long object representing the String parameter passed in
     * @throws RemoteValidationException if longStr is not a String representing a valid long number
     */
    public static Long toLongRequired(String longStr) throws RemoteValidationException
    {
        try
        {
            return new Long(longStr);
        }
        catch (NumberFormatException nfe)
        {
            throw new RemoteValidationException(nfe);
        }
    }

    /**
     * This will check a JiraServiceContext and if it contains any errors or error messages, it will throw a
     * RemoteException.
     *
     * @param serviceContext the JiraServiceContext to check
     * @throws RemoteException if it has any errors or error messages in it
     */
    public static void checkServiceContext(final JiraServiceContext serviceContext) throws RemoteException
    {
        final ErrorCollection errorCollection = serviceContext.getErrorCollection();
        if (errorCollection.hasAnyErrors())
        {
            if (errorCollection.getErrorMessages().isEmpty())
            {
                final Map errors = errorCollection.getErrors();
                throw new RemoteException((String) errors.values().iterator().next());
            }
            else
            {
                throw new RemoteException((String) errorCollection.getErrorMessages().iterator().next());
            }
        }
    }

}
