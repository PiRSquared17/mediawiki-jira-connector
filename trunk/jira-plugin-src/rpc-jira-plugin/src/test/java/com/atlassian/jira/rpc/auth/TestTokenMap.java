/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 10:09:43 AM
 */
package com.atlassian.jira.rpc.auth;

import junit.framework.TestCase;

public class TestTokenMap extends TestCase
{
    private static final String BAR = "bar";
    private static final String BAZ = "baz";
    private static final String FOO = "foo";

    public void testBasicOperations()
    {
        TokenMap<String,String> map = new TokenMap<String,String>(30000);

        assertNull(map.get(FOO));

        map.put(BAR, BAZ);
        assertEquals(BAZ, map.get(BAR));
        assertTrue(map.containsKey(BAR));
        assertNull(map.get(FOO));

        map.remove(BAR);
        assertNull(map.get(BAR));
        assertNull(map.get(FOO));

        map.put(BAR, BAZ);
        map.clear();
        assertNull(map.get(BAR));
    }

    public void testTimeout() throws InterruptedException
    {
        TokenMap<String,String> map = new TokenMap<String,String>(50); // timeout of 50 ms

        map.put(BAR, BAZ);
        assertEquals(BAZ, map.get(BAR));

        Thread.sleep(100);

        assertNull(map.get(BAR));
    }
}