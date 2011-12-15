/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 9, 2004
 * Time: 4:09:01 PM
 */
package com.atlassian.jira.rpc.xmlrpc;

import com.atlassian.core.util.RandomGenerator;
import junit.framework.TestCase;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class XmlRpcTestClient extends TestCase
{
    public void testServer()
    {
        try
        {
            final String server = "http://localhost:8080/rpc/xmlrpc";

            XmlRpcClient xmlrpc = new XmlRpcClient(server);

            // First let's be naughty and try to call a method without logging in
            try
            {
                xmlrpc.execute("jira1.getProjects", makeParams("foo"));
                fail("Should have blown up - what happened!");
            }
            catch (Exception e)
            {
            }

            // Now logging in properly...
            String token = (String) xmlrpc.execute("jira1.login", makeParams("a", "a"));
            assertNotNull(token);

            // Now let's get some projects...
            Vector projects = (Vector) xmlrpc.execute("jira1.getProjects", makeParams(token));
            assertNotNull(projects);
            System.out.println("projects:");
            printVector(projects);

            // Now let's get some components...
            Vector components = (Vector) xmlrpc.execute("jira1.getComponents", makeParams(token, "DON"));
            assertNotNull(components);
            System.out.println("components:");
            printVector(components);

            // Now let's get some components...
            Vector versions = (Vector) xmlrpc.execute("jira1.getVersions", makeParams(token, "DON"));
            assertNotNull(versions);
            System.out.println("versions:");
            printVector(versions);

            // Let's check we can't get a barfed issue...
            try
            {
                xmlrpc.execute("jira1.getIssue", makeParams(token, "10"));
                fail("Should have blown up");
            }
            catch (XmlRpcException e)
            {
                e.printStackTrace();
            }

            // Can we add a new issue I wonder?
            Hashtable issue = new Hashtable();
            final String summary = "remote page " + RandomGenerator.randomPassword();
            issue.put("summary", summary);
            issue.put("project", "DON");
            issue.put("type", "1");
            issue.put("environment", "green");
            issue.put("description", "nuffin");
            issue.put("assignee", "a");
            issue.put("priority", "3");
            final Date now = new Date();
            issue.put("updated", now);
            issue.put("components", components);
            Vector fixVersions = new Vector(versions.subList(0, versions.size() / 2));
            issue.put("fixVersions", fixVersions);
            Vector affectsVersions = new Vector();
            if (versions.size() > 0)
            {
                affectsVersions = new Vector(versions.subList(versions.size() / 2 - 1, versions.size() - 1));
            }
            issue.put("affectsVersions", affectsVersions);

            issue = (Hashtable) xmlrpc.execute("jira1.createIssue", makeParams(token, issue));

            assertEquals("DON", issue.get("project"));
            assertEquals(summary, issue.get("summary"));
            assertEquals("1", issue.get("type"));
            assertEquals("a", issue.get("reporter"));
            assertEquals("nuffin", issue.get("description"));
            assertEquals("green", issue.get("environment"));
            assertEquals("a", issue.get("assignee"));
            assertEquals("3", issue.get("priority"));
            assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0").format(now), issue.get("updated"));
            assertSameVector(components, (Vector) issue.get("components"));
            assertSameVector(new Vector(fixVersions), (Vector) issue.get("fixVersions"));
            assertSameVector(new Vector(affectsVersions), (Vector) issue.get("affectsVersions"));

            System.out.println("added issue = " + issue);

            // Now we should be nice and logout...
            Boolean loggedOut = (Boolean) xmlrpc.execute("jira1.logout", makeParams(token));
            assertTrue(loggedOut.booleanValue());

            // Oops, can't logout twice!
            loggedOut = (Boolean) xmlrpc.execute("jira1.logout", makeParams(token));
            assertFalse(loggedOut.booleanValue());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    private void assertSameVector(Vector v1, Vector v2)
    {
        assertEquals(v1.size(), v2.size());

        for (Iterator iterator = v1.iterator(); iterator.hasNext();)
        {
            Object o = (Object) iterator.next();
            assertTrue(v2.contains(o));
        }
    }

    private static void printVector(Vector vector)
    {
        System.out.println("Size: " + vector.size() + "{");
        for (Iterator iterator = vector.iterator(); iterator.hasNext();)
        {
            Object o = (Object) iterator.next();
            System.out.println("  " + o.toString());
        }
        System.out.println("}");
    }

    private static Vector makeParams(Object p1)
    {
        Vector params;
        params = new Vector();
        params.add(p1);

        return params;
    }

    private static Vector makeParams(Object p1, Object p2)
    {
        Vector params = makeParams(p1);
        params.add(p2);
        return params;
    }

    private static Vector makeParams(Object p1, Object p2, Object p3)
    {
        Vector params = makeParams(p1, p2);
        params.add(p3);
        return params;
    }

    private static Vector makeParams(Object p1, Object p2, Object p3, Object p4)
    {
        Vector params = makeParams(p1, p2, p3);
        params.add(p4);
        return params;
    }

    private static Vector makeParams(Object p1, Object p2, Object p3, Object p4, Object p5)
    {
        Vector params = makeParams(p1, p2, p3, p4);
        params.add(p5);
        return params;
    }
}