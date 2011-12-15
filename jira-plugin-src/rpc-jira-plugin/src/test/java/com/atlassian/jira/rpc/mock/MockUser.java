package com.atlassian.jira.rpc.mock;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserComparator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MockUser implements User
{
    private String name;
    private String fullName;
    private String email;
    private Map<String, Set<String>> values;

    public MockUser(final String username)
    {
        this(username, "", null);
    }

    public MockUser(final String username, final String fullName, final String email)
    {
        this(username, fullName, email, null);
    }

    public MockUser(final String username, final String fullName, final String email, Map<String, Set<String>> values)
    {
        this.name = username;
        this.fullName = fullName;
        this.email = email;
        if (values == null)
        {
            this.values = new HashMap<String, Set<String>>();
        }
        else
        {
            this.values = values;
        }
    }

    public boolean isActive()
    {
        return true;
    }

    public String getEmailAddress()
    {
        return email;
    }

    public String getDisplayName()
    {
        return fullName;
    }

    public long getDirectoryId()
    {
        return 1;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "<User " + name + ">";
    }

    @Override
    public boolean equals(Object o)
    {
        return (o instanceof User) && UserComparator.equal(this, (User) o);
    }

    @Override
    public int hashCode()
    {
        return UserComparator.hashCode(this);
    }

    public int compareTo(final com.atlassian.crowd.embedded.api.User other)
    {
        return UserComparator.compareTo(this, other);
    }
}

