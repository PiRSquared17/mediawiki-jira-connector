package com.atlassian.jira.rpc;

import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import junit.framework.TestCase;

import java.util.Hashtable;
import java.util.Vector;

public class TestRpcUtils extends TestCase
{
    public void testMakeStruct()
    {
        Hashtable<String, String> result = new Hashtable<String, String>();
        result.put("id", "10");
        result.put("name", "joe");

        RemoteProject project = new RemoteProject();
        project.setId("10");
        project.setName("joe");
        assertEquals(result, RpcUtils.makeStruct(project));
    }

    public void testMakeVector()
    {
        Hashtable<Object, String> first = new Hashtable<Object, String>();
        first.put("name", "joe");
        Hashtable<Object, String> second = new Hashtable<Object, String>();
        second.put("name", "paul");
        Vector<Hashtable<Object, String>> result = new Vector<Hashtable<Object, String>>();
        result.add(first);
        result.add(second);

        RemoteProject project = new RemoteProject();
        project.setName("joe");
        RemoteProject projectTwo = new RemoteProject();
        projectTwo.setName("paul");

        assertEquals(result, RpcUtils.makeVector(new RemoteProject[] { project, projectTwo }));
    }
}
