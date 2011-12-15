package com.atlassian.jira.rpc.mock;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManagerImpl;
import com.atlassian.jira.project.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * AvatarManager that keeps avatars in lists. Has no Store. Half-arsed.
 *
 * @since v4.0
 */
public class MockAvatarManager extends AvatarManagerImpl
{
    private List<Avatar> systemAvatars;
    private List<Avatar> customAvatars;
    private Long defaultAvatarId;

    public MockAvatarManager()
    {
        super(null, null, null, null);
        this.systemAvatars = new ArrayList<Avatar>();
        this.customAvatars = new ArrayList<Avatar>();
    }

    @Override
    public List<Avatar> getAllSystemAvatars(Avatar.Type type)
    {
        return systemAvatars;
    }

    /**
     * Returns all custom avatars, regardless of owner or type.
     *
     * @param type    ignored
     * @param ownerId ignored
     * @return the backing list for all custom avatars
     */
    public List<Avatar> getCustomAvatarsForOwner(final Avatar.Type type, final String ownerId)
    {
        return customAvatars;
    }

    /**
     * Working implementation assuming the avatars have been added using this mock's special methods.
     * @param avatarId
     * @return
     */
    public Avatar getById(final Long avatarId)
    {
        for (Avatar systemAvatar : systemAvatars)
        {
            if (systemAvatar.getId().equals(avatarId))
            {
                return systemAvatar;
            }
        }
        for (Avatar customAvatar : customAvatars)
        {
            if (customAvatar.getId().equals(avatarId))
            {
                return customAvatar;
            }
        }

        return null;
    }

    public MockAvatar addSystemProjectAvatar(long id, String filename, String contentType)
    {
        final MockAvatar avatar = new MockAvatar(id, filename, contentType, null, true);
        systemAvatars.add(avatar);
        return avatar;
    }

    public MockAvatar addCustomProjectAvatar(final long id, final String filename, final String contentType, final Project project)
    {
        final MockAvatar avatar = new MockAvatar(id, filename, contentType, project.getId().toString(), false);
        customAvatars.add(avatar);
        return avatar;
    }


    public Long getDefaultAvatarId(final Avatar.Type ofType)
    {
        return defaultAvatarId;
    }

    public void setDefaultAvatarId(final Long defaultAvatarId)
    {
        this.defaultAvatarId = defaultAvatarId;
    }
}
