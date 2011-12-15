/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

public class RemoteProject extends AbstractNamedRemoteEntity
{
    String key;
    String url;
    String projectUrl;
    String lead;
    String description;

    RemoteScheme issueSecurityScheme;
    RemotePermissionScheme permissionScheme;
    RemoteScheme notificationScheme;

    public RemoteProject()
    {
        super();
    }

    public RemoteProject(Project project, ApplicationProperties applicationProperties)
    {
        super(project.getId() == null ? "" : project.getId().toString(), project.getName());
        this.key = project.getKey();
        this.url = applicationProperties.getString(APKeys.JIRA_BASEURL) + "/browse/" + key;
        this.projectUrl = project.getUrl();
        this.lead = project.getLeadUserName();
        this.description = project.getDescription();
    }

    public RemoteProject(GenericValue project, ApplicationProperties applicationProperties)
    {
        super(project);
        this.key = project.getString("key");
        this.url = applicationProperties.getString(APKeys.JIRA_BASEURL) + "/browse/" + key;
        this.projectUrl = project.getString("url");
        this.lead = project.getString("lead");
        this.description = project.getString("description");
    }

    public String getKey()
    {
        return key;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getProjectUrl()
    {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl)
    {
        this.projectUrl = projectUrl;
    }

    public String getLead()
    {
        return lead;
    }

    public void setLead(String lead)
    {
        this.lead = lead;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public RemoteScheme getIssueSecurityScheme()
    {
        return issueSecurityScheme;
    }

    public void setIssueSecurityScheme(RemoteScheme issueSecurityScheme)
    {
        this.issueSecurityScheme = issueSecurityScheme;
    }

    public RemotePermissionScheme getPermissionScheme()
    {
        return permissionScheme;
    }

    public void setPermissionScheme(RemotePermissionScheme permissionScheme)
    {
        this.permissionScheme = permissionScheme;
    }

    public RemoteScheme getNotificationScheme()
    {
        return notificationScheme;
    }

    public void setNotificationScheme(RemoteScheme notificationScheme)
    {
        this.notificationScheme = notificationScheme;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RemoteProject))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        final RemoteProject remoteProject = (RemoteProject) o;

        if (description != null ? !description.equals(remoteProject.description) : remoteProject.description != null)
        {
            return false;
        }
        if (issueSecurityScheme != null ? !issueSecurityScheme.equals(remoteProject.issueSecurityScheme) : remoteProject.issueSecurityScheme != null)
        {
            return false;
        }
        if (key != null ? !key.equals(remoteProject.key) : remoteProject.key != null)
        {
            return false;
        }
        if (lead != null ? !lead.equals(remoteProject.lead) : remoteProject.lead != null)
        {
            return false;
        }
        if (notificationScheme != null ? !notificationScheme.equals(remoteProject.notificationScheme) : remoteProject.notificationScheme != null)
        {
            return false;
        }
        if (permissionScheme != null ? !permissionScheme.equals(remoteProject.permissionScheme) : remoteProject.permissionScheme != null)
        {
            return false;
        }
        if (projectUrl != null ? !projectUrl.equals(remoteProject.projectUrl) : remoteProject.projectUrl != null)
        {
            return false;
        }
        if (url != null ? !url.equals(remoteProject.url) : remoteProject.url != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 29 * result + (key != null ? key.hashCode() : 0);
        result = 29 * result + (url != null ? url.hashCode() : 0);
        result = 29 * result + (projectUrl != null ? projectUrl.hashCode() : 0);
        result = 29 * result + (lead != null ? lead.hashCode() : 0);
        result = 29 * result + (description != null ? description.hashCode() : 0);
        result = 29 * result + (issueSecurityScheme != null ? issueSecurityScheme.hashCode() : 0);
        result = 29 * result + (permissionScheme != null ? permissionScheme.hashCode() : 0);
        result = 29 * result + (notificationScheme != null ? notificationScheme.hashCode() : 0);
        return result;
    }
}
