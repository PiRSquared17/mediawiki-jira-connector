/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.soap.beans;

import org.ofbiz.core.entity.GenericValue;

import java.util.Date;

public class RemoteVersion extends AbstractNamedRemoteEntity
{
    private boolean released;
    private boolean archived;
    private Long sequence;
    private Date releaseDate;

    public RemoteVersion()
    {
    }

    public RemoteVersion(GenericValue gv)
    {
        super(gv);
        released = "true".equals(gv.getString("released"));
        archived = "true".equals(gv.getString("archived"));
        sequence = gv.getLong("sequence");
        releaseDate = gv.getTimestamp("releasedate");
    }

    public RemoteVersion(final String id, final String name, final boolean released, final boolean archived,
            final Long sequence,
            final Date releaseDate)
    {
        super(id, name);
        this.released = released;
        this.archived = archived;
        this.sequence = sequence;
        this.releaseDate = releaseDate;
    }

    public boolean isReleased()
    {
        return released;
    }

    public void setReleased(boolean released)
    {
        this.released = released;
    }

    public boolean isArchived()
    {
        return archived;
    }

    public void setArchived(boolean archived)
    {
        this.archived = archived;
    }

    public Long getSequence()
    {
        return sequence;
    }

    public void setSequence(Long sequence)
    {
        this.sequence = sequence;
    }

    public Date getReleaseDate()
    {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
    }

    ///CLOVER:OFF
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RemoteVersion))
        {
            return false;
        }

        final RemoteVersion remoteVersion = (RemoteVersion) o;

        if (archived != remoteVersion.archived)
        {
            return false;
        }
        if (released != remoteVersion.released)
        {
            return false;
        }
        if (releaseDate != null ? !releaseDate.equals(remoteVersion.releaseDate) : remoteVersion.releaseDate != null)
        {
            return false;
        }
        if (sequence != null ? !sequence.equals(remoteVersion.sequence) : remoteVersion.sequence != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (released ? 1 : 0);
        result = 29 * result + (archived ? 1 : 0);
        result = 29 * result + (sequence != null ? sequence.hashCode() : 0);
        result = 29 * result + (releaseDate != null ? releaseDate.hashCode() : 0);
        return result;
    }
}
