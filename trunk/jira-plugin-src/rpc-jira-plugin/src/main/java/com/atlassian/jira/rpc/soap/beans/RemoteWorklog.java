package com.atlassian.jira.rpc.soap.beans;

import java.util.Date;

/**
 * Remote type for {@link com.atlassian.jira.issue.worklog.Worklog} of an Issue.
 */
public class RemoteWorklog
{
    final private String author;
    final private String updateAuthor;
    final private Date created;
    final private Date updated;
    final private long timeSpentInSeconds;

    private String id;
    private String comment;
    private String groupLevel;
    private String roleLevelId;
    private Date startDate;
    private String timeSpent;


    /**
     * A protected contructor to allow JIRA code to set the read only and special fields.
     */
    protected RemoteWorklog(String id, String author, String updateAuthor, Date created, Date updated, String timeSpent, long timeSpentInSeconds)
    {
        this.id = id;
        this.author = author;
        this.updateAuthor = updateAuthor;
        this.created = created;
        this.updated = updated;
        this.timeSpent = timeSpent;
        this.timeSpentInSeconds = timeSpentInSeconds;
    }


    /**
     * Contructs a RemoteWorklog with empty fields.
     */
    public RemoteWorklog()
    {
        this.id = null;
        this.author = null;
        this.updateAuthor = null;
        this.created = null;
        this.updated = null;
        this.timeSpentInSeconds = 0;
    }

    /**
     * Contructs a RemoteWorklog with empty fields except the worklog id
     *
     * @param id the id of the worklog
     */
    public RemoteWorklog(String id)
    {
        this();
        this.id = id;
    }

    /**
     * @return the time duration in JIRA duration format, representing the time spent working on the worklog
     */
    public String getTimeSpent()
    {
        return timeSpent;
    }

    /**
     * Gets the time spent working on a worklog in seconds.
     *
     * @return the time spent working on a worklog in seconds.
     */
    public long getTimeSpentInSeconds()
    {
        return timeSpentInSeconds;
    }

    /**
     * Specifies a time duration in JIRA duration format, representing the time spent working on the worklog
     * <p/>
     * NOTES : JIRA time durations are in the following format
     * <p/>
     * <ul>
     * <li>minutes - eg 3m, 10m, 120m </li>
     * <li>hours - eg 3h, 10h, 120h </li>
     * <li>days - eg 3d, 10d, 120d </li>
     * <li>weeks - eg 3w, 10w, 120w </li>
     * </ul>
     *
     * @param timeSpent time duration in JIRA duration format
     */
    public void setTimeSpent(String timeSpent)
    {
        this.timeSpent = timeSpent;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public Date getCreated()
    {
        return created;
    }

    public String getGroupLevel()
    {
        return groupLevel;
    }

    public void setGroupLevel(String groupLevel)
    {
        this.groupLevel = groupLevel;
    }


    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }


    public String getRoleLevelId()
    {
        return roleLevelId;
    }

    public void setRoleLevelId(String roleLevelId)
    {
        this.roleLevelId = roleLevelId;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }


    public String getUpdateAuthor()
    {
        return updateAuthor;
    }

    public Date getUpdated()
    {
        return updated;
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

        RemoteWorklog that = (RemoteWorklog) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (author != null ? !author.equals(that.author) : that.author != null)
        {
            return false;
        }
        if (comment != null ? !comment.equals(that.comment) : that.comment != null)
        {
            return false;
        }
        if (created != null ? !created.equals(that.created) : that.created != null)
        {
            return false;
        }
        if (groupLevel != null ? !groupLevel.equals(that.groupLevel) : that.groupLevel != null)
        {
            return false;
        }
        if (roleLevelId != null ? !roleLevelId.equals(that.roleLevelId) : that.roleLevelId != null)
        {
            return false;
        }
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null)
        {
            return false;
        }
        if (timeSpent != null ? !timeSpent.equals(that.timeSpent) : that.timeSpent != null)
        {
            return false;
        }
        if (updateAuthor != null ? !updateAuthor.equals(that.updateAuthor) : that.updateAuthor != null)
        {
            return false;
        }
        if (updated != null ? !updated.equals(that.updated) : that.updated != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (updateAuthor != null ? updateAuthor.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (groupLevel != null ? groupLevel.hashCode() : 0);
        result = 31 * result + (roleLevelId != null ? roleLevelId.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (updated != null ? updated.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (timeSpent != null ? timeSpent.hashCode() : 0);
        return result;
    }

}
