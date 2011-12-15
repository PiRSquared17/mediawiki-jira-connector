/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 4:57:01 PM
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import junit.framework.TestCase;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

public class TestRemotePriority extends TestCase
{
    public void testObjectConstructorAndSetters()
    {
        GenericValue gv = new MockGenericValue("Priority", UtilMisc.toMap("statusColor", "red"));
        RemotePriority priority = new RemotePriority(gv);

        assertEquals("red", priority.getColor());

        // now try setters
        priority.setColor("blue");
        assertEquals("blue", priority.getColor());
    }
}