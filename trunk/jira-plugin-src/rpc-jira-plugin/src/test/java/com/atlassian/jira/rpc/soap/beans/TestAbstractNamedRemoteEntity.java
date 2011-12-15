/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 4:57:39 PM
 */
package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import junit.framework.TestCase;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

public class TestAbstractNamedRemoteEntity extends TestCase
{
    public void testObjectConstructorAndId()
    {
        GenericValue gv = new MockGenericValue("GV", UtilMisc.toMap("name", "Arne"));
        AbstractNamedRemoteEntity anre = new AbstractNamedRemoteEntity(gv)
        {
        };
        assertEquals("Arne", anre.getName());

        anre.setName("fred");
        assertEquals("fred", anre.getName());
    }
}