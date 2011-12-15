/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 9, 2004
 * Time: 12:23:38 PM
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutStorageException;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.soap.util.SoapUtils;
import com.atlassian.jira.rpc.soap.util.SoapUtilsBean;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class RemoteIssue extends AbstractRemoteEntity
{
    private String key;
    private String summary;
    private String reporter;
    private String assignee;
    private String description;
    private String environment;
    private String project;
    //
    // this was added as part of some SOAP extension work in 2011.  But turns out you cant add bean properties
    // in SOAP safely.  So we dont expose it to the world even though we take it as a parameter.  Maybe one day
    // in an ideal world this will make a comeback.
    //
    private String parentKey;
    private String type;
    private String status;
    private String priority;
    private String resolution;
    private Date created;
    private Date updated;
    private Date duedate;
    private Long votes;
    private RemoteComponent[] components;
    private RemoteVersion[] affectsVersions;
    private RemoteVersion[] fixVersions;
    private RemoteCustomFieldValue[] customFieldValues;
    private String[] attachmentNames;

    public RemoteIssue()
    {
    }

    public RemoteIssue(Issue issue, Issue parentIssue, CustomFieldManager customFieldManager, AttachmentManager attachmentManager, SoapUtilsBean soapUtils) throws RemoteException
    {
        super(issue.getId().toString());
        try
        {
            this.key = issue.getKey();
            // we only do this here as JiraKeyUtils requires lots of ugly imports for one method
            // and we already know key is a valid issue key - slight hack.
            this.project = key.substring(0, key.lastIndexOf("-"));

            if (parentIssue != null)
            {
                this.parentKey = parentIssue.getKey();
            }

            if (soapUtils.isVisible(issue, IssueFieldConstants.SUMMARY))
            {
                this.summary = issue.getSummary();
            }

            if (soapUtils.isVisible(issue, IssueFieldConstants.DESCRIPTION))
            {
                this.description = issue.getDescription();
            }

            if (soapUtils.isVisible(issue, IssueFieldConstants.ENVIRONMENT))
            {
                this.environment = issue.getEnvironment();
            }

            if (soapUtils.isVisible(issue, IssueFieldConstants.REPORTER))
            {
                try
                {
                    this.reporter = issue.getReporter() != null ? issue.getReporter().getName() : null;
                }
                catch (Exception ex)
                {
                    // JRA-15399. If the user has been deleted, then we just show the username.
                    this.reporter = issue.getReporterId();
                }
            }

            if (soapUtils.isVisible(issue, IssueFieldConstants.ASSIGNEE))
            {
                try
                {
                    this.assignee = issue.getAssignee() != null ? issue.getAssignee().getName() : null;
                }
                catch (Exception ex)
                {
                    // JRA-15399. If the user has been deleted, then we just show the username.
                    this.assignee = issue.getAssigneeId();
                }
            }

            this.votes = issue.getVotes();

            if (soapUtils.isVisible(issue, IssueFieldConstants.ISSUE_TYPE))
            {
                this.type = getId(issue.getIssueTypeObject());
            }

            this.status = issue.getStatusObject().getId();

            if (soapUtils.isVisible(issue, IssueFieldConstants.PRIORITY))
            {
                this.priority = getId(issue.getPriorityObject());
            }

            this.resolution = getId(issue.getResolutionObject());
            this.created = issue.getCreated();
            this.updated = issue.getUpdated();

            this.duedate = issue.getDueDate();

            this.affectsVersions = SoapUtils.getVersions(issue.getAffectedVersions());
            this.fixVersions = SoapUtils.getVersions(issue.getFixVersions());
            this.components = SoapUtils.getComponents(issue.getComponents());

            // Deal with custom field values
            List customFields = customFieldManager.getCustomFieldObjects(issue.getProject().getLong("id"), issue.getIssueTypeObject().getId());
            if (customFields != null)
            {
                this.customFieldValues = SoapUtils.getCustomFieldValues(customFields, issue);
            }

            List attachments = attachmentManager.getAttachments(issue);
            this.attachmentNames = new String[attachments.size()];
            int i = 0;
            for (Iterator iterator = attachments.iterator(); iterator.hasNext(); i++)
            {
                Attachment attachment = (Attachment) iterator.next();
                this.attachmentNames[i] = attachment.getFilename();
            }
        }
        catch (FieldLayoutStorageException e)
        {
            throw new RemoteException("Error creating issue: " + e, e);
        }
    }

    private String getId(IssueConstant issueConstant)
    {
        return issueConstant != null ? issueConstant.getId() : null;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getAssignee()
    {
        return assignee;
    }

    public void setAssignee(String assignee)
    {
        this.assignee = assignee;
    }

    public Date getCreated()
    {
        return created;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Date getDuedate()
    {
        return duedate;
    }

    public void setDuedate(Date duedate)
    {
        this.duedate = duedate;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(String environment)
    {
        this.environment = environment;
    }

    public String getKey()
    {
        return key;
    }

    public String getPriority()
    {
        return priority;
    }

    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    public String getProject()
    {
        return project;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    public String getReporter()
    {
        return reporter;
    }

    public void setReporter(String reporter)
    {
        this.reporter = reporter;
    }

    public String getResolution()
    {
        return resolution;
    }

    public String getStatus()
    {
        return status;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Date getUpdated()
    {
        return updated;
    }

    public void setUpdated(Date updated)
    {
        this.updated = updated;
    }

    public Long getVotes()
    {
        return votes;
    }

    public RemoteVersion[] getAffectsVersions()
    {
        return affectsVersions;
    }

    public void setAffectsVersions(RemoteVersion[] affectsVersions)
    {
        this.affectsVersions = affectsVersions;
    }

    public RemoteComponent[] getComponents()
    {
        return components;
    }

    public void setComponents(RemoteComponent[] components)
    {
        this.components = components;
    }

    public RemoteVersion[] getFixVersions()
    {
        return fixVersions;
    }

    public void setFixVersions(RemoteVersion[] fixVersions)
    {
        this.fixVersions = fixVersions;
    }

    public RemoteCustomFieldValue[] getCustomFieldValues()
    {
        return customFieldValues;
    }

    public void setCustomFieldValues(RemoteCustomFieldValue[] customFieldValues)
    {
        this.customFieldValues = customFieldValues;
    }

    public String[] getAttachmentNames()
    {
        return attachmentNames;
    }

    public void setAttachmentNames(String[] attachmentNames)
    {
        this.attachmentNames = attachmentNames;
    }

    ///CLOVER:OFF
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RemoteIssue))
        {
            return false;
        }

        final RemoteIssue remoteIssue = (RemoteIssue) o;

        if (!Arrays.equals(affectsVersions, remoteIssue.affectsVersions))
        {
            return false;
        }
        if (assignee != null ? !assignee.equals(remoteIssue.assignee) : remoteIssue.assignee != null)
        {
            return false;
        }
        if (!Arrays.equals(components, remoteIssue.components))
        {
            return false;
        }
        if (created != null ? !created.equals(remoteIssue.created) : remoteIssue.created != null)
        {
            return false;
        }
        if (description != null ? !description.equals(remoteIssue.description) : remoteIssue.description != null)
        {
            return false;
        }
        if (duedate != null ? !duedate.equals(remoteIssue.duedate) : remoteIssue.duedate != null)
        {
            return false;
        }
        if (environment != null ? !environment.equals(remoteIssue.environment) : remoteIssue.environment != null)
        {
            return false;
        }
        if (!Arrays.equals(fixVersions, remoteIssue.fixVersions))
        {
            return false;
        }
        if (!Arrays.equals(customFieldValues, remoteIssue.customFieldValues))
        {
            return false;
        }
        if (!Arrays.equals(attachmentNames, remoteIssue.attachmentNames))
        {
            return false;
        }
        if (key != null ? !key.equals(remoteIssue.key) : remoteIssue.key != null)
        {
            return false;
        }
        if (priority != null ? !priority.equals(remoteIssue.priority) : remoteIssue.priority != null)
        {
            return false;
        }
        if (parentKey != null ? !parentKey.equals(remoteIssue.parentKey) : remoteIssue.parentKey != null)
        {
            return false;
        }
        if (project != null ? !project.equals(remoteIssue.project) : remoteIssue.project != null)
        {
            return false;
        }
        if (reporter != null ? !reporter.equals(remoteIssue.reporter) : remoteIssue.reporter != null)
        {
            return false;
        }
        if (resolution != null ? !resolution.equals(remoteIssue.resolution) : remoteIssue.resolution != null)
        {
            return false;
        }
        if (status != null ? !status.equals(remoteIssue.status) : remoteIssue.status != null)
        {
            return false;
        }
        if (summary != null ? !summary.equals(remoteIssue.summary) : remoteIssue.summary != null)
        {
            return false;
        }
        if (type != null ? !type.equals(remoteIssue.type) : remoteIssue.type != null)
        {
            return false;
        }
        if (updated != null ? !updated.equals(remoteIssue.updated) : remoteIssue.updated != null)
        {
            return false;
        }
        if (votes != null ? !votes.equals(remoteIssue.votes) : remoteIssue.votes != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (key != null ? key.hashCode() : 0);
        result = 29 * result + (summary != null ? summary.hashCode() : 0);
        result = 29 * result + (reporter != null ? reporter.hashCode() : 0);
        result = 29 * result + (assignee != null ? assignee.hashCode() : 0);
        result = 29 * result + (description != null ? description.hashCode() : 0);
        result = 29 * result + (environment != null ? environment.hashCode() : 0);
        result = 29 * result + (parentKey != null ? parentKey.hashCode() : 0);
        result = 29 * result + (project != null ? project.hashCode() : 0);
        result = 29 * result + (type != null ? type.hashCode() : 0);
        result = 29 * result + (status != null ? status.hashCode() : 0);
        result = 29 * result + (priority != null ? priority.hashCode() : 0);
        result = 29 * result + (resolution != null ? resolution.hashCode() : 0);
        result = 29 * result + (created != null ? created.hashCode() : 0);
        result = 29 * result + (updated != null ? updated.hashCode() : 0);
        result = 29 * result + (duedate != null ? duedate.hashCode() : 0);
        result = 29 * result + (votes != null ? votes.hashCode() : 0);
        return result;
    }
}
