package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import junit.framework.TestCase;

public class TestRemoteUser extends TestCase
{
    public void testBlankConstructor()
    {
        RemoteUser ru = new RemoteUser();
        ru.setName("name");
        ru.setEmail("foo@bar.com");
        ru.setFullname("Full Name");

        assertEquals("name", ru.getName());
        assertEquals("foo@bar.com", ru.getEmail());
        assertEquals("Full Name", ru.getFullname());
    }

    public void testObjectConstructor()
    {
        User bill = ImmutableUser.newUser().name("bill").emailAddress("email@host.com").displayName("Full Bill").toUser();
        RemoteUser ru = new RemoteUser(bill);
        assertEquals("bill", ru.getName());
        assertEquals("email@host.com", ru.getEmail());
        assertEquals("Full Bill", ru.getFullname());
    }
}