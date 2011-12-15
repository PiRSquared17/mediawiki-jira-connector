package com.atlassian.jira.rpc.mock;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.Entity;
import com.opensymphony.user.ProviderAccessor;
import com.opensymphony.user.UserManager;
import com.opensymphony.user.provider.AccessProvider;
import com.opensymphony.user.provider.CredentialsProvider;
import com.opensymphony.user.provider.ProfileProvider;


/**
 * TODO - this a  COPY AND PASTE hack.  We need to resolve this dependency somehow elsewhere
 */
public class MockProviderAccessor implements ProviderAccessor
{
    /**
     * delegate
     */
    private ProviderAccessor providerAccessorProxy;

    public MockProviderAccessor()
    {
        this("Administrator", "admin@example.com");
    }

    /**
     * Main ctor for User where you want to specify the name and email
     *
     * @param fullName
     * @param email
     */
    public MockProviderAccessor(final String fullName, final String email)
    {

        Object credentialsProvider = new Object()
        {};
        final CredentialsProvider credentialsProviderProxy = (CredentialsProvider) DuckTypeProxy.getProxy(CredentialsProvider.class, credentialsProvider);

        Object accessProvider = new Object()
        {
            public boolean inGroup(String username, String groupname)
            {
                return "test".equals(username) && "test".equals(groupname);
            }
        };
        final AccessProvider accessProviderProxy = (AccessProvider) DuckTypeProxy.getProxy(AccessProvider.class, accessProvider);

        Object propertySet = new Object()
        {
            public String getString(String name)
            {
                if ("fullName".equals(name))
                {
                    return fullName;
                }
                else if ("email".equals(name))
                {
                    return email;
                }
                return null;
            }
        };
        final PropertySet propertySetProxy = (PropertySet) DuckTypeProxy.getProxy(PropertySet.class, propertySet);

        Object profileProvider = new Object()
        {
            public boolean handles(String string)
            {
                return true;
            }

            public PropertySet getPropertySet(String string)
            {
                return propertySetProxy;
            }

        };
        final ProfileProvider profileProviderProxy = (ProfileProvider) DuckTypeProxy.getProxy(ProfileProvider.class, profileProvider);


        Object providerAccessor = new Object()
        {
            public ProfileProvider getProfileProvider(String name)
            {
                return profileProviderProxy;
            }

            public CredentialsProvider getCredentialsProvider(String name)
            {
                return credentialsProviderProxy;
            }

            public AccessProvider getAccessProvider(String name)
            {
                return accessProviderProxy;
            }
        };
        providerAccessorProxy = (ProviderAccessor) DuckTypeProxy.getProxy(ProviderAccessor.class, providerAccessor);
    }


    public AccessProvider getAccessProvider(String s)
    {
        return providerAccessorProxy.getAccessProvider(s);
    }

    public CredentialsProvider getCredentialsProvider(String s)
    {
        return providerAccessorProxy.getCredentialsProvider(s);
    }

    public ProfileProvider getProfileProvider(String s)
    {
        return providerAccessorProxy.getProfileProvider(s);
    }

    public UserManager getUserManager()
    {
        throw new UnsupportedOperationException("uh-oh, world of pain. if you see this, you are probably calling User.inGroup(String) or Group.add/removeMember(Principal) methods. You need to resolve all User and Group instances before you get to the User/Group calls.");
    }
}
