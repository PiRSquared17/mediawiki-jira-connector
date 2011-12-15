package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.jira.issue.attachment.Attachment;

import java.util.Date;

public class RemoteAttachment extends AbstractRemoteEntity
{
    private String author;
    private Date created;
    private String filename;
    private Long filesize;
    private String mimetype;

    public RemoteAttachment(Attachment attachment)
    {
        super(attachment.getId().toString());
        author = attachment.getAuthor();
        created = attachment.getCreated();
        filename = attachment.getFilename();
        filesize = attachment.getFilesize();
        mimetype = attachment.getMimetype();
    }

    public String getAuthor()
    {
        return author;
    }

    public Date getCreated()
    {
        return created;
    }

    public String getFilename()
    {
        return filename;
    }

    public Long getFilesize()
    {
        return filesize;
    }

    public String getMimetype()
    {
        return mimetype;
    }
}
