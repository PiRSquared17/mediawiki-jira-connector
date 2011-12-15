package com.atlassian.jira.rpc.mock;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.util.OSUserConverter;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.issue.issuetype.IssueType;

import java.util.Collection;

/**
 * Bean implementation of Project interface but doesn't believe in GenericValues.
 */
public class MockProject implements Project
{

    private Long id;
    private String name;
    private String key;
    private String url;
    private User user;
    private com.opensymphony.user.User lead;
    private String description;
    private Long assigneeType;
    private Long counter;
    private Collection components;
    private Collection versions;
    private GenericValue projectGV;
    private GenericValue projectCategoryGV;
    private Avatar avatar;

    public MockProject()
    {
    }

    public MockProject(long id)
    {
        this(id, null, null, null);
    }

    public MockProject(Long id)
    {
        this(id, null, null, null);
    }

    public MockProject(long id, String key)
    {
        this(id, key, key, null);
    }

    public MockProject(long id, String key, String name)
    {
        this(id, key, name, null);
    }

    public MockProject(Long id, String key, String name)
    {
        this(id, key, name, null);
    }

    public MockProject(long id, String key, String name, GenericValue projectGV)
    {
        this(new Long(id), key, name, projectGV);
    }

    public MockProject(Long id, String key, String name, GenericValue projectGV)
    {
        this.id = id;
        this.key = key;
        this.name = name;
        this.projectGV = projectGV;
    }

    public Long getAssigneeType()
    {
        return assigneeType;
    }

    public void setAssigneeType(Long assigneeType)
    {
        this.assigneeType = assigneeType;
    }

    public Collection getComponents()
    {
        return components;
    }

    public void setComponents(Collection components)
    {
        this.components = components;
    }

    public Collection<ProjectComponent> getProjectComponents()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Long getCounter()
    {
        return counter;
    }

    public void setCounter(Long counter)
    {
        this.counter = counter;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public Collection getVersions()
    {
        return versions;
    }

    public GenericValue getProjectCategory()
    {
        return projectCategoryGV;
    }

    public void setProjectCategoryGV(GenericValue projectCategoryGV)
    {
        this.projectCategoryGV = projectCategoryGV;
    }

    public void setVersions(Collection versions)
    {
        this.versions = versions;
    }

    public GenericValue getGenericValue()
    {
        if (projectGV != null)
        {
            return projectGV;
        }
        // Create one on the fly...
        // TODO: Add other fields.
        MockGenericValue gv = new MockGenericValue("Project");
        gv.set("id", getId());
        gv.set("name", getName());
        gv.set("key", getKey());
        gv.set("description", getDescription());
        return gv;
    }

    public com.opensymphony.user.User getLead()
    {
        return lead;
    }

    public String getLeadUserName()
    {
        return lead.getName();
    }

    public void setLead(User lead)
    {
        this.lead = OSUserConverter.convertToOSUser(lead);
    }

    public Avatar getAvatar()
    {
        return avatar;
    }

    public void setAvatar(final Avatar avatar)
    {
        this.avatar = avatar;
    }

    public User getLeadUser()
    {
        return null;
    }

    public Collection<IssueType> getIssueTypes()
    {
        return null;
    }
}
