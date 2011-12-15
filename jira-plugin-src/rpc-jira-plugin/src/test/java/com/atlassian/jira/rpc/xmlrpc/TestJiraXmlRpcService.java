package com.atlassian.jira.rpc.xmlrpc;

import com.atlassian.jira.rpc.soap.JiraSoapService;
import com.atlassian.jira.rpc.soap.beans.RemoteComment;
import com.atlassian.jira.rpc.soap.beans.RemoteComponent;
import com.atlassian.jira.rpc.soap.beans.RemoteCustomFieldValue;
import com.atlassian.jira.rpc.soap.beans.RemoteFilter;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.atlassian.jira.rpc.soap.beans.RemoteIssueType;
import com.atlassian.jira.rpc.soap.beans.RemotePriority;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import com.atlassian.jira.rpc.soap.beans.RemoteResolution;
import com.atlassian.jira.rpc.soap.beans.RemoteStatus;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;
import com.atlassian.jira.rpc.soap.beans.RemoteVersion;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginManager;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.util.Hashtable;
import java.util.Vector;

public class TestJiraXmlRpcService extends MockObjectTestCase
{
    private Mock mockSoapService;
    private JiraXmlRpcService xmlRpcService;

    protected void setUp() throws Exception
    {
        super.setUp();
        Mock mockPluginManager = new Mock(PluginManager.class);

        mockSoapService = new Mock(JiraSoapService.class);
        JiraSoapService jiraSoapService = (JiraSoapService) mockSoapService.proxy();

        Mock mockModuleDescriptor = new Mock(ModuleDescriptor.class);
        mockModuleDescriptor.expects(once()).method("getModule").will(returnValue(jiraSoapService));

        mockPluginManager.expects(once()).method("getPluginModule").will(returnValue((ModuleDescriptor) mockModuleDescriptor.proxy()));
        xmlRpcService = new JiraXmlRpcService((PluginManager) mockPluginManager.proxy());
    }

    public void testAuthenticationWorks() throws Exception
    {
        mockSoapService.expects(once()).method("login").with(same("foo"), same("bar")).will(returnValue("toktoken"));
        mockSoapService.expects(once()).method("logout").with(same("toktoken")).will(returnValue(true));
        String token = xmlRpcService.login("foo", "bar");
        xmlRpcService.logout(token);
    }

    public void testGetProjects() throws Exception
    {
        RemoteProject[] result = new RemoteProject[2];
        result[0] = new RemoteProject();
        result[0].setKey("FRU");
        result[1] = new RemoteProject();
        result[1].setName("Vegetables");

        mockSoapService.expects(once()).method("getProjectsNoSchemes").will(returnValue(result));

        Vector projects = xmlRpcService.getProjectsNoSchemes("token");

        assertEquals(2, projects.size());
        Hashtable projectStruct = (Hashtable) projects.get(0);
        assertEquals("FRU", projectStruct.get("key"));
        Hashtable projectStruct2 = (Hashtable) projects.get(1);
        assertEquals("Vegetables", projectStruct2.get("name"));
    }

    public void testGetVersions() throws Exception
    {
        RemoteVersion[] result = makeVersions("FRU", "Vegetables");

        mockSoapService.expects(once()).method("getVersions").will(returnValue(result));

        Vector versions = xmlRpcService.getVersions("token", "LON");

        assertMadeEntities(versions, "FRU", "Vegetables");
    }

    private void assertMadeEntities(Vector entities, String name1, String name2)
    {
        assertEquals(2, entities.size());
        Hashtable struct1 = (Hashtable) entities.get(0);
        assertEquals(name1, struct1.get("name"));
        Hashtable struct2 = (Hashtable) entities.get(1);
        assertEquals(name2, struct2.get("name"));
    }

    private RemoteVersion[] makeVersions(String name1, String name2)
    {
        RemoteVersion[] result = new RemoteVersion[2];
        result[0] = new RemoteVersion();
        result[0].setName(name1);
        result[1] = new RemoteVersion();
        result[1].setName(name2);
        return result;
    }

    public void testGetComponents() throws Exception
    {
        RemoteComponent[] result = makeComponents("Walnes Component", "Stevenson Component");
        mockSoapService.expects(once()).method("getComponents").will(returnValue(result));
        assertMadeEntities(xmlRpcService.getComponents("token", "LON"), "Walnes Component", "Stevenson Component");
    }

    private RemoteComponent[] makeComponents(String name1, String name2)
    {
        RemoteComponent[] result = new RemoteComponent[2];
        result[0] = new RemoteComponent();
        result[0].setName(name1);
        result[1] = new RemoteComponent();
        result[1].setName(name2);
        return result;
    }

    public void testGetIssueTypesForProject() throws Exception
    {
        RemoteIssueType[] result = new RemoteIssueType[2];
        result[0] = new RemoteIssueType();
        result[0].setName("FRU");
        result[1] = new RemoteIssueType();
        result[1].setName("Vegetables");

        mockSoapService.expects(once()).method("getIssueTypesForProject").with(eq("token"), eq("1")).will(returnValue(result));

        Vector issuetypes = xmlRpcService.getIssueTypesForProject("token", "1");
        assertMadeEntities(issuetypes, "FRU", "Vegetables");
    }

    public void testGetIssuetypes() throws Exception
    {
        RemoteIssueType[] result = new RemoteIssueType[2];
        result[0] = new RemoteIssueType();
        result[0].setName("FRU");
        result[1] = new RemoteIssueType();
        result[1].setName("Vegetables");

        mockSoapService.expects(once()).method("getIssueTypes").will(returnValue(result));

        Vector issuetypes = xmlRpcService.getIssueTypes("token");

        assertMadeEntities(issuetypes, "FRU", "Vegetables");
    }

    public void testGetSubTaskIssuetypes() throws Exception
    {
        RemoteIssueType[] result = new RemoteIssueType[2];
        result[0] = new RemoteIssueType();
        result[0].setName("FRU");
        result[1] = new RemoteIssueType();
        result[1].setName("Vegetables");

        mockSoapService.expects(once()).method("getSubTaskIssueTypes").will(returnValue(result));

        Vector issuetypes = xmlRpcService.getSubTaskIssueTypes("token");

        assertMadeEntities(issuetypes, "FRU", "Vegetables");
    }

    public void testGetPriorities() throws Exception
    {
        RemotePriority[] result = new RemotePriority[2];
        result[0] = new RemotePriority();
        result[0].setName("FRU");
        result[1] = new RemotePriority();
        result[1].setName("Vegetables");

        mockSoapService.expects(once()).method("getPriorities").will(returnValue(result));

        Vector priorities = xmlRpcService.getPriorities("token");

        assertMadeEntities(priorities, "FRU", "Vegetables");
    }

    public void testGetStatuses() throws Exception
    {
        RemoteStatus[] result = new RemoteStatus[2];
        result[0] = new RemoteStatus();
        result[0].setName("FRU");
        result[1] = new RemoteStatus();
        result[1].setName("Vegetables");

        mockSoapService.expects(once()).method("getStatuses").will(returnValue(result));

        Vector statuses = xmlRpcService.getStatuses("token");

        assertMadeEntities(statuses, "FRU", "Vegetables");
    }

    public void testGetResolutions() throws Exception
    {
        RemoteResolution[] result = new RemoteResolution[2];
        result[0] = new RemoteResolution();
        result[0].setName("FRU");
        result[1] = new RemoteResolution();
        result[1].setName("Vegetables");

        mockSoapService.expects(once()).method("getResolutions").will(returnValue(result));

        Vector resolutions = xmlRpcService.getResolutions("token");

        assertMadeEntities(resolutions, "FRU", "Vegetables");
    }

    public void testGetUser() throws Exception
    {
        RemoteUser result = new RemoteUser();
        result.setName("Bill Atkinson");

        mockSoapService.expects(once()).method("getUser").with(same("token"), same("bill")).will(returnValue(result));

        Hashtable user = xmlRpcService.getUser("token", "bill");

        assertEquals("Bill Atkinson", user.get("name"));
    }

    public void testGetSavedFilters() throws Exception
    {
        RemoteFilter[] result = new RemoteFilter[2];
        result[0] = new RemoteFilter();
        result[0].setName("FRU");
        result[1] = new RemoteFilter();
        result[1].setName("Vegetables");

        mockSoapService.expects(once()).method("getFavouriteFilters").will(returnValue(result));

        Vector savedfilters = xmlRpcService.getSavedFilters("token");

        assertMadeEntities(savedfilters, "FRU", "Vegetables");
    }

    public void testGetFavouriteFilters() throws Exception
    {
        RemoteFilter[] result = new RemoteFilter[2];
        result[0] = new RemoteFilter();
        result[0].setName("FRU");
        result[1] = new RemoteFilter();
        result[1].setName("Vegetables");

        mockSoapService.expects(once()).method("getFavouriteFilters").will(returnValue(result));

        Vector savedfilters = xmlRpcService.getFavouriteFilters("token");

        assertMadeEntities(savedfilters, "FRU", "Vegetables");
    }

    public void testGetComments() throws Exception
    {
        RemoteComment[] result = new RemoteComment[2];
        result[0] = new RemoteComment();
        result[0].setBody("FRU");
        result[1] = new RemoteComment();
        result[1].setBody("Vegetables");

        mockSoapService.expects(once()).method("getComments").will(returnValue(result));

        Vector comments = xmlRpcService.getComments("token", "JRA-52");

        assertEquals(2, comments.size());
        Hashtable commentstruct = (Hashtable) comments.get(0);
        assertEquals("FRU", commentstruct.get("body"));
        Hashtable commentstruct2 = (Hashtable) comments.get(1);
        assertEquals("Vegetables", commentstruct2.get("body"));
    }

    public void testGetIssueWithDependencies() throws Exception
    {
        RemoteIssue result = new RemoteIssue();
        result.setSummary("Bill Atkinson");
        result.setComponents(makeComponents("Walnes Component", "Stevenson Component"));
        result.setFixVersions(makeVersions("FV1", "FV2"));
        result.setAffectsVersions(makeVersions("AV1", "AV2"));

        mockSoapService.expects(once()).method("getIssue").with(same("token"), same("BILL-28")).will(returnValue(result));

        Hashtable issue = xmlRpcService.getIssue("token", "BILL-28");

        assertEquals("Bill Atkinson", issue.get("summary"));
        assertMadeEntities((Vector) issue.get("components"), "Walnes Component", "Stevenson Component");
        assertMadeEntities((Vector) issue.get("fixVersions"), "FV1", "FV2");
        assertMadeEntities((Vector) issue.get("affectsVersions"), "AV1", "AV2");
    }

    public void testGetIssueNoDependencies() throws Exception
    {
        RemoteIssue result = new RemoteIssue();
        result.setSummary("Bill Atkinson");
        mockSoapService.expects(once()).method("getIssue").with(same("token"), same("BILL-28")).will(returnValue(result));
        Hashtable issue = xmlRpcService.getIssue("token", "BILL-28");
        assertEquals("Bill Atkinson", issue.get("summary"));
        assertNull(issue.get("components"));
        assertNull(issue.get("affectsVersions"));
        assertNull(issue.get("fixVersions"));
    }

    public void testCreateIssueNoDependencies() throws Exception
    {
        RemoteIssue expectedRi = new RemoteIssue();
        expectedRi.setSummary("Bill Atkinson");
        Hashtable struct = new Hashtable();
        struct.put("summary", "Bill Atkinson");
        mockSoapService.expects(once()).method("createIssue").with(same("token"), eq(expectedRi)).will(returnValue(expectedRi));
        Hashtable issue = xmlRpcService.createIssue("token", struct);
        assertEquals(struct, issue);
    }

    public void testCreateIssueWithDependencies() throws Exception
    {
        RemoteIssue expectedRi = new RemoteIssue();
        expectedRi.setSummary("Bill Atkinson");
        expectedRi.setComponents(makeComponents("Walnes Component", "Stevenson Component"));
        expectedRi.setFixVersions(makeVersions("FV1", "FV2"));
        expectedRi.setAffectsVersions(makeVersions("AV1", "AV2"));

        mockSoapService.expects(once()).method("createIssue").with(same("token"), eq(expectedRi)).will(returnValue(expectedRi));

        Hashtable struct = new Hashtable();
        struct.put("summary", "Bill Atkinson");
        struct.put("components", makeVector(makeHashtable("name", "Walnes Component"), makeHashtable("name", "Stevenson Component")));
        struct.put("fixVersions", makeVector(makeHashtable("name", "FV1"), makeHashtable("name", "FV2")));
        struct.put("affectsVersions", makeVector(makeHashtable("name", "AV1"), makeHashtable("name", "AV2")));

        Hashtable issue = xmlRpcService.createIssue("token", struct);

        assertEquals("Bill Atkinson", issue.get("summary"));
        assertMadeEntities((Vector) issue.get("components"), "Walnes Component", "Stevenson Component");
        assertMadeEntities((Vector) issue.get("fixVersions"), "FV1", "FV2");
        assertMadeEntities((Vector) issue.get("affectsVersions"), "AV1", "AV2");
    }

    public void testCreateIssueWithCustomFields() throws Exception
    {
        RemoteIssue expectedRi = new RemoteIssue();
        expectedRi.setSummary("Custom Fields");
        expectedRi.setCustomFieldValues(makeCustomFieldValues("customfield_10010", "value 1"));

        mockSoapService.expects(once()).method("createIssue").with(same("token"), eq(expectedRi)).will(returnValue(expectedRi));

        Hashtable struct = new Hashtable();
        struct.put("summary", "Custom Fields");
        struct.put("customFieldValues", makeVector(makeCustomFieldHashtable("customfield_10010", "value 1")));

        Hashtable issue = xmlRpcService.createIssue("token", struct);

        assertEquals("Custom Fields", issue.get("summary"));
        assertNotNull(issue.get("customFieldValues"));
    }

    private RemoteCustomFieldValue[] makeCustomFieldValues(String customFieldId, String value)
    {
        RemoteCustomFieldValue customFieldValue = new RemoteCustomFieldValue(customFieldId, value);

        final RemoteCustomFieldValue[] customFieldValues = new RemoteCustomFieldValue[] { customFieldValue };
        return customFieldValues;
    }

    private Hashtable makeHashtable(String s, String s1)
    {
        Hashtable t = new Hashtable();
        t.put(s, s1);
        return t;
    }

    private Hashtable makeCustomFieldHashtable(String customFieldId, String value)
    {
        Hashtable t = new Hashtable();
        t.put("customfieldId", customFieldId);
        t.put("values", makeVector(value));
        return t;
    }

    private Vector makeVector(Object p0, Object p1)
    {
        Vector v = new Vector(2);
        v.add(p0);
        v.add(p1);
        return v;
    }

    private Vector makeVector(Object p0)
    {
        Vector v = new Vector(1);
        v.add(p0);
        return v;
    }

}
