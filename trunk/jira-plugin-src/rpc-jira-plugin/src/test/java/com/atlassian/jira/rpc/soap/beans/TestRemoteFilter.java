/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 4:57:01 PM
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.ImmutableException;
import junit.framework.TestCase;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

public class TestRemoteFilter extends TestCase
{
    public void testObjectConstructorAndSetters() throws ImmutableException, DuplicateEntityException
    {
        GenericValue gv = new MockGenericValue("Project", UtilMisc.toMap("author", "anton",
                "description", "foo", "project", "JRA", "request", "<req />"));
        RemoteFilter filter = new RemoteFilter(gv);

        assertEquals("foo", filter.getDescription());
        assertEquals("anton", filter.getAuthor());
        assertNull(filter.getProject());
        assertNull(filter.getXml());

        // set and check
        filter.setDescription("BAR");
        assertEquals("BAR", filter.getDescription());
        filter.setAuthor("BAR");
        assertEquals("BAR", filter.getAuthor());
        filter.setProject("BAR");
        assertEquals("BAR", filter.getProject());
        filter.setXml("BAR");
        assertEquals("BAR", filter.getXml());
    }
}