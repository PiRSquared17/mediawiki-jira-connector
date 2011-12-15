/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 4:57:01 PM
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.ImmutableException;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import java.sql.Timestamp;

public class TestRemoteProject extends MockObjectTestCase
{
    private Mock mockAp;

    protected void setUp() throws Exception
    {
        super.setUp();
        mockAp = new Mock(ApplicationProperties.class);
    }

    public void testObjectConstructorAndSetters() throws ImmutableException, DuplicateEntityException
    {
        mockAp.expects(once()).method("getString").with(eq(APKeys.JIRA_BASEURL)).will(returnValue("http://atl.com"));
        GenericValue gv = new MockGenericValue("Project", UtilMisc.toMap("key", "JRA",
                "description", "foo", "lead", "anton", "url", "http://server.com",
                "created", new Timestamp(1000)));
        RemoteProject project = new RemoteProject(gv, (ApplicationProperties) mockAp.proxy());

        assertEquals("JRA", project.getKey());
        assertEquals("foo", project.getDescription());
        assertEquals("anton", project.getLead());
        assertEquals("http://server.com", project.getProjectUrl());
        assertEquals("http://atl.com/browse/JRA", project.getUrl());

        // set and check
        project.setKey("BAR");
        assertEquals("BAR", project.getKey());
        project.setDescription("BAR");
        assertEquals("BAR", project.getDescription());
        project.setLead("BAR");
        assertEquals("BAR", project.getLead());
        project.setProjectUrl("BAR");
        assertEquals("BAR", project.getProjectUrl());
    }
}