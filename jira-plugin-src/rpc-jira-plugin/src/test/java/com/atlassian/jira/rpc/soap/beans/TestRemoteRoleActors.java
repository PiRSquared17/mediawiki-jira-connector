package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import junit.framework.TestCase;

public class TestRemoteRoleActors extends TestCase
{
    public void testGetUsers()
    {
        RemoteProjectRole projectRole = new RemoteProjectRole(null, "name", "description");

        User user = ImmutableUser.newUser().name("driver").emailAddress("test@test.com").displayName("Test User").toUser();
        RemoteUser remoteUser = new RemoteUser(user);
        RemoteRoleActor actor = new RemoteRoleActor(projectRole, "descriptor", "TYPE", "parameter", new RemoteUser[] { remoteUser });

        RemoteRoleActors remoteRoleActors = new RemoteRoleActors(projectRole, new RemoteRoleActor[] { actor });

        RemoteUser[] returnedUsers = remoteRoleActors.getUsers();
        assertNotNull(returnedUsers);

        assertTrue(returnedUsers.length == 1);

        assertTrue(returnedUsers[0] == remoteUser);
    }

}
