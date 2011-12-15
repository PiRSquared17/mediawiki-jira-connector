package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

/**
 * RemoteConfiguration represents some of the configuration options within JIRA
 *
 * @author Brock Janiczak via JRA-8966 modified by Justin Koke
 */
public class RemoteConfiguration
{
    private boolean allowAttachments = false;
    private boolean allowUnassignedIssues = false;
    private boolean allowVoting = false;
    private boolean allowWatching = false;
    private boolean allowTimeTracking = false;
    private boolean allowSubTasks = false;
    private boolean allowIssueLinking = false;
    private boolean allowExternalUserManagment = false;
    private int timeTrackingHoursPerDay;
    private int timeTrackingDaysPerWeek;

    public RemoteConfiguration(ApplicationProperties applicationProperties)
    {
        allowAttachments = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS);
        allowVoting = applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING);
        allowWatching = applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING);
        allowUnassignedIssues = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
        allowTimeTracking = applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        allowSubTasks = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS);
        allowIssueLinking = applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        allowExternalUserManagment = applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);

        timeTrackingHoursPerDay = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));
        timeTrackingDaysPerWeek = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));
    }

    /**
     * @return the number of hours in a day as defined by JIRA time tracking configuration
     */
    public int getTimeTrackingHoursPerDay()
    {
        return timeTrackingHoursPerDay;
    }

    /**
     * @return the number of days in a week as defined by JIRA time tracking configuration
     */
    public int getTimeTrackingDaysPerWeek()
    {
        return timeTrackingDaysPerWeek;
    }

    /**
     * @return true if attachments are allowed for issues in JIRA
     */
    public boolean isAllowAttachments()
    {
        return this.allowAttachments;
    }

    /**
     * @return true if time tracking is enabled in JIRA
     */
    public boolean isAllowTimeTracking()
    {
        return this.allowTimeTracking;
    }

    /**
     * @return true if issues can be unassigned in JIRA
     */
    public boolean isAllowUnassignedIssues()
    {
        return this.allowUnassignedIssues;
    }

    /**
     * @return true if voting for issues is enabled in JIRA
     */
    public boolean isAllowVoting()
    {
        return this.allowVoting;
    }

    /**
     * @return true if watching of issues is enabled in JIRA
     */
    public boolean isAllowWatching()
    {
        return this.allowWatching;
    }

    /**
     * @return true if issues can have subtasks in JIRA
     */
    public boolean isAllowSubTasks()
    {
        return this.allowSubTasks;
    }

    /**
     * @return true if issues can be linked to other issues in JIRA
     */
    public boolean isAllowIssueLinking()
    {
        return this.allowIssueLinking;
    }

    /**
     * @return true if external user management is enabled in JIRA
     */
    public boolean isAllowExternalUserManagment()
    {
        return allowExternalUserManagment;
    }
}
