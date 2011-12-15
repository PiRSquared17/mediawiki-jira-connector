/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 9, 2004
 * Time: 1:16:17 PM
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.security.roles.ProjectRole;

import java.util.Date;

public class RemoteComment
{
    private String body;
    private String author;
    private String updateAuthor;
    private String groupLevel;
    private String roleLevel;
    private String id;
    private Date created;
    private Date updated;

    public RemoteComment(Comment comment)
    {
        body = comment.getBody();
        author = comment.getAuthor();
        updateAuthor = comment.getUpdateAuthor();
        groupLevel = comment.getGroupLevel();
        final ProjectRole projectRole = comment.getRoleLevel();
        if (projectRole != null)
        {
            roleLevel = projectRole.getName();
        }
        else
        {
            roleLevel = null;
        }
        id = comment.getId().toString();
        created = comment.getCreated();
        updated = comment.getUpdated();
    }

    public RemoteComment()
    {
    }

    public RemoteComment(String body)
    {
        this.body = body;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getGroupLevel()
    {
        return groupLevel;
    }

    public void setGroupLevel(String groupLevel)
    {
        this.groupLevel = groupLevel;
    }

    public Date getCreated()
    {
        return created;
    }

    public Date getUpdated()
    {
        return updated;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getUpdateAuthor()
    {
        return updateAuthor;
    }

    ///CLOVER:OFF

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

        final RemoteComment that = (RemoteComment) o;

        if (body != null ? !body.equals(that.body) : that.body != null)
        {
            return false;
        }
        if (groupLevel != null ? !groupLevel.equals(that.groupLevel) : that.groupLevel != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (roleLevel != null ? !roleLevel.equals(that.roleLevel) : that.roleLevel != null)
        {
            return false;
        }
        if (created != null ? !created.equals(that.created) : that.created != null)
        {
            return false;
        }
        if (author != null ? !author.equals(that.author) : that.author != null)
        {
            return false;
        }
        if (updated != null ? !updated.equals(that.updated) : that.updated != null)
        {
            return false;
        }
        if (updateAuthor != null ? !updateAuthor.equals(that.updateAuthor) : that.updateAuthor != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (body != null ? body.hashCode() : 0);
        result = 29 * result + (author != null ? author.hashCode() : 0);
        result = 29 * result + (updateAuthor != null ? updateAuthor.hashCode() : 0);
        result = 29 * result + (groupLevel != null ? groupLevel.hashCode() : 0);
        result = 29 * result + (roleLevel != null ? roleLevel.hashCode() : 0);
        result = 29 * result + (id != null ? id.hashCode() : 0);
        result = 29 * result + (created != null ? created.hashCode() : 0);
        result = 29 * result + (updated != null ? updated.hashCode() : 0);
        return result;
    }

    public String getRoleLevel()
    {
        return roleLevel;
    }

    public void setRoleLevel(String roleLevel)
    {
        this.roleLevel = roleLevel;
    }
}