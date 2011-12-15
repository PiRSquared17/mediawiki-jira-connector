/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 4:57:53 PM
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import junit.framework.TestCase;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

public class TestAbstractRemoteEntity extends TestCase
{
    public void testObjectConstructorAndId()
    {
        GenericValue gv = new MockGenericValue("GV", UtilMisc.toMap("id", new Long(10)));
        AbstractRemoteEntity are = new AbstractRemoteEntity(gv)
        {
        };
        assertEquals("10", are.getId());

        are.setId("15");
        assertEquals("15", are.getId());
    }
}