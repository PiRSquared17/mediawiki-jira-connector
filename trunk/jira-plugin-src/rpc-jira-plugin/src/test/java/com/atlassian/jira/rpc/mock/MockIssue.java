package com.atlassian.jira.rpc.mock;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.user.MockCrowdService;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO - this a  COPY AND PASTE hack.  We need to resolve this dependency somehow elsewhere
 */

public class MockIssue implements MutableIssue
{

    // Issue fields
    private Long id;
    private Long projectId;
    private String key;
    private String issueTypeId;
    private String summary;
    private String description;
    private String environment;
    private String assigneeId;
    private String reporterId;
    private Timestamp created;
    private Timestamp updated;
    private Timestamp dueDate;
    private Long securityLevelId;
    private String priorityId;
    private String resolutionId;
    private String statusId;
    private Long votes;
    private Long watches;
    private Long originalEstimate;
    private Long estimate;
    private Long timespent;
    private Long workflowId;
    private Long parentId;
    private GenericValue genericValue;
    private Set<Label> labels;

    private Map modifiedFields;
    private GenericValue project;
    private GenericValue issueType;
    private GenericValue resolution;
    private IssueType issueTypeObject;
    private com.opensymphony.user.User assignee;
    private Collection components;
    private com.opensymphony.user.User reporter;
    private Collection affectedVersions;
    private Resolution resolutionObject;
    private Collection fixVersions;
    private GenericValue securityLevel;
    private GenericValue priority;
    private GenericValue status;
    private Long timeSpent;
    private Status statusObject;
    private Timestamp resolutionDate;

    public MockIssue()
    {
        modifiedFields = new HashMap();
        long now = System.currentTimeMillis();
        created = new Timestamp(now);
        updated = new Timestamp(now);
        dueDate = new Timestamp(now);
    }

    /**
     * This constructor does not have any time precision set by default
     * user MockIssue(Long id, Long now) if you want to preset the Issue's time values.
     *
     * @param id
     */
    public MockIssue(Long id)
    {
        this(id, null);
    }

    /**
     * Use this constructor if you want time precision in your tests
     *
     * @param id
     * @param now
     */
    public MockIssue(Long id, Long now)
    {
        this.id = id;
        if (now != null)
        {
            created = new Timestamp(now.longValue());
            updated = new Timestamp(now.longValue());
            dueDate = new Timestamp(now.longValue());
        }
    }

    public Long getId()
    {
        return id;
    }

    public GenericValue getProject()
    {
        return project;
    }

    public Project getProjectObject()
    {
        return new ProjectImpl(project);
    }

    public void setProject(GenericValue project)
    {
        this.project = project;
    }

    public GenericValue getIssueType()
    {
        return issueType;
    }

    public IssueType getIssueTypeObject()
    {
        return issueTypeObject;
    }

    public void setIssueType(GenericValue issueType)
    {
        this.issueType = issueType;
    }

    public void setIssueTypeId(String issueTypeId)
    {
        this.issueTypeId = issueTypeId;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public com.opensymphony.user.User getAssignee()
    {
        return assignee;
    }

    public User getAssigneeUser()
    {
        return assignee;
    }

    public String getAssigneeId()
    {
        return null;
    }

    public void setAssignee(com.opensymphony.user.User assignee)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void setAssignee(final User user)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Collection getComponents()
    {
        return components;
    }

    public Collection getComponentObjects()
    {
        return null;
    }

    public void setComponents(Collection components)
    {
        this.components = components;
    }

    public void setAssigneeId(String assigneeId)
    {
        this.assigneeId = assigneeId;
    }

    public com.opensymphony.user.User getReporter()
    {
        return reporter;
    }

    public String getReporterId()
    {
        return reporterId;
    }

    public User getReporterUser()
    {
        return reporter;
    }

    public void setReporter(com.opensymphony.user.User reporter)
    {
        this.reporter = reporter;
    }

    public void setReporter(final User user)
    {
        this.reporter = new com.opensymphony.user.User(user, new MockCrowdService());
    }

    public void setReporterId(String reporterId)
    {
        this.reporterId = reporterId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(String environment)
    {
        this.environment = environment;
    }

    public Collection getAffectedVersions()
    {
        return affectedVersions;
    }

    public Collection getFixVersions()
    {
        return fixVersions;
    }

    public Timestamp getDueDate()
    {
        return dueDate;
    }

    public GenericValue getSecurityLevel()
    {
        return null;
    }

    public String getIssueTypeId()
    {
        return issueTypeId;
    }

    public String getPriorityId()
    {
        return priorityId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public String getResolutionId()
    {
        return resolutionId;
    }

    public Long getSecurityLevelId()
    {
        return securityLevelId;
    }

    public GenericValue getPriority()
    {
        return null;
    }

    public Priority getPriorityObject()
    {
        return null;
    }

    public String getStatusId()
    {
        return statusId;
    }

    public Long getTimespent()
    {
        return timespent;
    }

    public void setAffectedVersions(Collection affectedVersions)
    {
        this.affectedVersions = affectedVersions;
    }

    public void setFixVersions(Collection fixVersions)
    {
        this.fixVersions = fixVersions;
    }

    public void setDueDate(Timestamp dueDate)
    {
        this.dueDate = dueDate;
    }

    public void setSecurityLevel(GenericValue securityLevel)
    {
        this.securityLevel = securityLevel;
    }

    public void setPriority(GenericValue priority)
    {
        this.priority = priority;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public void setIssueTypeObject(IssueType issueTypeObject)
    {
        this.issueTypeObject = issueTypeObject;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public void setSecurityLevelId(Long securityLevelId)
    {
        this.securityLevelId = securityLevelId;
    }

    public void setTimespent(Long timespent)
    {
        this.timespent = timespent;
    }

    public void setPriorityId(String priorityId)
    {
        this.priorityId = priorityId;
    }

    public GenericValue getResolution()
    {
        return resolution;
    }

    public void setResolutionObject(Resolution resolutionObject)
    {
        this.resolutionObject = resolutionObject;
    }

    public Resolution getResolutionObject()
    {
        return resolutionObject;
    }

    public void setResolution(GenericValue resolution)
    {
        this.resolution = resolution;
    }

    public String getKey()
    {
        return this.key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public Long getVotes()
    {
        return votes;
    }

    public void setVotes(Long votes)
    {
        this.votes = votes;
    }

    public void setWatches(final Long watches)
    {
        this.watches = watches;
    }

    public Long getWatches()
    {
        return watches;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    public void setCreated(Timestamp created)
    {
        this.created = created;
    }

    public Timestamp getResolutionDate()
    {
        return resolutionDate;
    }

    public void setResolutionDate(Timestamp resolutionDate)
    {
        this.resolutionDate = resolutionDate;
    }

    public Timestamp getUpdated()
    {
        return updated;
    }

    public void setUpdated(Timestamp updated)
    {
        this.updated = updated;
    }

    public Long getWorkflowId()
    {
        return workflowId;
    }

    public Object getCustomFieldValue(CustomField customField)
    {
        return null;
    }

    public GenericValue getStatus()
    {
        return this.status;
    }

    public Status getStatusObject()
    {
        return this.statusObject;
    }

    public void setStatusObject(Status status)
    {
        this.statusObject = status;
    }

    public void setWorkflowId(Long workflowId)
    {
        this.workflowId = workflowId;
    }


    public void setStatusId(String statusId)
    {
        this.statusId = statusId;
    }

    public Map getModifiedFields()
    {
        return this.modifiedFields;
    }

    public void setModifiedFields(Map modifiedFields)
    {
        this.modifiedFields = modifiedFields;
    }


    public void resetModifiedFields()
    {
        modifiedFields.clear();
    }

    public boolean isSubTask()
    {
        return false;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void setParentObject(final Issue issue)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public boolean isCreated()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public GenericValue getParent()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Issue getParentObject()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Collection getSubTasks()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Collection getSubTaskObjects()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getString(String name)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Timestamp getTimestamp(String name)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Long getLong(String name)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public GenericValue getGenericValue()
    {
        if (genericValue == null)
        {
            return getHackedGVThatReturnsId();
        }
        return genericValue;
    }

    protected GenericValue getHackedGVThatReturnsId()
    {
        MockGenericValue horriblyHackedIssueGV = new MockGenericValue("Issue");
        horriblyHackedIssueGV.set("id", getId());
        return horriblyHackedIssueGV;
    }

    public void setGenericValue(GenericValue genericValue)
    {
        this.genericValue = genericValue;

        // adding these as necessary, if you need more, then add 'em...
        setId(genericValue.getLong("id"));
        setKey(genericValue.getString("key"));
        setProjectId(genericValue.getLong(IssueFieldConstants.PROJECT));
        setSummary(genericValue.getString(IssueFieldConstants.SUMMARY));
        setDescription(genericValue.getString(IssueFieldConstants.DESCRIPTION));
    }

    public void store()
    {

    }

    public void setResolutionId(String resolutionId)
    {
        this.resolutionId = resolutionId;
    }

    public void setLabels(final Set<Label> labels)
    {
        this.labels = labels;
    }

    public boolean isEditable()
    {
        return true;
    }

    public IssueRenderContext getIssueRenderContext()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Collection getAttachments()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Set<Label> getLabels()
    {
        return labels;
    }

    public void setCustomFieldValue(CustomField customField, Object value)
    {
    }

    public void setExternalFieldValue(String fieldId, Object value)
    {
    }

    public void setExternalFieldValue(final String fieldId, final Object oldValue, final Object newValue)
    {
    }

    public void setStatus(GenericValue status)
    {
        this.status = status;
    }

    public void setTimeSpent(Long timespent)
    {
        this.timeSpent = timespent;
    }

    public Long getEstimate()
    {
        return estimate;
    }

    public Long getTimeSpent()
    {
        return null;
    }

    public Object getExternalFieldValue(String fieldId)
    {
        return null;
    }

    public Long getOriginalEstimate()
    {
        return originalEstimate;
    }

    public void setEstimate(Long estimate)
    {
        this.estimate = estimate;
    }

    public void setOriginalEstimate(Long originalEstimate)
    {
        this.originalEstimate = originalEstimate;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final MockIssue issue = (MockIssue) o;

        if (affectedVersions != null ? !affectedVersions.equals(issue.affectedVersions) : issue.affectedVersions != null)
        {
            return false;
        }
        if (assignee != null ? !assignee.equals(issue.assignee) : issue.assignee != null)
        {
            return false;
        }
        if (assigneeId != null ? !assigneeId.equals(issue.assigneeId) : issue.assigneeId != null)
        {
            return false;
        }
        if (components != null ? !components.equals(issue.components) : issue.components != null)
        {
            return false;
        }
        if (created != null ? !created.equals(issue.created) : issue.created != null)
        {
            return false;
        }
        if (description != null ? !description.equals(issue.description) : issue.description != null)
        {
            return false;
        }
        if (dueDate != null ? !dueDate.equals(issue.dueDate) : issue.dueDate != null)
        {
            return false;
        }
        if (environment != null ? !environment.equals(issue.environment) : issue.environment != null)
        {
            return false;
        }
        if (estimate != null ? !estimate.equals(issue.estimate) : issue.estimate != null)
        {
            return false;
        }
        if (fixVersions != null ? !fixVersions.equals(issue.fixVersions) : issue.fixVersions != null)
        {
            return false;
        }
        if (genericValue != null ? !genericValue.equals(issue.genericValue) : issue.genericValue != null)
        {
            return false;
        }
        if (!id.equals(issue.id))
        {
            return false;
        }
        if (issueType != null ? !issueType.equals(issue.issueType) : issue.issueType != null)
        {
            return false;
        }
        if (issueTypeId != null ? !issueTypeId.equals(issue.issueTypeId) : issue.issueTypeId != null)
        {
            return false;
        }
        if (issueTypeObject != null ? !issueTypeObject.equals(issue.issueTypeObject) : issue.issueTypeObject != null)
        {
            return false;
        }
        if (key != null ? !key.equals(issue.key) : issue.key != null)
        {
            return false;
        }
        if (originalEstimate != null ? !originalEstimate.equals(issue.originalEstimate) : issue.originalEstimate != null)
        {
            return false;
        }
        if (parentId != null ? !parentId.equals(issue.parentId) : issue.parentId != null)
        {
            return false;
        }
        if (priority != null ? !priority.equals(issue.priority) : issue.priority != null)
        {
            return false;
        }
        if (priorityId != null ? !priorityId.equals(issue.priorityId) : issue.priorityId != null)
        {
            return false;
        }
        if (project != null ? !project.equals(issue.project) : issue.project != null)
        {
            return false;
        }
        if (projectId != null ? !projectId.equals(issue.projectId) : issue.projectId != null)
        {
            return false;
        }
        if (reporter != null ? !reporter.equals(issue.reporter) : issue.reporter != null)
        {
            return false;
        }
        if (reporterId != null ? !reporterId.equals(issue.reporterId) : issue.reporterId != null)
        {
            return false;
        }
        if (resolution != null ? !resolution.equals(issue.resolution) : issue.resolution != null)
        {
            return false;
        }
        if (resolutionId != null ? !resolutionId.equals(issue.resolutionId) : issue.resolutionId != null)
        {
            return false;
        }
        if (resolutionObject != null ? !resolutionObject.equals(issue.resolutionObject) : issue.resolutionObject != null)
        {
            return false;
        }
        if (securityLevel != null ? !securityLevel.equals(issue.securityLevel) : issue.securityLevel != null)
        {
            return false;
        }
        if (securityLevelId != null ? !securityLevelId.equals(issue.securityLevelId) : issue.securityLevelId != null)
        {
            return false;
        }
        if (status != null ? !status.equals(issue.status) : issue.status != null)
        {
            return false;
        }
        if (statusId != null ? !statusId.equals(issue.statusId) : issue.statusId != null)
        {
            return false;
        }
        if (statusObject != null ? !statusObject.equals(issue.statusObject) : issue.statusObject != null)
        {
            return false;
        }
        if (summary != null ? !summary.equals(issue.summary) : issue.summary != null)
        {
            return false;
        }
        if (timeSpent != null ? !timeSpent.equals(issue.timeSpent) : issue.timeSpent != null)
        {
            return false;
        }
        if (timespent != null ? !timespent.equals(issue.timespent) : issue.timespent != null)
        {
            return false;
        }
        if (updated != null ? !updated.equals(issue.updated) : issue.updated != null)
        {
            return false;
        }
        if (votes != null ? !votes.equals(issue.votes) : issue.votes != null)
        {
            return false;
        }
        if (labels != null ? !labels.equals(issue.labels) : issue.labels != null)
        {
            return false;
        }
        if (workflowId != null ? !workflowId.equals(issue.workflowId) : issue.workflowId != null)
        {
            return false;
        }

        return true;
    }
}
