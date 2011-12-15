package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.jira.util.dbc.Assertions;

/**
 * A remote API representation of the Avatar entity.
 *
 * @since v4.0
 */
public class RemoteAvatar
{

    private final long id;

    private final String type;

    private final String contentType;

    private final String base64Data;

    private final String owner;

    private final boolean isSystem;

    public RemoteAvatar(final long id, final String contentType, final String owner, final boolean system, final String type, final String base64Data)
    {
        Assertions.notNull("contentType", contentType);
        Assertions.notNull("id", id);
        if (!system)
        {
            Assertions.notNull("owner", owner);
        }
        Assertions.notNull("type", type);
        Assertions.notNull("base64Data", base64Data);
        this.contentType = contentType;
        this.id = id;
        this.isSystem = system;
        this.owner = owner;
        this.type = type;
        this.base64Data = base64Data;
    }

    /**
     * Provides the MIME type of the image.
     *
     * @return the Content Type.
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * Provides the database unique identifier for the Avatar.
     *
     * @return the id.
     */
    public long getId()
    {
        return id;
    }

    /**
     * Indicates whether the avatar is a system or custom avatar.
     *
     * @return true only if this is a system avatar.
     */
    public boolean isSystem()
    {
        return isSystem;
    }

    /**
     * Returns the entity to which this avatar belongs if it is custom. E.g. if it is a project, the project id.
     *
     * @return the owner or null for system avatars.
     */
    public String getOwner()
    {
        return owner;
    }

    /**
     * Provides a string reprentation of the type of avatar, e.g. "project".
     *
     * @return the type.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Provides the image data for the avatar.
     *
     * @return the data as a base 64 encoded String.
     */
    public String getBase64Data()
    {
        return base64Data;
    }

}
