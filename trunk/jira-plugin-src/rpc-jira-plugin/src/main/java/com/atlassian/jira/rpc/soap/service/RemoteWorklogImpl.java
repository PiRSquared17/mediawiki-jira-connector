package com.atlassian.jira.rpc.soap.service;

import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.rpc.soap.beans.RemoteWorklog;
import com.atlassian.jira.util.JiraDurationUtils;

import java.util.Date;

/**
 * A little PACKAGE LEVEL implementation of {@link com.atlassian.jira.rpc.soap.beans.RemoteWorklog} that allows the
 * read only constructor to be accessed. This way RemoteWorklog does not have setters for things that cant be set!
 */
class RemoteWorklogImpl extends RemoteWorklog
{
    RemoteWorklogImpl(String id, String author, String updateAuthor, Date created, Date updated, String timeSpent, long timeSpentInSeconds)
    {
        super(id, author, updateAuthor, created, updated, timeSpent, timeSpentInSeconds);
    }

    /**
     * Copies the local Worklog object into a RemoteWorklog object with the required semantics.
     *
     * @param worklog           the worklog to copy
     * @param jiraDurationUtils jira duration utils object to format time; must not be null
     * @return a RemoteWorklog that represents the Worklog to be copied; null if the original worklog was null
     */
    static RemoteWorklog copyToRemoteWorkLog(Worklog worklog, final JiraDurationUtils jiraDurationUtils)
    {
        if (worklog != null)
        {
            String id = worklog.getId() == null ? null : String.valueOf(worklog.getId());
            long timeSpentInSeconds = worklog.getTimeSpent().longValue();
            String timeSpentDuration = jiraDurationUtils.getFormattedDuration(new Long(timeSpentInSeconds));

            RemoteWorklog remoteWorklog = new RemoteWorklogImpl(id, worklog.getAuthor(), worklog.getUpdateAuthor(), worklog.getCreated(), worklog.getUpdated(), timeSpentDuration, timeSpentInSeconds);
            remoteWorklog.setComment(worklog.getComment());
            remoteWorklog.setTimeSpent(timeSpentDuration);
            remoteWorklog.setGroupLevel(worklog.getGroupLevel());
            remoteWorklog.setStartDate(worklog.getStartDate());
            remoteWorklog.setRoleLevelId(worklog.getRoleLevelId() == null ? null : String.valueOf(worklog.getRoleLevelId()));
            return remoteWorklog;
        }
        return null;
    }
}
