/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 5:25:28 PM
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import junit.framework.TestCase;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

public class TestAbstractRemoteConstant extends TestCase
{
    public void testObjectConstructor()
    {
        GenericValue gv = new MockGenericValue("IssueType", UtilMisc.toMap("description", "a constant", "iconurl", "http://icon.gif"));
        AbstractRemoteConstant arc = new RemoteIssueType(gv, false);
        assertEquals("a constant", arc.getDescription());
        assertEquals("http://icon.gif", arc.getIcon());

        // set stuff and check
        arc.setDescription("foo");
        assertEquals("foo", arc.getDescription());
        arc.setIcon("foo");
        assertEquals("foo", arc.getIcon());

        // check with null icon
        gv.set("iconurl", null);
        assertNull(new RemoteIssueType(gv, false).getIcon());
    }
}