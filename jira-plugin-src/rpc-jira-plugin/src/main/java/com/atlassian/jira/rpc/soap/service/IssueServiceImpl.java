package com.atlassian.jira.rpc.soap.service;

import com.atlassian.core.util.LocaleUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.comment.DefaultCommentService;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueUtilsBean;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.exception.RemoteValidationException;
import com.atlassian.jira.rpc.soap.beans.RemoteAttachment;
import com.atlassian.jira.rpc.soap.beans.RemoteComment;
import com.atlassian.jira.rpc.soap.beans.RemoteCustomFieldValue;
import com.atlassian.jira.rpc.soap.beans.RemoteField;
import com.atlassian.jira.rpc.soap.beans.RemoteFieldValue;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.atlassian.jira.rpc.soap.beans.RemoteNamedObject;
import com.atlassian.jira.rpc.soap.beans.RemoteSecurityLevel;
import com.atlassian.jira.rpc.soap.beans.RemoteWorklog;
import com.atlassian.jira.rpc.soap.util.PluginSoapAttachmentHelper;
import com.atlassian.jira.rpc.soap.util.SoapUtils;
import com.atlassian.jira.rpc.soap.util.SoapUtilsBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class IssueServiceImpl implements IssueService
{
    private PermissionManager permissionManager;
    private ProjectManager projectManager;
    private IssueManager issueManager;
    private AttachmentManager attachmentManager;
    private LocaleUtils localeUtils;
    private ApplicationProperties applicationProperties;
    private final IssueFactory issueFactory;
    private final CommentService commentService;
    private final com.atlassian.jira.bc.projectroles.ProjectRoleService projectRoleService;
    private final IssueUpdater issueUpdater;
    private final AttachmentService attachmentService;
    private final JiraDurationUtils jiraDurationUtils;
    private final com.atlassian.jira.bc.issue.IssueService issueService;
    private CustomFieldManager customFieldManager;
    private PluginSoapAttachmentHelper attachmentHelper;
    private final IssueCreationHelperBean issueCreationHelperBean;
    private final OutlookDateManager outlookDateManager;
    private final IssueUtilsBean issueUtilsBean;
    private final JiraAuthenticationContext authenticationContext;
    private final SoapUtilsBean soapUtilsBean;
    private final ConstantsManager constantsManager;
    private final WorklogService worklogService;
    private final SubTaskManager subTaskManager;
    private final IssueRetriever issueRetriever;

    private static final Logger log = Logger.getLogger(IssueServiceImpl.class);

    public static final String GENERIC_CONTENT_TYPE = "application/octet-stream";

    public IssueServiceImpl(PermissionManager permissionManager,
                            ProjectManager projectManager,
                            IssueManager issueManager,
                            AttachmentManager attachmentManager,
                            CustomFieldManager customFieldManager,
                            IssueCreationHelperBean issueCreationHelperBean,
                            OutlookDateManager outlookDateManager,
                            IssueUtilsBean issueUtilsBean,
                            JiraAuthenticationContext authenticationContext,
                            SoapUtilsBean soapUtilsBean,
                            ConstantsManager constantsManager,
                            LocaleUtils localeUtils,
                            ApplicationProperties applicationProperties,
                            IssueFactory issueFactory,
                            CommentService commentService,
                            com.atlassian.jira.bc.projectroles.ProjectRoleService projectRoleService,
                            IssueUpdater issueUpdater,
                            AttachmentService attachmentService,
                            WorklogService worklogService,
                            JiraDurationUtils jiraDurationUtils,
                            com.atlassian.jira.bc.issue.IssueService issueService,
                            SubTaskManager subTaskManager)
    {
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.issueManager = issueManager;
        this.attachmentManager = attachmentManager;
        this.customFieldManager = customFieldManager;
        this.localeUtils = localeUtils;
        this.applicationProperties = applicationProperties;
        this.issueFactory = issueFactory;
        this.commentService = commentService;
        this.projectRoleService = projectRoleService;
        this.issueUpdater = issueUpdater;
        this.attachmentService = attachmentService;
        this.jiraDurationUtils = jiraDurationUtils;
        this.issueService = issueService;
        this.subTaskManager = subTaskManager;
        this.attachmentHelper = PluginSoapAttachmentHelper.getInstance();
        this.issueCreationHelperBean = issueCreationHelperBean;
        this.outlookDateManager = outlookDateManager;
        this.issueUtilsBean = issueUtilsBean;
        this.authenticationContext = authenticationContext;
        this.soapUtilsBean = soapUtilsBean;
        this.constantsManager = constantsManager;
        this.worklogService = worklogService;
        this.issueRetriever = new IssueRetriever(issueManager, permissionManager);
    }

    public RemoteIssue getIssue(User user, String issueKey) throws RemoteException, RemotePermissionException
    {
        //JRA-13712: Need to set the user into the authenticationContext, as it may be used by certain fields during
        //issue constructions.
        final User oldUser = setRemoteUserInJira(user);
        try
        {
            final IssueRetriever.IssueInfo issueInfo = issueRetriever.retrieveIssue(issueKey, user);
            return new RemoteIssue(issueInfo.getIssue(), issueInfo.getParentIssue(), customFieldManager, attachmentManager, soapUtilsBean);
        }
        finally
        {
            setRemoteUserInJira(oldUser);
        }
    }

    public RemoteIssue createIssueWithSecurityLevel(final User user, final String parentIssueKey, final RemoteIssue rIssue, final Long securityLevelId)
            throws RemotePermissionException, RemoteValidationException, RemoteException
    {
        if (securityLevelId != null)
        {
            if (!permissionManager.hasPermission(Permissions.SET_ISSUE_SECURITY, projectManager.getProjectObjByKey(rIssue.getProject()), user))
            {
                throw new RemotePermissionException("This user does not have the 'set issue security' permission.");
            }
        }

        User oldUser = setRemoteUserInJira(user);
        try
        {
            final GenericValue project = validateRpcOnlyFields(rIssue, user);

            // sub task validation
            final Issue parentIssue;
            if (parentIssueKey != null)
            {
                parentIssue = validateSubTaskCreate(user, parentIssueKey, rIssue);
            }
            else
            {
                parentIssue = null;
            }

            // Go nuts with making a issue input params
            IssueInputParameters issueInputParameters = makeIssueInputParameters(project, rIssue, user, securityLevelId);
            // We want to indicate to the create validation which fields are provided and which need to be populated
            // with their default values
            // NOTE: this behaves slightly different from 4.0. Here we will not set the default value of a required field but we will
            // get it to run its validation. In 4.0 we would run its validation (which will always report an error if the value is not
            // provided and it is required) but we would then populate the field values holder with its default value.
            // The end result should be the same, an error exception reported to the user, it is just that the field
            // values holder will no longer contain the fields default value.
            final com.atlassian.jira.bc.issue.IssueService.CreateValidationResult createValidationResult;
            if (parentIssue != null)
            {
                createValidationResult = this.issueService.validateSubTaskCreate(user, parentIssue.getId(), issueInputParameters);
            }
            else
            {
                createValidationResult = this.issueService.validateCreate(user, issueInputParameters);
            }
            // Throw exception if there's a problem
            if (!createValidationResult.isValid())
            {
                final ErrorCollection errors = createValidationResult.getErrorCollection();
                throw new RemoteValidationException(errors.getErrors() + " : " + errors.getErrorMessages().toString());
            }

            final com.atlassian.jira.bc.issue.IssueService.IssueResult createIssueResult = issueService.create(user, createValidationResult);

            if (!createIssueResult.isValid())
            {
                final ErrorCollection errors = createIssueResult.getErrorCollection();
                throw new RemoteValidationException(errors.getErrors() + " : " + errors.getErrorMessages().toString());
            }
            final Issue createdIssue = createIssueResult.getIssue();
            if (parentIssue != null)
            {
                // I would argue this should be in the issue service but its not so
                // we do the same extra link step as the action does.
                try
                {
                    subTaskManager.createSubTaskIssueLink(parentIssue, createdIssue, user);
                }
                catch (CreateException e)
                {
                    throw new RemoteException(e);
                }
            }

            // Attach files if there are any
            if (rIssue.getAttachmentNames() != null)
            {
                addAttachmentToIssueFromMimeAttachments(user, rIssue.getAttachmentNames(), createdIssue.getGenericValue());
            }
            final IssueRetriever.IssueInfo issueInfo = issueRetriever.retrieveIssue(createdIssue, user);
            return new RemoteIssue(issueInfo.getIssue(), issueInfo.getParentIssue(), customFieldManager, attachmentManager, soapUtilsBean);
        }
        catch (DataAccessException e)
        {
            throw new RemoteException("Error creating issue: " + e, e);
        }
        finally
        {
            setRemoteUserInJira(oldUser);
        }
    }

    private Issue validateSubTaskCreate(User user, String parentIssueKey, RemoteIssue rIssue) throws RemoteValidationException, RemotePermissionException
    {
        if (!subTaskManager.isSubTasksEnabled())
        {
            throw new RemoteValidationException("Subtasks are not enabled in this instance of JIRA");
        }
        final IssueRetriever.IssueInfo parentInfo = issueRetriever.retrieveIssue(parentIssueKey, user);
        final Issue parentIssue = parentInfo.getIssue();
        final String parentProjectKey = parentIssue.getProjectObject().getKey();

        // is it in the same project
        if (!parentProjectKey.equals(rIssue.getProject()))
        {
            throw new RemoteValidationException("The parent issue is not in the same project");
        }
        if (!parentIssue.isEditable())
        {
            throw new RemoteValidationException("The parent issue does not exist or is not editable");
        }
        // Subtasks of subtasks seems to work but is not supported yet.  There are moves a foot to see this happen sooner rather than later
        // but for now we toe the line that sub tasks cant have subtasks.
        if (subTaskManager.isSubTask(parentIssue.getGenericValue()))
        {
            throw new RemoteValidationException("Parent is already a subtask : " + parentIssueKey);
        }
        return parentIssue;
    }

    private Collection<String> getProvidedFields(final User user, final GenericValue project, final IssueInputParameters issueInputParameters)
    {
        final Collection<String> providedFields = new ArrayList<String>();
        final MutableIssue contextIssue = issueFactory.getIssue();
        // Most calls using the issue object will fail unless the issue object has the project and issue type are set
        contextIssue.setProject(project);
        contextIssue.setIssueTypeId(issueInputParameters.getIssueTypeId());

        FieldScreenRenderer renderer = issueCreationHelperBean.createFieldScreenRenderer(user, contextIssue);
        FieldLayout fieldLayout = renderer.getFieldLayout();
        List visibleLayoutItems = fieldLayout.getVisibleLayoutItems(user, projectManager.getProjectObj(project.getLong("id")), EasyList.build(issueInputParameters.getIssueTypeId()));
        for (Iterator iterator = visibleLayoutItems.iterator(); iterator.hasNext();)
        {
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) iterator.next();
            OrderableField orderableField = fieldLayoutItem.getOrderableField();
            final String fieldId = orderableField.getId();
            if (issueInputParameters.isFieldSet(fieldId) || fieldLayoutItem.isRequired())
            {
                providedFields.add(fieldId);
            }
        }
        return providedFields;
    }

    public RemoteIssue updateIssue(User user, String issueKey, Map actionParams) throws RemoteException
    {
        final Issue issueObject = issueRetriever.retrieveIssue(issueKey, user).getIssue();

        User oldUser = setRemoteUserInJira(user);
        try
        {

            final IssueInputParameters issueInputParameters = new IssueInputParametersImpl(actionParams);
            final Long projectId = issueObject.getProjectObject().getId();

            final List<CustomField> customFieldObjects = customFieldManager.getCustomFieldObjects(issueObject);

            for (final CustomField customField : customFieldObjects)
            {
                final Long customFieldId = customField.getIdAsLong();

                final String[] values = issueInputParameters.getCustomFieldValue(customFieldId);

                final FieldConfig fieldConfig = customField.getRelevantConfig(new IssueContextImpl(projectId,
                        issueObject.getIssueTypeObject().getId()));
                final Options options = customField.getOptions(null, fieldConfig, new ProjectContext(projectId));

                if(values != null && options != null)
                {
                    // If options are returned and they are in fact options then we should try and translate any strings to Option Ids
                    // Versions and Projects (maybe more classes) erroneously implement Options but fill the list with random things and not instances of Option.
                    if (options.isEmpty() || options.get(0) instanceof Option)
                    {
                        final String[] sanitisedValues = convertCustomFieldOptionValuesToOptionIds(options, values);
                        issueInputParameters.addCustomFieldValue(customFieldId, sanitisedValues);
                    }
                }
            }

            final com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult validationResult = issueService.validateUpdate(user, issueObject.getId(), issueInputParameters);

            if (!validationResult.isValid())
            {
                throw new RemoteValidationException("Fields not valid for issue: \n" + validationResult.getErrorCollection());
            }

            final com.atlassian.jira.bc.issue.IssueService.IssueResult issueResult = issueService.update(user, validationResult);

            final IssueRetriever.IssueInfo issueInfo = issueRetriever.retrieveIssue(issueResult.getIssue(), user);
            return new RemoteIssue(issueInfo.getIssue(), issueInfo.getParentIssue(), customFieldManager, attachmentManager, soapUtilsBean);
        }
        finally
        {
            setRemoteUserInJira(oldUser);
        }
    }

    User setRemoteUserInJira(User user)
    {
        return soapUtilsBean.setRemoteUserInJira(user);
    }

    public RemoteIssue updateIssue(User user, String issueKey, RemoteFieldValue[] actionParams) throws RemoteException
    {
        return updateIssue(user, issueKey, soapUtilsBean.mapFieldValueToMap(actionParams));
    }

    public RemoteField[] getFieldsForCreate(User user, String projectKey, Long issueTypeId) throws RemoteException
    {
        //JRA-13703: Need to have the user available in the authenticationContext, such that fields
        // can carry out permission checks.
        final User oldUser = setRemoteUserInJira(user);
        try
        {
            if (issueTypeId == null)
            {
                throw new RemoteValidationException("The issue type must be specified");
            }
            Project project = projectManager.getProjectObjByKey(projectKey);
            if (project == null || !permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user))
            {
                throw new RemotePermissionException("You do not have create permission for this project.");
            }

            MutableIssue issue = issueFactory.getIssue();
            issue.setProjectId(project.getId());
            issue.setIssueTypeId(issueTypeId.toString());

            return soapUtilsBean.getFieldsForCreate(user, issue);
        }
        finally
        {
            setRemoteUserInJira(oldUser);
        }
    }

    public RemoteField[] getFieldsForEdit(User user, String issueKey) throws RemoteException
    {
        //JRA-13703: Need to have the user available in the authenticationContext, such that fields
        // can carry out permission checks.
        final User oldUser = setRemoteUserInJira(user);
        try
        {
            final Issue issueObject = issueRetriever.retrieveIssue(issueKey, user).getIssue();
            if (!permissionManager.hasPermission(Permissions.EDIT_ISSUE, issueObject, user))
            {
                throw new RemotePermissionException("You do not have edit permission for this issue.");
            }

            return soapUtilsBean.getFieldsForEdit(user, issueObject);
        }
        finally
        {
            setRemoteUserInJira(oldUser);
        }
    }


    private GenericValue validateRpcOnlyFields(RemoteIssue rIssue, User user)
            throws RemotePermissionException, RemoteValidationException
    {
        //if there is no project then the issue cannot be created
        final GenericValue project = projectManager.getProjectByKey(rIssue.getProject());
        if (project == null || !permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user))
        {
            throw new RemotePermissionException("The project specified does not exist or you don't have permission to create issues in it.");
        }

        if (StringUtils.isNotEmpty(rIssue.getId()))
        {
            throw new RemoteValidationException("You cannot specify an issue ID when creating an issue.");
        }

        if (StringUtils.isNotEmpty(rIssue.getKey()))
        {
            throw new RemoteValidationException("You cannot specify an issue key when creating an issue.");
        }

        if (StringUtils.isEmpty(rIssue.getType()))
        {
            throw new RemoteValidationException("No issue type specified.");
        }

        if (constantsManager.getIssueType(rIssue.getType()) == null)
        {
            throw new RemoteValidationException("Invalid issue type specified: " + rIssue.getType());
        }

        if (StringUtils.isEmpty(rIssue.getSummary()))
        {
            throw new RemoteValidationException("You must specify a summary when creating an issue.");
        }
        return project;
    }

    private IssueInputParameters makeIssueInputParameters(final GenericValue project, RemoteIssue rIssue, User user, Long securityLevelId)
            throws RemoteValidationException
    {
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        final Long projectId = project.getLong("id");
        issueInputParameters.setProjectId(projectId);
        issueInputParameters.setIssueTypeId(rIssue.getType());
        issueInputParameters.setSummary(rIssue.getSummary());
        if (StringUtils.isNotEmpty(rIssue.getReporter()))
        {
            issueInputParameters.setReporterId(rIssue.getReporter());
        }
        else
        {
            issueInputParameters.setReporterId(user.getName());
        }
        issueInputParameters.setAssigneeId(rIssue.getAssignee());
        issueInputParameters.setDescription(rIssue.getDescription());
        issueInputParameters.setEnvironment(rIssue.getEnvironment());
        issueInputParameters.setStatusId(rIssue.getStatus());
        issueInputParameters.setPriorityId(rIssue.getPriority());
        issueInputParameters.setResolutionId(rIssue.getResolution());
        issueInputParameters.setSecurityLevelId(securityLevelId);
        issueInputParameters.setFixVersionIds(SoapUtils.getRemoteEntityIdsAsLong(rIssue.getFixVersions()));
        issueInputParameters.setAffectedVersionIds(SoapUtils.getRemoteEntityIdsAsLong(rIssue.getAffectsVersions()));
        issueInputParameters.setComponentIds(SoapUtils.getRemoteEntityIdsAsLong(rIssue.getComponents()));

        // Setup the formatted due date
        if (outlookDateManager != null)
        {
            JiraUserPreferences userPreferences = new JiraUserPreferences(user);
            String locale = userPreferences.getString(PreferenceKeys.USER_LOCALE);
            Locale userLocale;
            if (TextUtils.stringSet(locale))
            {
                userLocale = localeUtils.getLocale(locale);
            }
            else
            {
                userLocale = applicationProperties.getDefaultLocale();
            }
            final OutlookDate outlookDate = outlookDateManager.getOutlookDate(userLocale);
            if (rIssue.getDuedate() != null)
            {
                issueInputParameters.setDueDate(outlookDate.formatDatePicker(rIssue.getDuedate()));
            }
        }

        // Include all the custom fields
        final RemoteCustomFieldValue[] remoteCustomFieldValues = rIssue.getCustomFieldValues();
        if (remoteCustomFieldValues != null && remoteCustomFieldValues.length > 0)
        {
            for (RemoteCustomFieldValue remoteCustomFieldValue : remoteCustomFieldValues)
            {
                final String customfieldId = remoteCustomFieldValue.getCustomfieldId();
                final String key = remoteCustomFieldValue.getKey();

                CustomField customField = customFieldManager.getCustomFieldObject(customfieldId);
                if (customField != null)
                {
                    String fullCfKey = customfieldId + (StringUtils.isEmpty(key) ? "" : ":" + remoteCustomFieldValue.getKey());

                    final String[] values = remoteCustomFieldValue.getValues();
                    final FieldConfig fieldConfig = customField.getRelevantConfig(new IssueContextImpl(projectId, rIssue.getType()));
                    final Options options = customField.getOptions(null, fieldConfig, new ProjectContext(projectId));

                    // If options are returned and they ar in fact options then we should try and translate any strings to Option Ids
                    // Versions and Projects (maybe more classes) erroneously implement Options but fill the list with random things and not instances of Option.
                    if (options != null && (options.isEmpty() || options.get(0) instanceof Option))
                    {
                        final String[] sanitisedValues = convertCustomFieldOptionValuesToOptionIds(options, values);
                        issueInputParameters.addCustomFieldValue(fullCfKey, sanitisedValues);
                    }
                    else
                    {
                        issueInputParameters.addCustomFieldValue(fullCfKey, values);
                    }
                }
                else
                {
                    throw new RemoteValidationException("Custom field ID '" + remoteCustomFieldValue.getCustomfieldId() + "' is invalid.");
                }
            }
        }

        // Lets get the provided fields map and put it into the input parameters
        issueInputParameters.setProvidedFields(getProvidedFields(user, project, issueInputParameters));

        return issueInputParameters;
    }

    public void deleteIssue(User user, String issueKey) throws RemoteException, RemotePermissionException
    {
        final GenericValue project = projectManager.getProjectByKey(this.getIssue(user, issueKey).getProject());
        if (project == null || !permissionManager.hasPermission(Permissions.DELETE_ISSUE, project, user))
        {
            throw new RemotePermissionException("The project specified does not exist or you don't have permission to delete issues in it.");
        }
        try
        {
            final Issue issue = issueRetriever.retrieveIssue(issueKey, user).getIssue();
            com.atlassian.jira.bc.issue.IssueService.DeleteValidationResult validationResult = issueService.validateDelete(user, issue.getId());
            if (validationResult.isValid())
            {
                issueService.delete(user, validationResult);
            }
            else
            {
                throw new RemoveException("Delete action executed with errors");
            }
        }
        catch (Exception e)
        {
            throw new RemoteException("Unable to delete issue, cause: " + e.getMessage(), e);
        }
    }

    public RemoteComment[] getComments(User user, String issueKey) throws RemoteException, RemotePermissionException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        Issue issue = issueRetriever.retrieveIssue(issueKey, user).getIssue();
        RemoteComment[] comments = SoapUtils.getComments(commentService.getCommentsForUser(user, issue, errorCollection));

        checkAndThrowRemoteException(errorCollection);

        return comments;
    }

    public void addComment(User user, String issueKey, RemoteComment remoteComment)
            throws RemoteException, RemotePermissionException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        ProjectRole projectRole = null;

        final String roleLevel = remoteComment.getRoleLevel();
        if (StringUtils.isNotBlank(roleLevel))
        {
            projectRole = projectRoleService.getProjectRoleByName(user, roleLevel, errorCollection);
            if (projectRole == null)
            {
                throw new RemoteException("Project role: " + roleLevel + " does not exist");
            }
        }

        commentService.create(
                user,
                issueRetriever.retrieveIssue(issueKey, user).getIssue(),
                remoteComment.getBody(),
                remoteComment.getGroupLevel(),
                projectRole == null ? null : projectRole.getId(),
                remoteComment.getCreated(),
                true,
                errorCollection);

        checkAndThrowRemoteException(errorCollection);
    }

    public boolean hasPermissionToEditComment(User user, RemoteComment remoteComment) throws RemoteException
    {
        // Before we start using the comment we need to make sure it is somewhat valid
        if (remoteComment == null)
        {
            throw new RemoteException(getI18nHelper().getText(DefaultCommentService.ERROR_NULL_COMMENT));
        }
        if (remoteComment.getId() == null)
        {
            throw new RemoteException(getI18nHelper().getText(DefaultCommentService.ERROR_NULL_COMMENT_ID));
        }

        final ErrorCollection errorCollection = new SimpleErrorCollection();

        final Long commentId = (remoteComment.getId() == null) ? null : new Long(remoteComment.getId());

        final Comment comment = commentService.getMutableComment(user, commentId, errorCollection);
        checkAndThrowRemoteException(errorCollection);

        boolean hasPermissionToEdit = commentService.hasPermissionToEdit(user, comment, errorCollection);
        return !errorCollection.hasAnyErrors() && hasPermissionToEdit;

    }

    public RemoteComment editComment(User user, RemoteComment remoteComment) throws RemoteException
    {
        // Before we start using the comment we need to make sure it is somewhat valid
        if (remoteComment == null)
        {
            throw new RemoteException(getI18nHelper().getText(DefaultCommentService.ERROR_NULL_COMMENT));
        }
        if (remoteComment.getId() == null)
        {
            throw new RemoteException(getI18nHelper().getText(DefaultCommentService.ERROR_NULL_COMMENT_ID));
        }

        ErrorCollection errorCollection = new SimpleErrorCollection();

        ProjectRole projectRole = null;

        final String roleLevel = remoteComment.getRoleLevel();
        if (StringUtils.isNotBlank(roleLevel))
        {
            projectRole = projectRoleService.getProjectRoleByName(user, roleLevel, errorCollection);
            if (projectRole == null)
            {
                throw new RemoteException("Project role: " + roleLevel + " does not exist");
            }
        }

        final Long commentId = (remoteComment.getId() == null) ? null : new Long(remoteComment.getId());
        final Long projectRoleId = (projectRole == null) ? null : projectRole.getId();
        commentService.validateCommentUpdate(user, commentId, remoteComment.getBody(),
                remoteComment.getGroupLevel(), projectRoleId, errorCollection);

        // If validation produced any errors lets throw them out the user
        checkAndThrowRemoteException(errorCollection);

        final MutableComment mutableComment = commentService.getMutableComment(user, commentId, errorCollection);

        // If comment does not exist
        checkAndThrowRemoteException(errorCollection);

        Issue issue = mutableComment.getIssue();
        if (issue == null)
        {
            throw new RemoteException("No issue found for comment with id: " + commentId);
        }

        mutableComment.setBody(remoteComment.getBody());
        mutableComment.setGroupLevel(remoteComment.getGroupLevel());
        mutableComment.setRoleLevelId(projectRoleId);
        commentService.update(user, mutableComment, true, errorCollection);
        checkAndThrowRemoteException(errorCollection);

        return new RemoteComment(mutableComment);
    }

    public RemoteComment getComment(User user, Long commentId) throws RemoteException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        Comment comment = commentService.getCommentById(user, commentId, errorCollection);
        checkAndThrowRemoteException(errorCollection);

        return new RemoteComment(comment);
    }

    /**
     * Throws a RemoteValidation if errorCollection is non empty.
     *
     * @param errorCollection the ErrorCollection to check.
     * @throws RemoteValidationException if errorCollection is non empty.
     */
    private void checkAndThrowValidationException(ErrorCollection errorCollection) throws RemoteValidationException
    {
        if (errorCollection.hasAnyErrors())
        {
            throw new RemoteValidationException(errorCollection.toString());
        }
    }

    private void checkAndThrowRemoteException(ErrorCollection errorCollection) throws RemoteException
    {
        if (errorCollection.hasAnyErrors())
        {
            throw new RemoteException(errorCollection.toString());
        }
    }

    public RemoteNamedObject[] getAvailableActions(User user, String issueKey) throws RemoteException
    {
        User oldUser = setRemoteUserInJira(user);
        try
        {
            RemoteNamedObject[] actions = null;
            try
            {
                Map map = issueUtilsBean.loadAvailableActions(issueRetriever.retrieveIssue(issueKey, user).getIssue());
                if (map != null && !map.isEmpty())
                {
                    actions = new RemoteNamedObject[map.size()];
                    Set entries = map.entrySet();
                    int i = 0;
                    for (Iterator iterator = entries.iterator(); iterator.hasNext();)
                    {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        Integer actionId = (Integer) entry.getKey();
                        ActionDescriptor action = (ActionDescriptor) entry.getValue();
                        actions[i] = new RemoteNamedObject(actionId.toString(), action.getName());
                        i++;
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("Error loading available actions", e);
            }
            return actions;
        }
        finally
        {
            setRemoteUserInJira(oldUser);
        }
    }

    public RemoteField[] getFieldsForAction(User user, String issueKey, String actionIdString) throws RemoteException
    {
        User oldUser = setRemoteUserInJira(user);
        try
        {
            MutableIssue issue = (MutableIssue) issueRetriever.retrieveIssue(issueKey, user).getIssue();
            WorkflowTransitionUtil workflowTransitionUtil = getWorkflowTransitionUtil(issue, actionIdString);

            List fields = new ArrayList();
            if (workflowTransitionUtil.hasScreen())
            {
                for (Iterator iterator = workflowTransitionUtil.getFieldScreenRenderer().getFieldScreenRenderTabs().iterator(); iterator.hasNext();)
                {
                    FieldScreenRenderTab fieldScreenRenderTab = (FieldScreenRenderTab) iterator.next();
                    for (Iterator iterator1 = fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing().iterator(); iterator1.hasNext();)
                    {
                        FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) iterator1.next();
                        if (fieldScreenRenderLayoutItem.isShow(issue))
                        {
                            OrderableField field = fieldScreenRenderLayoutItem.getOrderableField();
                            fields.add(field);
                        }
                    }
                }
            }
            return soapUtilsBean.convertFieldsToRemoteFields(fields);
        }
        finally
        {
            setRemoteUserInJira(oldUser);
        }

    }

    public RemoteIssue progressWorkflowAction(User user, String issueKey, String actionIdString, RemoteFieldValue[] actionParams)
            throws RemoteException
    {
        return progressWorkflowAction(user, issueKey, actionIdString, soapUtilsBean.mapFieldValueToMap(actionParams));
    }

    public RemoteIssue getIssueById(User user, String issueId) throws RemoteException, RemotePermissionException
    {
        //JRA-13712: Need to set the user into the authenticationContext, as it may be used by certain fields during
        //issue constructions.
        final User oldUser = setRemoteUserInJira(user);
        try
        {
            // Cast the issue id to a Long and pass to the retrieve issue function
            IssueRetriever.IssueInfo issueInfo = issueRetriever.retrieveIssue(new Long(issueId), user);
            return new RemoteIssue(issueInfo.getIssue(), issueInfo.getParentIssue(), customFieldManager, attachmentManager, soapUtilsBean);
        }
        finally
        {
            setRemoteUserInJira(oldUser);
        }
    }

    private RemoteIssue progressWorkflowAction(User user, String issueKey, String actionIdString, Map actionParams)
            throws RemoteException
    {
        User oldUser = setRemoteUserInJira(user);
        try
        {
            MutableIssue issue = (MutableIssue) issueRetriever.retrieveIssue(issueKey, user).getIssue();
            WorkflowTransitionUtil workflowTransitionUtil = getWorkflowTransitionUtil(issue, actionIdString);

            // Validate params
            Map workflowTransitionParams = new HashMap();
            if (workflowTransitionUtil.hasScreen())
            {
                if (actionParams == null)
                {
                    actionParams = Collections.EMPTY_MAP;
                }

                for (Iterator iterator = workflowTransitionUtil.getFieldScreenRenderer().getFieldScreenRenderTabs().iterator(); iterator.hasNext();)
                {
                    FieldScreenRenderTab fieldScreenRenderTab = (FieldScreenRenderTab) iterator.next();
                    for (Iterator iterator1 = fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing().iterator(); iterator1.hasNext();)
                    {
                        FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) iterator1.next();
                        if (fieldScreenRenderLayoutItem.isShow(issue))
                        {
                            OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();

                            //JRA-16915: first popuplate the fieldvalues holder used for the transition from the value stored in the
                            //issue. This is so that existing values don't get lost.
                            orderableField.populateFromIssue(workflowTransitionParams, issue);

                            //Only override the fields value if something was submitted in the SOAP request for this field.
                            //Using the fieldId as the key into the actionParam map.  This is safe, since the id is always
                            //used to key the field in the parameter map (see implementations of
                            // {@link com.atlassian.jira.issue.fields.AbstractOrderableField#getRelevantParams(Map params)}).
                            if (actionParams.containsKey(orderableField.getId()))
                            {
                                // Then populate the field values holder with the action params submitted with the SOAP request
                                // this should overwrite any values on the issue with what the user has submitted.
                                orderableField.populateFromParams(workflowTransitionParams, actionParams);
                            }
                        }
                    }
                }


                workflowTransitionUtil.setParams(workflowTransitionParams);
                ErrorCollection errors = workflowTransitionUtil.validate();
                if (errors.hasAnyErrors())
                {
                    throw new RemoteValidationException("Fields not valid for workflow action " + workflowTransitionUtil.getActionDescriptor().getName() + ": \n" + errors);
                }
            }

            // Execute the workflow action
            ErrorCollection errors = workflowTransitionUtil.progress();
            if (errors.hasAnyErrors())
            {
                throw new RemoteException("Error occurred when running workflow action " + workflowTransitionUtil.getActionDescriptor().getName() + ": \n" + errors);
            }
            return getIssue(user, issueKey);
        }
        finally
        {
            setRemoteUserInJira(oldUser);
        }
    }

    public boolean addAttachmentsToIssue(User user, String issueKey, String[] fileNames, byte[][] attachments)
            throws RemoteException
    {
        Issue issue = issueRetriever.retrieveIssue(issueKey, user).getIssue();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, errorCollection);

        if (!attachmentService.canCreateAttachments(serviceContext, issue))
        {
            if (errorCollection.hasAnyErrors())
            {
                throw new RemoteValidationException("Can not create attachments.", errorCollection);
            }

            throw new RemotePermissionException("You do not have permission to create attachments, or attachments are not enabled in JIRA.");
        }

        if (attachments != null && fileNames != null)
        {
            if (attachments.length != fileNames.length)
            {
                throw new RemoteValidationException("Number of attachments (" + attachments.length + ") must match " +
                        "the number of names (" + fileNames.length + ") passed.");
            }

            List changeItemBeans = new ArrayList();
            for (int i = 0; i < attachments.length; i++)
            {
                byte[] attachment = attachments[i];
                String fileName = fileNames[i];
                try
                {
                    File tempFile = attachmentHelper.byteArrayToTempFile(attachment);
                    changeItemBeans.add(attachmentManager.createAttachment(tempFile, fileName, GENERIC_CONTENT_TYPE, user, issue.getGenericValue()));
                }
                catch (GenericEntityException e)
                {
                    final String errorMsg = "Unable to persist file: " + fileName;
                    log.warn(errorMsg, e);
                    throw new RemoteException(errorMsg, e);
                }
                catch (AttachmentException e)
                {
                    final String errorMsg = "Unable to attach file '" + fileName + "' through the SOAP interface";
                    log.warn(errorMsg, e);
                    throw new RemoteException(errorMsg, e);
                }
            }
            // if there are any change items, then update the issues change history
            if (!changeItemBeans.isEmpty())
            {
                try
                {
                    IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issue.getGenericValue(), issue.getGenericValue(), EventType.ISSUE_UPDATED_ID, user);
                    issueUpdateBean.setChangeItems(changeItemBeans);
                    issueUpdateBean.setDispatchEvent(true);
                    issueUpdateBean.setParams(EasyMap.build("eventsource", IssueEventSource.ACTION));
                    issueUpdater.doUpdate(issueUpdateBean, true);
                }
                catch (JiraException e)
                {
                    log.warn("Unable to update issues change history for attached file(s)");
                    throw new RemoteException("Unable to add change item for attached file.", e);
                }
            }
        }
        else
        {
            throw new RemoteException("Attachments / attachment names not specified");
        }

        return true;
    }


    public RemoteAttachment[] getAttachmentsFromIssue(User user, String issueKey) throws RemoteException
    {
        Issue issue = issueRetriever.retrieveIssue(issueKey, user).getIssue();
        return SoapUtils.getAttachments(attachmentManager.getAttachments(issue));
    }

    /**
     * Attaches files using the method outlined in Fear of attachments at http://www.iseran.com/Steve/papers/fear-of-attachments.pdf
     * and http://marc.theaimsgroup.com/?l=axis-user&m=109103923030924&w=2
     *
     * @return boolean
     */
    private boolean addAttachmentToIssueFromMimeAttachments(User user, String[] fileNames, GenericValue issueToAttach)
            throws RemoteException, RemotePermissionException
    {
        try
        {
            File[] files = attachmentHelper.saveFile(fileNames);

            for (int i = 0; i < files.length; i++)
            {
                File file = files[i];
                attachmentManager.createAttachment(file,
                        file.getName(),
                        GENERIC_CONTENT_TYPE,
                        user,
                        issueToAttach);
            }

        }
        catch (Exception e)
        {
            log.warn("Unable to attach files through the SOAP interface", e);
            throw new RemoteException("Unable to attach files.", e);
        }

        return true;
    }


    I18nHelper getI18nHelper()
    {
        return authenticationContext.getI18nHelper();
    }

    private WorkflowTransitionUtil getWorkflowTransitionUtil(MutableIssue issue, String actionIdString)
            throws RemoteException
    {
        try
        {
            int actionId = Integer.parseInt(actionIdString);
            WorkflowTransitionUtil workflowTransitionUtil = (WorkflowTransitionUtil) JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
            workflowTransitionUtil.setIssue(issue);
            workflowTransitionUtil.setAction(actionId);

            workflowTransitionUtil.getActionDescriptor();

            return workflowTransitionUtil;
        }
        catch (NumberFormatException e)
        {
            throw new RemoteException(e);
        }
        catch (IllegalArgumentException e)
        {
            throw new RemoteException(e);
        }
    }

    /**
     * A wrapper to call through to the {@link com.atlassian.jira.bc.issue.worklog.WorklogService} to validate and
     * create a new worklog entry
     *
     * @param user                 the user in play
     * @param issueKey             the key of the issue in play
     * @param remoteWorklog        the remote worklog data
     * @param newRemainingEstimate a new remaning extimate
     * @return Created worklog with the id set or null if no worklog was created.
     * @throws RemoteException           if anything goes wrong in general
     * @throws RemotePermissionException if the user does not have permission to adda worklog
     * @throws RemoteValidationException if the data does not pass validation
     */
    public RemoteWorklog addWorklogWithNewRemainingEstimate(final User user, final String issueKey, final RemoteWorklog remoteWorklog, final String newRemainingEstimate)
            throws RemoteException, RemotePermissionException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        Issue issue = getIssueFromKey(issueKey);

        String timeSpent = remoteWorklog.getTimeSpent();
        Date startDate = remoteWorklog.getStartDate();
        String comment = remoteWorklog.getComment();
        String groupLevel = remoteWorklog.getGroupLevel();
        String roleLevelId = remoteWorklog.getRoleLevelId();

        WorklogNewEstimateInputParameters params = WorklogInputParametersImpl
                .issue(issue)
                .timeSpent(timeSpent)
                .startDate(startDate)
                .comment(comment)
                .groupLevel(groupLevel)
                .roleLevelId(roleLevelId)
                .newEstimate(newRemainingEstimate)
                .buildNewEstimate();
        WorklogNewEstimateResult worklogResult = worklogService.validateCreateWithNewEstimate(serviceContext, params);
        checkAndThrowValidationException(serviceContext.getErrorCollection());
        if (worklogResult == null)
        {
            throw new RemoteValidationException(getI18nHelper().getText("error.unexpected.condition", "WorklogService.validateCreateWithNewEstimate"));
        }
        Worklog createdWorklog = worklogService.createWithNewRemainingEstimate(serviceContext, worklogResult, true);
        checkAndThrowRemoteException(serviceContext.getErrorCollection());
        return RemoteWorklogImpl.copyToRemoteWorkLog(createdWorklog, jiraDurationUtils);
    }

    public RemoteWorklog addWorklogAndAutoAdjustRemainingEstimate(final User user, final String issueKey, final RemoteWorklog remoteWorklog)
            throws RemoteException, RemotePermissionException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());

        Issue issue = getIssueFromKey(issueKey);
        String timeSpent = remoteWorklog.getTimeSpent();
        Date startDate = remoteWorklog.getStartDate();
        String comment = remoteWorklog.getComment();
        String groupLevel = remoteWorklog.getGroupLevel();
        String roleLevelId = remoteWorklog.getRoleLevelId();

        WorklogInputParameters params = WorklogInputParametersImpl
                .issue(issue)
                .timeSpent(timeSpent)
                .startDate(startDate)
                .comment(comment)
                .groupLevel(groupLevel)
                .roleLevelId(roleLevelId)
                .build();
        WorklogResult worklogResult = worklogService.validateCreate(serviceContext, params);
        checkAndThrowValidationException(serviceContext.getErrorCollection());
        if (worklogResult == null)
        {
            throw new RemoteValidationException(getI18nHelper().getText("error.unexpected.condition", "WorklogService.validateCreate"));
        }
        Worklog createdWorklog = worklogService.createAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);
        checkAndThrowRemoteException(serviceContext.getErrorCollection());
        return RemoteWorklogImpl.copyToRemoteWorkLog(createdWorklog, jiraDurationUtils);
    }

    public RemoteWorklog addWorklogAndRetainRemainingEstimate(final User user, final String issueKey, final RemoteWorklog remoteWorklog)
            throws RemoteException, RemotePermissionException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());

        Issue issue = getIssueFromKey(issueKey);
        String timeSpent = remoteWorklog.getTimeSpent();
        Date startDate = remoteWorklog.getStartDate();
        String comment = remoteWorklog.getComment();
        String groupLevel = remoteWorklog.getGroupLevel();
        String roleLevelId = remoteWorklog.getRoleLevelId();

        WorklogInputParameters params = WorklogInputParametersImpl
                .issue(issue)
                .timeSpent(timeSpent)
                .startDate(startDate)
                .comment(comment)
                .groupLevel(groupLevel)
                .roleLevelId(roleLevelId)
                .build();
        WorklogResult worklogResult = worklogService.validateCreate(serviceContext, params);
        checkAndThrowValidationException(serviceContext.getErrorCollection());
        if (worklogResult == null)
        {
            throw new RemoteValidationException(getI18nHelper().getText("error.unexpected.condition", "WorklogService.validateCreate"));
        }
        Worklog createdWorklog = worklogService.createAndRetainRemainingEstimate(serviceContext, worklogResult, true);
        checkAndThrowRemoteException(serviceContext.getErrorCollection());
        return RemoteWorklogImpl.copyToRemoteWorkLog(createdWorklog, jiraDurationUtils);
    }

    public void deleteWorklogAndAutoAdjustRemainingEstimate(User user, String remoteWorklogId)
            throws RemoteException, RemotePermissionException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        Long id = SoapUtils.toLongRequired(remoteWorklogId);
        WorklogResult worklogResult = worklogService.validateDelete(serviceContext, id);
        checkAndThrowValidationException(serviceContext.getErrorCollection());
        if (worklogResult == null)
        {
            throw new RemoteValidationException(getI18nHelper().getText("error.unexpected.condition", "WorklogService.validateDelete"));
        }
        worklogService.deleteAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);
        checkAndThrowRemoteException(serviceContext.getErrorCollection());
    }

    public void deleteWorklogAndRetainRemainingEstimate(User user, String remoteWorklogId)
            throws RemoteException, RemotePermissionException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        Long id = SoapUtils.toLongRequired(remoteWorklogId);
        WorklogResult worklogResult = worklogService.validateDelete(serviceContext, id);
        checkAndThrowValidationException(serviceContext.getErrorCollection());
        if (worklogResult == null)
        {
            throw new RemoteValidationException(getI18nHelper().getText("error.unexpected.condition", "WorklogService.validateDelete"));
        }
        worklogService.deleteAndRetainRemainingEstimate(serviceContext, worklogResult, true);
        checkAndThrowRemoteException(serviceContext.getErrorCollection());
    }

    public void deleteWorklogWithNewRemainingEstimate(User user, String remoteWorklogId, String newRemainingEstimate)
            throws RemoteException, RemotePermissionException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        Long id = SoapUtils.toLongRequired(remoteWorklogId);
        WorklogNewEstimateResult worklogResult = worklogService.validateDeleteWithNewEstimate(serviceContext, id, newRemainingEstimate);
        checkAndThrowValidationException(serviceContext.getErrorCollection());
        if (worklogResult == null)
        {
            throw new RemoteValidationException(getI18nHelper().getText("error.unexpected.condition", "WorklogService.validateDeleteWithNewEstimate"));
        }
        worklogService.deleteWithNewRemainingEstimate(serviceContext, worklogResult, true);
        checkAndThrowRemoteException(serviceContext.getErrorCollection());
    }

    public void updateWorklogAndAutoAdjustRemainingEstimate(User user, RemoteWorklog remoteWorklog)
            throws RemoteException, RemotePermissionException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        Long id = SoapUtils.toLongRequired(remoteWorklog.getId());
        String timeSpent = remoteWorklog.getTimeSpent();
        Date startDate = remoteWorklog.getStartDate();
        String comment = remoteWorklog.getComment();
        String groupLevel = remoteWorklog.getGroupLevel();
        String roleLevelId = remoteWorklog.getRoleLevelId();

        final WorklogInputParameters params = WorklogInputParametersImpl
                .timeSpent(timeSpent)
                .worklogId(id)
                .startDate(startDate)
                .comment(comment)
                .groupLevel(groupLevel)
                .roleLevelId(roleLevelId)
                .build();
        WorklogResult worklogResult = worklogService.validateUpdate(serviceContext, params);
        checkAndThrowValidationException(serviceContext.getErrorCollection());
        if (worklogResult == null)
        {
            throw new RemoteValidationException(getI18nHelper().getText("error.unexpected.condition", "WorklogService.validateUpdate"));
        }
        worklogService.updateAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);
        checkAndThrowRemoteException(serviceContext.getErrorCollection());
    }

    public void updateWorklogAndRetainRemainingEstimate(User user, RemoteWorklog remoteWorklog)
            throws RemoteException, RemotePermissionException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        Long id = SoapUtils.toLongRequired(remoteWorklog.getId());
        String timeSpent = remoteWorklog.getTimeSpent();
        Date startDate = remoteWorklog.getStartDate();
        String comment = remoteWorklog.getComment();
        String groupLevel = remoteWorklog.getGroupLevel();
        String roleLevelId = remoteWorklog.getRoleLevelId();

        final WorklogInputParameters params = WorklogInputParametersImpl
                .timeSpent(timeSpent)
                .worklogId(id)
                .startDate(startDate)
                .comment(comment)
                .groupLevel(groupLevel)
                .roleLevelId(roleLevelId)
                .build();
        WorklogResult worklogResult = worklogService.validateUpdate(serviceContext, params);
        checkAndThrowValidationException(serviceContext.getErrorCollection());
        if (worklogResult == null)
        {
            throw new RemoteValidationException(getI18nHelper().getText("error.unexpected.condition", "WorklogService.validateUpdate"));
        }
        worklogService.updateAndRetainRemainingEstimate(serviceContext, worklogResult, true);
        checkAndThrowRemoteException(serviceContext.getErrorCollection());
    }

    public void updateWorklogWithNewRemainingEstimate(User user, RemoteWorklog remoteWorklog, String newRemainingEstimate)
            throws RemoteException, RemotePermissionException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        Long id = SoapUtils.toLongRequired(remoteWorklog.getId());
        String timeSpent = remoteWorklog.getTimeSpent();
        Date startDate = remoteWorklog.getStartDate();
        String comment = remoteWorklog.getComment();
        String groupLevel = remoteWorklog.getGroupLevel();
        String roleLevelId = remoteWorklog.getRoleLevelId();

        final WorklogNewEstimateInputParameters params = WorklogInputParametersImpl
                .timeSpent(timeSpent)
                .worklogId(id)
                .startDate(startDate)
                .comment(comment)
                .groupLevel(groupLevel)
                .roleLevelId(roleLevelId)
                .newEstimate(newRemainingEstimate)
                .buildNewEstimate();
        WorklogNewEstimateResult worklogResult = worklogService.validateUpdateWithNewEstimate(serviceContext, params);
        checkAndThrowValidationException(serviceContext.getErrorCollection());
        if (worklogResult == null)
        {
            throw new RemoteValidationException(getI18nHelper().getText("error.unexpected.condition", "WorklogService.validateUpdateWithNewEstimate"));
        }
        worklogService.updateWithNewRemainingEstimate(serviceContext, worklogResult, true);
        checkAndThrowRemoteException(serviceContext.getErrorCollection());

    }

    public RemoteWorklog[] getWorklogs(final User user, final String issueKey)
            throws RemoteException, RemotePermissionException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        Issue issue = getIssueFromKey(issueKey);
        List workLogs = worklogService.getByIssueVisibleToUser(serviceContext, issue);
        RemoteWorklog[] remoteWorkLogs = new RemoteWorklog[workLogs.size()];
        int i = 0;
        for (Iterator iterator = workLogs.iterator(); iterator.hasNext();)
        {
            Worklog worklog = (Worklog) iterator.next();
            remoteWorkLogs[i++] = RemoteWorklogImpl.copyToRemoteWorkLog(worklog, jiraDurationUtils);
        }
        return remoteWorkLogs;
    }

    public boolean hasPermissionToCreateWorklog(User user, String issueKey)
            throws RemoteException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        Issue issue = getIssueFromKey(issueKey);
        return worklogService.hasPermissionToCreate(serviceContext, issue, true);
    }

    public boolean hasPermissionToDeleteWorklog(User user, String worklogId)
            throws RemoteException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        Long id = SoapUtils.toLongRequired(worklogId);
        Worklog worklog = worklogService.getById(serviceContext, id);
        // getById does not currenty return errors but it might so we do this code
        checkAndThrowValidationException(serviceContext.getErrorCollection());
        return worklog != null && worklogService.hasPermissionToDelete(serviceContext, worklog);
    }

    public boolean hasPermissionToUpdateWorklog(User user, String worklogId)
            throws RemoteException, RemoteValidationException
    {
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        Long id = SoapUtils.toLongRequired(worklogId);
        Worklog worklog = worklogService.getById(serviceContext, id);
        // getById does not currenty return errors but it might so we do this code
        checkAndThrowValidationException(serviceContext.getErrorCollection());
        return worklog != null && worklogService.hasPermissionToUpdate(serviceContext, worklog);
    }

    /**
     * Format up a time spend Long into a JIRA time duration pretty string.
     *
     * @param timeSpentInSeconds - the time spend in seconds as a Long
     * @return a JIRA time duration.
     */
    String formatTimeDuration(long timeSpentInSeconds)
    {
        return jiraDurationUtils.getFormattedDuration(new Long(timeSpentInSeconds));
    }

    /**
     * Gets the issue.
     *
     * @param issueKey the issue key.
     * @return issue the Issue object.
     * @throws RemoteException if the issue doesn't exist.
     */
    Issue getIssueFromKey(String issueKey) throws RemoteException
    {
        Issue issue;
        try
        {
            issue = issueManager.getIssueObject(issueKey);
            if (issue == null)
            {
                throw new RemoteValidationException(getI18nHelper().getText("issue.does.not.exist.desc", issueKey));
            }
        }
        catch (DataAccessException dae)
        {
            throw new RemoteException(dae);
        }
        catch (RuntimeException rt)
        {
            throw new RemoteException(rt);
        }
        return issue;
    }

    public RemoteSecurityLevel getSecurityLevel(final User user, final String issueKey)
            throws RemoteException, RemotePermissionException
    {
        final Issue issue = issueManager.getIssueObject(issueKey);

        if (issue == null)
        {
            throw new RemotePermissionException("This issue does not exist.");
        }

        if (!permissionManager.hasPermission(Permissions.BROWSE, issue.getGenericValue(), user))
        {
            return null;
        }

        final GenericValue securityLevelGV = issue.getSecurityLevel();
        return securityLevelGV == null ? null : new RemoteSecurityLevel(securityLevelGV);
    }

    public Date getResolutionDateByKey(final User user, final String issueKey) throws RemoteException
    {
        final Issue issue = issueRetriever.retrieveIssue(issueKey, user).getIssue();
        return issue.getResolutionDate();
    }

    public Date getResolutionDateById(final User user, final Long issueId) throws RemoteException
    {
        final Issue issue = issueRetriever.retrieveIssue(issueId, user).getIssue();
        return issue.getResolutionDate();
    }

    /**
     * For backward compatibility, we allow SelectCFType/MultiSelectCFType fields to
     * accept both ids or values.
     *
     * For each of the supplied values if the option can parse to a long, matches an Option and
     * does not exist in the set of OptionValues, we have a list of OptionIds
     *
     */
    private String[] convertCustomFieldOptionValuesToOptionIds(final Options options, final String[] values)
    {
        final Map<String, Option> valueToOptionMap = Maps.newHashMap();
        for (final Object optionObj : options)
        {
            final Option option = (Option)optionObj;
            valueToOptionMap.put(option.getValue(), option);
        }

        final List<String> transformedOptions = Lists.newArrayList();

        boolean isListOfIds = true;

        for (final String value : values)
        {
            final Long optionId = OptionUtils.safeParseLong(value);
            final Option matchingOption = optionId != null ? options.getOptionById(optionId) : null;

            if(optionId == null || matchingOption == null)
            {
                // we do NOT have a list of ids
                isListOfIds = false;
            }

            // Since we're already going through the loop...
            final Option option = valueToOptionMap.get(value);
            if(option != null)
            {
                transformedOptions.add(option.getOptionId().toString());
            }
        }

        // If we didn't succeed in converting, return the original values so the service will handle the invalid values
        // and produce a suitable exception
        if(isListOfIds || (transformedOptions.size() != values.length))
        {
            return values;
        }
        else
        {
            return transformedOptions.toArray(values);
        }
    }

}
