/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Sep 9, 2004
 * Time: 5:22:22 PM
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.BuildUtils;
import com.atlassian.jira.util.BuildUtilsInfo;

import java.util.Date;

public class RemoteServerInfo
{
    private String baseUrl;
    private String version;
    private Date buildDate;
    private String buildNumber;

    public RemoteServerInfo()
    {
        final BuildUtilsInfo buildUtilsInfo = ComponentManager.getComponent(BuildUtilsInfo.class);
        final ApplicationProperties applicationProperties = ComponentManager.getComponent(ApplicationProperties.class);

        this.buildDate = buildUtilsInfo.getCurrentBuildDate();
        this.buildNumber = buildUtilsInfo.getCurrentBuildNumber();
        this.version = buildUtilsInfo.getVersion();
        this.baseUrl = applicationProperties.getString(APKeys.JIRA_BASEURL);
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public Date getBuildDate()
    {
        return buildDate;
    }

    public String getBuildNumber()
    {
        return buildNumber;
    }

    public String getVersion()
    {
        return version;
    }

    public RemoteTimeInfo getServerTime()
    {
        return new RemoteTimeInfo();
    }
}