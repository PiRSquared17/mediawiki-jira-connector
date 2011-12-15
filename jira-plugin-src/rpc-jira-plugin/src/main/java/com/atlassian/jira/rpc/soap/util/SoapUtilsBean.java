package com.atlassian.jira.rpc.soap.util;

import com.atlassian.core.action.ActionUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutStorageException;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.soap.beans.RemoteComment;
import com.atlassian.jira.rpc.soap.beans.RemoteField;
import com.atlassian.jira.rpc.soap.beans.RemoteFieldValue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.jira.web.action.issue.UpdateFieldsHelperBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import webwork.dispatcher.ActionResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SoapUtilsBean
{
    private static final Logger log = Logger.getLogger(SoapUtilsBean.class);

    private final FieldLayoutManager fieldLayoutManager;
    private final FieldManager fieldManager;
    private final UpdateFieldsHelperBean updateFieldsHelperBean;
    private final JiraAuthenticationContext authenticationContext;
    private final IssueCreationHelperBean creationHelperBean;

    public SoapUtilsBean(final FieldLayoutManager fieldLayoutManager,
                         final FieldManager fieldManager, final UpdateFieldsHelperBean updateFieldsHelperBean,
                         final JiraAuthenticationContext authenticationContext,
                         final IssueCreationHelperBean creationHelperBean)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.fieldManager = fieldManager;
        this.updateFieldsHelperBean = updateFieldsHelperBean;
        this.authenticationContext = authenticationContext;
        this.creationHelperBean = creationHelperBean;
    }

    /**
     * @param issue
     * @param fieldname
     * @return
     * @throws FieldLayoutStorageException
     * @deprecated Please use {@link #isVisible(com.atlassian.jira.issue.Issue, String)} instead.
     */
    public boolean isVisible(GenericValue issue, String fieldname) throws FieldLayoutStorageException
    {
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
        return !fieldLayout.getFieldLayoutItem(fieldManager.getOrderableField(fieldname)).isHidden();
    }

    public boolean isVisible(Issue issue, String fieldname) throws FieldLayoutStorageException
    {
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue.getProject(), issue.getIssueTypeObject().getId());
        return !fieldLayout.getFieldLayoutItem(fieldManager.getOrderableField(fieldname)).isHidden();
    }

    public ExternalComment mapRemoteCommentToExternalComment(RemoteComment remoteComment)
    {
        try
        {
            ExternalComment comment = new ExternalComment();
            PropertyUtils.copyProperties(comment, remoteComment);
            return comment;
        }
        catch (Exception e)
        {
            log.warn("Could not convert issue", e);
            return null;
        }
    }

    public RemoteField[] convertFieldsToRemoteFields(Collection fields)
    {
        RemoteField[] remoteFields = new RemoteField[fields.size()];
        int i = 0;
        for (Iterator iterator = fields.iterator(); iterator.hasNext();)
        {
            Field field = (Field) iterator.next();
            RemoteField remoteField = new RemoteField(field.getId(), field.getName());
            remoteFields[i] = remoteField;
            i++;
        }

        return remoteFields;
    }

    public Map mapFieldValueToMap(RemoteFieldValue[] actionParams)
    {
        if (actionParams != null)
        {
            HashMap map = new HashMap(actionParams.length);
            for (int i = 0; i < actionParams.length; i++)
            {
                RemoteFieldValue remoteFieldValue = actionParams[i];
                map.put(remoteFieldValue.getId(), remoteFieldValue.getValues());
            }
            return map;
        }
        else
        {
            return null;
        }

    }


    public void updateIssue(MutableIssue issueObject,
            OperationContext operationContext,
            User user,
            ErrorCollection errors,
            I18nHelper i18n) throws RemoteException
    {
        try
        {
            ActionResult aResult = updateFieldsHelperBean.updateIssue(issueObject, operationContext, user, errors, i18n);
            ActionUtils.checkForErrors(aResult);
        }
        catch (Throwable e)
        {
            log.error("Exception occurred editing issue: " + e, e);
            throw new RemoteException("Error occurred editing issue: " + errors.getErrors() + " " + errors.getErrorMessages(), e);
        }

    }

    public void validate(Issue issueObject,
            OperationContext operationContext,
            Map actionParams,
            User user,
            ErrorCollection errors,
            I18nHelper i18n) throws RemoteException
    {
        try
        {
            updateFieldsHelperBean.validate(issueObject, operationContext, actionParams, user, errors, i18n);
        }
        catch (Throwable e)
        {
            log.error("Exception validating issue: " + e, e);
            throw new RemoteException("Error occurred validating issue: " + errors.getErrors() + " " + errors.getErrorMessages(), e);
        }
    }

    public RemoteField[] getFieldsForEdit(User user, Issue issueObject)
    {
        List fields = updateFieldsHelperBean.getFieldsForEdit(user, issueObject);
        return convertFieldsToRemoteFields(fields);
    }

    public RemoteField[] getFieldsForCreate(User user, Issue issue)
    {
        List fields = creationHelperBean.getFieldsForCreate(user, issue);
        return convertFieldsToRemoteFields(fields);
    }

    /**
     * This will set the user into the JIRA authentication context (ThreadLocal) and return the prevous user who was in
     * there.  Make sure you use a try / finally block when calling this method
     *
     * @param user the user to set into the JIRA auth context
     * @return the previous user that was in the JIRA auth context
     */
    public User setRemoteUserInJira(User user)
    {
        User oldUser = authenticationContext.getLoggedInUser();
        authenticationContext.setLoggedInUser(user);
        return oldUser;
    }
}
