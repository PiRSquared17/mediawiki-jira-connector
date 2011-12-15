package com.atlassian.jira.rpc.soap.beans;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Remote version of @see com.atlassian.jira.security.roles.DefaultRoleActors
 */
public class RemoteRoleActors
{
    private Set roleActors = null;
    private RemoteProjectRole remoteProjectRole = null;

    public RemoteRoleActors(RemoteProjectRole remoteProjectRole, RemoteRoleActor[] roleActors)
    {
        this.roleActors = new HashSet(Arrays.asList(roleActors));
        this.remoteProjectRole = remoteProjectRole;
    }

    public RemoteUser[] getUsers()
    {
        Set remoteUsers = new HashSet();
        for (Iterator iterator = roleActors.iterator(); iterator.hasNext();)
        {
            RemoteRoleActor remoteRoleActor = (RemoteRoleActor) iterator.next();
            for (int j = 0; j < remoteRoleActor.getUsers().length; j++)
            {
                RemoteUser remoteUser = remoteRoleActor.getUsers()[j];
                remoteUsers.add(remoteUser);
            }
        }

        return (RemoteUser[]) remoteUsers.toArray(new RemoteUser[remoteUsers.size()]);

        //return convertUsersListToArray(remoteUsers);
    }

    private RemoteUser[] convertUsersListToArray(Set remoteUsers)
    {
        RemoteUser[] users = new RemoteUser[remoteUsers.size()];
        int i = 0;
        for (Iterator iterator = remoteUsers.iterator(); iterator.hasNext(); i++)
        {
            users[i] = (RemoteUser) iterator.next();
        }
        return users;
    }

    public RemoteRoleActor[] getRoleActors()
    {
        return convertRoleActorSetToArray(roleActors);
    }

    public RemoteProjectRole getProjectRole()
    {
        return this.remoteProjectRole;
    }

    public void addRoleActor(RemoteRoleActor roleActor)
    {
        this.roleActors.add(roleActor);
    }

    public void addRoleActors(Collection roleActors)
    {
        this.roleActors.addAll(roleActors);
    }

    public void removeRoleActor(RemoteRoleActor roleActor)
    {
        this.roleActors.remove(roleActor);
    }

    public void removeRoleActors(Collection roleActors)
    {
        this.roleActors.removeAll(roleActors);
    }

    public RemoteRoleActor[] getRoleActorsByType(String type)
    {
        Set roleActorsForType = new HashSet();
        for (Iterator iterator = roleActors.iterator(); iterator.hasNext();)
        {
            RemoteRoleActor remoteRoleActor = (RemoteRoleActor) iterator.next();
            if (remoteRoleActor.getType().equals(type))
            {
                roleActorsForType.add(remoteRoleActor);
            }
        }
        return convertRoleActorSetToArray(roleActorsForType);
    }

    private RemoteRoleActor[] convertRoleActorSetToArray(Set roleActors)
    {
        RemoteRoleActor[] roleActorsArr = new RemoteRoleActor[roleActors.size()];
        int i = 0;
        for (Iterator iterator = roleActors.iterator(); iterator.hasNext(); i++)
        {
            roleActorsArr[i] = (RemoteRoleActor) iterator.next();
        }
        return roleActorsArr;
    }
}
