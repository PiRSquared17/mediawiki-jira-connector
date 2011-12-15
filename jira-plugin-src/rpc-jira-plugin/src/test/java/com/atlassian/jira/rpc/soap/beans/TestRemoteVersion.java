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

import java.sql.Timestamp;

public class TestRemoteVersion extends TestCase
{
    public void testObjectConstructorAndSetters()
    {
        GenericValue gv = new MockGenericValue("Version", UtilMisc.toMap("released", "true", "archived", "false",
                "sequence", new Long(5), "releasedate", new Timestamp(4000)));
        RemoteVersion rv = new RemoteVersion(gv);

        assertTrue(rv.isReleased());
        assertFalse(rv.isArchived());
        assertEquals(new Long(5), rv.getSequence());
        assertEquals(new Timestamp(4000), rv.getReleaseDate());

        // now try setters
        rv.setReleased(false);
        assertFalse(rv.isReleased());

        rv.setArchived(true);
        assertTrue(rv.isArchived());

        rv.setSequence(new Long(10));
        assertEquals(new Long(10), rv.getSequence());

        rv.setReleaseDate(new Timestamp(3000));
        assertEquals(new Timestamp(3000), rv.getReleaseDate());
    }
}