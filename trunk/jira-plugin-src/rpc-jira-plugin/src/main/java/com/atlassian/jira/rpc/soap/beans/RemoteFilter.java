/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.jira.issue.search.SearchRequest;
import org.ofbiz.core.entity.GenericValue;

public class RemoteFilter extends AbstractNamedRemoteEntity
{
    String description;
    String author;
    String project;
    String xml;

    public RemoteFilter()
    {
    }

    public RemoteFilter(GenericValue gv)
    {
        super(gv);
        this.description = gv.getString("description");
        this.author = gv.getString("author");
        this.project = null;
        this.xml = null;
    }

    public RemoteFilter(SearchRequest filter)
    {
        super(filter.getId().toString(), filter.getName());
        this.description = filter.getDescription();
        this.author = filter.getOwnerUserName();
        this.project = null;
        this.xml = null;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    /**
     * @return always returns null, saved filters no longer keep a reference to a single project
     * @deprecated
     */
    public String getProject()
    {
        return project;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    /**
     * @return always returns null, saved filters no longer keep their query in XML format, not really sure if this
     * was ever useful.
     * @deprecated use {@link #}
     */
    public String getXml()
    {
        return xml;
    }

    public void setXml(String xml)
    {
        this.xml = xml;
    }

    ///CLOVER:OFF
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RemoteFilter))
        {
            return false;
        }

        final RemoteFilter remoteFilter = (RemoteFilter) o;

        if (author != null ? !author.equals(remoteFilter.author) : remoteFilter.author != null)
        {
            return false;
        }
        if (description != null ? !description.equals(remoteFilter.description) : remoteFilter.description != null)
        {
            return false;
        }
        if (project != null ? !project.equals(remoteFilter.project) : remoteFilter.project != null)
        {
            return false;
        }
        if (xml != null ? !xml.equals(remoteFilter.xml) : remoteFilter.xml != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (description != null ? description.hashCode() : 0);
        result = 29 * result + (author != null ? author.hashCode() : 0);
        result = 29 * result + (project != null ? project.hashCode() : 0);
        result = 29 * result + (xml != null ? xml.hashCode() : 0);
        return result;
    }
}
