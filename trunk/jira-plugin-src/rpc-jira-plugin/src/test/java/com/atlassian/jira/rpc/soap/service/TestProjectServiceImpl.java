/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 8:59:32 AM
 */
package com.atlassian.jira.rpc.soap.service;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.Selection;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.mock.MockAvatar;
import com.atlassian.jira.rpc.mock.MockAvatarManager;
import com.atlassian.jira.rpc.mock.MockProject;
import com.atlassian.jira.rpc.soap.beans.RemoteAvatar;
import com.atlassian.jira.rpc.soap.beans.RemoteComponent;
import com.atlassian.jira.rpc.soap.beans.RemotePermissionScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import com.atlassian.jira.rpc.soap.beans.RemoteScheme;
import com.atlassian.jira.rpc.soap.beans.RemoteSecurityLevel;
import com.atlassian.jira.rpc.soap.beans.RemoteVersion;
import com.atlassian.jira.rpc.soap.util.MockRemoteEntityFactory;
import com.atlassian.jira.rpc.soap.util.RemoteEntityFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.Base64InputStreamConsumer;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.multitenant.MultiTenantContext;
import com.opensymphony.user.UserManager;
import org.apache.commons.codec.binary.Base64;
import org.easymock.EasyMock;
import org.easymock.MockControl;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.TRUE;

public class TestProjectServiceImpl extends MockObjectTestCase
{
    private Mock mockPerM;
    private Mock mockVerS;
    private Mock mockAppProps;
    private Mock mockNotificationSchemeManager;
    private Mock mockPermissionSchemeManager;
    private Mock mockIssueSecuritySchemeManager;
    private Mock mockProjectService;
    private Mock mockProjectComponentManager;

    private RemoteEntityFactory remoteEntityFactory;

    private ProjectServiceImpl service;
    private GenericValue projectGV;
    private Project project;
    private User user;

    protected void setUp() throws Exception
    {
        super.setUp();
        MultiTenantContextTestUtils.setupMultiTenantSystem();
        mockPerM = new Mock(PermissionManager.class);
        mockVerS = new Mock(VersionService.class);
        mockNotificationSchemeManager = new Mock(NotificationSchemeManager.class);
        mockPermissionSchemeManager = new Mock(PermissionSchemeManager.class);
        mockIssueSecuritySchemeManager = new Mock(IssueSecuritySchemeManager.class);
        mockAppProps = new Mock(ApplicationProperties.class);
        user = ImmutableUser.newUser().name("driver").toUser();
        mockProjectService = new Mock(com.atlassian.jira.bc.project.ProjectService.class);
        mockProjectComponentManager = new Mock(ProjectComponentManager.class);

        remoteEntityFactory = new MockRemoteEntityFactory();

        service = new ProjectServiceImpl(
                (PermissionManager) mockPerM.proxy(),
                (ApplicationProperties) mockAppProps.proxy(),
                (NotificationSchemeManager) mockNotificationSchemeManager.proxy(),
                (PermissionSchemeManager) mockPermissionSchemeManager.proxy(),
                (IssueSecuritySchemeManager) mockIssueSecuritySchemeManager.proxy(),
                null,
                (com.atlassian.jira.bc.project.ProjectService) mockProjectService.proxy(),
                (ProjectComponentManager) mockProjectComponentManager.proxy(),
                (VersionService) mockVerS.proxy(),
                null, null, null, remoteEntityFactory);

        projectGV = new MockGenericValue("Project", EasyMap.build("name", "Bus riding", "key", "BUS"));
        project = new ProjectImpl(projectGV);
    }

    protected void tearDown()
    {
        UserManager.reset();
        MultiTenantContext.setFactory(null);
    }

    public void testCreateProject() throws Exception
    {
        mockAppProps.expects(atLeastOnce()).method("getString").with(eq("jira.baseurl")).will(returnValue("http://server"));
        com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult result =
                new com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult(new SimpleErrorCollection(),
                        "Bus riding", "BUS", "Bus riding description", "driver", "http://www.project.com", null, null);
        com.atlassian.jira.bc.project.ProjectService.UpdateProjectSchemesValidationResult schemesResult =
                new com.atlassian.jira.bc.project.ProjectService.UpdateProjectSchemesValidationResult(
                        new SimpleErrorCollection(), 0L, 1L, null);

        mockProjectService.expects(once()).method("validateCreateProject").with(
                new Constraint[] { eq(user),
                                   eq("Bus riding"),
                                   eq("BUS"),
                                   eq("Bus riding description"),
                                   eq("driver"),
                                   eq("http://www.project.com"),
                                   eq(null) }).will(returnValue(result));
        mockProjectService.expects(once()).method("validateUpdateProjectSchemes").with(
                eq(user), eq(0L), eq(1L), eq(null)).will(returnValue(schemesResult));

        MockGenericValue mockGenericValue = new MockGenericValue("Project",
                EasyMap.build("key", "BUS",
                        "name", "Bus riding",
                        "lead", "driver",
                        "description", "Bus riding description",
                        "url", "http://www.project.com"));
        final ProjectImpl newProject = new ProjectImpl(mockGenericValue);
        mockProjectService.expects(once()).method("createProject").with(eq(result)).will(returnValue(newProject));
        mockProjectService.expects(once()).method("updateProjectSchemes").with(eq(schemesResult), eq(newProject));

        //set scheme expectations
        GenericValue schemeFields = new MockGenericValue("DefaultPermissionScheme", EasyMap.build("id", 0L, "name", "DefaultPermissionScheme"));
        RemotePermissionScheme defaultPermissionScheme = new RemotePermissionScheme(schemeFields);

        GenericValue notificationSchemeFields = new MockGenericValue("DefaultNotificationScheme", EasyMap.build("id", 1L, "name", "DefaultNotificationScheme"));
        RemoteScheme defaultNotificationScheme = new RemoteScheme(notificationSchemeFields, "notification");

        RemoteProject rProject = service.createProject(user, "BUS", "Bus riding", "Bus riding description",
                "http://www.project.com", "driver", defaultPermissionScheme, defaultNotificationScheme, null);

        assertEquals(rProject.getKey(), "BUS");
        assertEquals(rProject.getName(), "Bus riding");
        assertEquals(rProject.getDescription(), "Bus riding description");
        assertEquals(rProject.getProjectUrl(), "http://www.project.com");
        assertEquals("http://server/browse/BUS", rProject.getUrl());
        assertEquals(rProject.getLead(), "driver");
        assertEquals(rProject.getPermissionScheme(), defaultPermissionScheme);
        assertEquals(rProject.getNotificationScheme(), defaultNotificationScheme);
        assertEquals(rProject.getIssueSecurityScheme(), null);
    }

    public void testCreateProjectErrors()
    {
        mockAppProps.expects(atLeastOnce()).method("getString").with(eq("jira.baseurl")).will(returnValue("http://server"));
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("Something bad happened");
        errorCollection.addError("projectKey", "Invalid Key");
        final SimpleErrorCollection schemesErrors = new SimpleErrorCollection();
        schemesErrors.addErrorMessage("PermissionScheme not found with id -999");
        com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult result =
                new com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult(errorCollection);
        com.atlassian.jira.bc.project.ProjectService.UpdateProjectSchemesValidationResult schemesResult =
                new com.atlassian.jira.bc.project.ProjectService.UpdateProjectSchemesValidationResult(schemesErrors);
        mockProjectService.expects(once()).method("validateCreateProject").with(
                new Constraint[] { eq(user),
                                   eq("Bus riding"),
                                   eq("BUS"),
                                   eq("Bus riding description"),
                                   eq("driver"),
                                   eq("http://www.project.com"),
                                   eq(null) }).will(returnValue(result));
        mockProjectService.expects(once()).method("validateUpdateProjectSchemes").with(
                eq(user), eq(-999L), eq(1L), eq(null)).will(returnValue(schemesResult));

        //set scheme expectations
        GenericValue schemeFields = new MockGenericValue("DefaultPermissionScheme", EasyMap.build("id", -999L, "name", "DefaultPermissionScheme"));
        RemotePermissionScheme defaultPermissionScheme = new RemotePermissionScheme(schemeFields);

        GenericValue notificationSchemeFields = new MockGenericValue("DefaultNotificationScheme", EasyMap.build("id", 1L, "name", "DefaultNotificationScheme"));
        RemoteScheme defaultNotificationScheme = new RemoteScheme(notificationSchemeFields, "notification");

        try
        {
            service.createProject(user, "BUS", "Bus riding", "Bus riding description",
                    "http://www.project.com", "driver", defaultPermissionScheme, defaultNotificationScheme, null);
            fail("No validation errors occured");
        }
        catch (RemoteException e)
        {
            assertTrue(e.getMessage().contains("Invalid Key"));
            assertTrue(e.getMessage().contains("Something bad happened"));
            assertTrue(e.getMessage().contains("PermissionScheme not found with id -999"));
        }
    }

    public void testUpdateProject() throws RemoteException
    {
        mockAppProps.expects(atLeastOnce()).method("getString").with(eq("jira.baseurl")).will(returnValue("http://server"));
        com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult result =
                new com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult(new SimpleErrorCollection(),
                        "Bike riding", "BUS", null, "admin", "http://bikes.org", null, null, project);
        com.atlassian.jira.bc.project.ProjectService.UpdateProjectSchemesValidationResult schemesResult =
                new com.atlassian.jira.bc.project.ProjectService.UpdateProjectSchemesValidationResult(
                        new SimpleErrorCollection(), null, null, null);

        mockProjectService.expects(once()).method("validateUpdateProject").with(
                new Constraint[] { eq(user),
                                   eq("Bike riding"),
                                   eq("BUS"),
                                   eq(null),
                                   eq("admin"),
                                   eq("http://bikes.org"),
                                   eq(null) }).will(returnValue(result));
        mockProjectService.expects(once()).method("validateUpdateProjectSchemes").with(
                eq(user), eq(null), eq(null), eq(null)).will(returnValue(schemesResult));

        MockGenericValue mockGenericValue = new MockGenericValue("Project",
                EasyMap.build("key", "BUS",
                        "name", "Bike riding",
                        "lead", "admin",
                        "description", "",
                        "url", "http://bikes.org"));
        final ProjectImpl newProject = new ProjectImpl(mockGenericValue);
        mockProjectService.expects(once()).method("updateProject").with(eq(result)).will(returnValue(newProject));
        mockProjectService.expects(once()).method("updateProjectSchemes").with(eq(schemesResult), eq(newProject));

        GenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("name", "Bike riding", "key", "BUS", "url", "http://bikes.org", "lead", "admin"));
        RemoteProject updateProject = new RemoteProject(mockProjectGV, (ApplicationProperties) mockAppProps.proxy());
        RemoteProject rProject = service.updateProject(user, updateProject);

        assertEquals("BUS", rProject.getKey());
        assertEquals("Bike riding", rProject.getName());
        assertEquals("", rProject.getDescription());
        assertEquals("http://bikes.org", rProject.getProjectUrl());
        assertEquals("http://server/browse/BUS", rProject.getUrl());
        assertEquals("admin", rProject.getLead());
        assertEquals(null, rProject.getPermissionScheme());
        assertEquals(null, rProject.getNotificationScheme());
        assertEquals(null, rProject.getIssueSecurityScheme());
    }

    public void testUpdateProjectWithErrors() throws RemoteException
    {
        mockAppProps.expects(atLeastOnce()).method("getString").with(eq("jira.baseurl")).will(returnValue("http://server"));
        ErrorCollection updateErrors = new SimpleErrorCollection();
        updateErrors.addErrorMessage("Generic update error");
        updateErrors.addError("projectName", "Project with name already exists");
        com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult result =
                new com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult(updateErrors);
        ErrorCollection schemeErrors = new SimpleErrorCollection();
        schemeErrors.addErrorMessage("Permission Scheme with id -999 doesn't exist");
        com.atlassian.jira.bc.project.ProjectService.UpdateProjectSchemesValidationResult schemesResult =
                new com.atlassian.jira.bc.project.ProjectService.UpdateProjectSchemesValidationResult(schemeErrors);

        mockProjectService.expects(once()).method("validateUpdateProject").with(
                new Constraint[] { eq(user),
                                   eq("Bike riding"),
                                   eq("BUS"),
                                   eq(null),
                                   eq("admin"),
                                   eq("http://bikes.org"),
                                   eq(null) }).will(returnValue(result));
        mockProjectService.expects(once()).method("validateUpdateProjectSchemes").with(
                eq(user), eq(-999L), eq(null), eq(null)).will(returnValue(schemesResult));

        GenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("name", "Bike riding", "key", "BUS", "url", "http://bikes.org", "lead", "admin"));
        RemoteProject updateProject = new RemoteProject(mockProjectGV, (ApplicationProperties) mockAppProps.proxy());
        //set scheme expectations
        GenericValue schemeFields = new MockGenericValue("DefaultPermissionScheme", EasyMap.build("id", -999L, "name", "DefaultPermissionScheme"));
        RemotePermissionScheme defaultPermissionScheme = new RemotePermissionScheme(schemeFields);
        updateProject.setPermissionScheme(defaultPermissionScheme);

        try
        {
            service.updateProject(user, updateProject);
            fail("Validation didn't catch incorrect data.");
        }
        catch (RemoteException e)
        {
            assertTrue(e.getMessage().contains("Generic update error"));
            assertTrue(e.getMessage().contains("Project with name already exists"));
            assertTrue(e.getMessage().contains("Permission Scheme with id -999 doesn't exist"));
        }
    }

    public void testDeleteProjectWithError() throws Exception
    {
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Administrative rights are required to remove project");
        com.atlassian.jira.bc.project.ProjectService.DeleteProjectValidationResult result =
                new com.atlassian.jira.bc.project.ProjectService.DeleteProjectValidationResult(errors);

        mockProjectService.expects(once()).method("validateDeleteProject").with(
                new Constraint[] { eq(user),
                                   eq("BUS")
                }).will(returnValue(result));
        try
        {
            service.deleteProject(user, "BUS");
        }
        catch (RemoteException e)
        {
            assertTrue(e.getMessage().contains("Administrative rights are required to remove project"));
        }
    }

    public void testDeleteProjectWithUnexpectedError() throws Exception
    {
        com.atlassian.jira.bc.project.ProjectService.DeleteProjectValidationResult result =
                new com.atlassian.jira.bc.project.ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection());

        mockProjectService.expects(once()).method("validateDeleteProject").with(
                new Constraint[] { eq(user),
                                   eq("BUS")
                }).will(returnValue(result));
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Administrative rights are required to remove project");
        mockProjectService.expects(once()).method("deleteProject").with(eq(user),
                eq(result)
        ).will(returnValue(new com.atlassian.jira.bc.project.ProjectService.DeleteProjectResult(errors)));

        try
        {
            service.deleteProject(user, "BUS");
        }
        catch (RemoteException e)
        {
            assertTrue(e.getMessage().contains("Administrative rights are required to remove project"));
        }
    }

    public void testDeleteProject() throws Exception
    {
        com.atlassian.jira.bc.project.ProjectService.DeleteProjectValidationResult result =
                new com.atlassian.jira.bc.project.ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(),
                        project);
        com.atlassian.jira.bc.project.ProjectService.DeleteProjectResult deleteResult =
                new com.atlassian.jira.bc.project.ProjectService.DeleteProjectResult(new SimpleErrorCollection());

        mockProjectService.expects(once()).method("validateDeleteProject").with(
                new Constraint[] { eq(user),
                                   eq("BUS")
                }).will(returnValue(result));
        mockProjectService.expects(once()).method("deleteProject").with(eq(user), eq(result)).will(returnValue(deleteResult));

        service.deleteProject(user, "BUS");
    }

    public void testGetProjectsWithSchemes() throws Exception
    {
        GenericValue project = new MockGenericValue("Project", EasyMap.build("name", "Bus riding", "key", "BUS"));
        List expected = UtilMisc.toList(project);
        mockPerM.expects(atLeastOnce()).method("getProjects").with(eq(Permissions.BROWSE), ANYTHING).will(returnValue(expected));
        mockPerM.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), ANYTHING).will(returnValue(true));
        mockAppProps.expects(once()).method("getString").with(same(APKeys.JIRA_BASEURL)).will(returnValue("http://server"));

        GenericValue aNotificationScheme = new MockGenericValue("aNotificationScheme", EasyMap.build("id", 0L, "name", "A NotificationScheme"));
        List expectedNotificationSchemes = UtilMisc.toList(aNotificationScheme);
        mockNotificationSchemeManager.expects(once()).method("getSchemes").with(eq(project)).will(returnValue(expectedNotificationSchemes));

        GenericValue defaultPermissionScheme = new MockGenericValue("DefaultPermissionScheme", EasyMap.build("id", 0L, "name", "Default Permission Scheme"));
        List expectedPermissionSchemes = UtilMisc.toList(defaultPermissionScheme);
        mockPermissionSchemeManager.expects(atLeastOnce()).method("getSchemes").with(eq(project)).will(returnValue(expectedPermissionSchemes));
        mockPermissionSchemeManager.expects(atLeastOnce()).method("getEntities").with(ANYTHING, ANYTHING).will(returnValue(new ArrayList()));

        GenericValue anIssueSecurityScheme = new MockGenericValue("IssueSecurityScheme", EasyMap.build("id", 0L, "name", "An IssueSecurityScheme"));
        List expectedIssueSecuritySchemes = UtilMisc.toList(anIssueSecurityScheme);
        mockIssueSecuritySchemeManager.expects(once()).method("getSchemes").with(eq(project)).will(returnValue(expectedIssueSecuritySchemes));
        RemoteProject[] projects = service.getProjects(user, true);
        assertNotNull("No Project", projects[0]);
        assertEquals("Bus riding", projects[0].getName());
        assertEquals("http://server/browse/BUS", projects[0].getUrl());
        assertEquals("A NotificationScheme", projects[0].getNotificationScheme().getName());
        assertEquals("Default Permission Scheme", projects[0].getPermissionScheme().getName());
        assertNotNull(projects[0].getIssueSecurityScheme());
        assertEquals("An IssueSecurityScheme", projects[0].getIssueSecurityScheme().getName());
    }

    public void testGetProjectWithSchemesById() throws Exception
    {
        final Long projectId = 90L;
        final String projectName = "Bus riding";
        final String projectKey = "BUS";
        final GenericValue projectGV = new MockGenericValue("Project", EasyMap.build("id", projectId, "name", projectName, "key", projectKey));
        final MockProject project = new MockProject(projectId, projectKey, projectName, projectGV);
        project.setLead(user);

        mockProjectService.expects(once()).method("getProjectById").with(eq(user), eq(projectId)).
                will(returnValue(new com.atlassian.jira.bc.project.ProjectService.GetProjectResult(new SimpleErrorCollection(), project)));

        mockPerM.expects(atLeastOnce()).method("hasPermission").with(eq(Permissions.ADMINISTER), eq(user)).will(returnValue(true));
        mockAppProps.expects(once()).method("getString").with(same(APKeys.JIRA_BASEURL)).will(returnValue("http://server"));

        GenericValue aNotificationScheme = new MockGenericValue("aNotificationScheme", EasyMap.build("id", 0L, "name", "A NotificationScheme"));
        List expectedNotificationSchemes = UtilMisc.toList(aNotificationScheme);
        mockNotificationSchemeManager.expects(once()).method("getSchemes").with(eq(projectGV)).will(returnValue(expectedNotificationSchemes));

        GenericValue defaultPermissionScheme = new MockGenericValue("DefaultPermissionScheme", EasyMap.build("id", 0L, "name", "Default Permission Scheme"));
        List expectedPermissionSchemes = UtilMisc.toList(defaultPermissionScheme);
        mockPermissionSchemeManager.expects(atLeastOnce()).method("getSchemes").with(eq(projectGV)).will(returnValue(expectedPermissionSchemes));
        mockPermissionSchemeManager.expects(atLeastOnce()).method("getEntities").with(ANYTHING, ANYTHING).will(returnValue(new ArrayList()));

        GenericValue anIssueSecurityScheme = new MockGenericValue("IssueSecurityScheme", EasyMap.build("id", 0L, "name", "An IssueSecurityScheme"));
        List expectedIssueSecuritySchemes = UtilMisc.toList(anIssueSecurityScheme);
        mockIssueSecuritySchemeManager.expects(once()).method("getSchemes").with(eq(projectGV)).will(returnValue(expectedIssueSecuritySchemes));
        RemoteProject rProject = service.getProjectWithSchemesById(user, projectId);
        assertNotNull("No Project", rProject);
        assertEquals(projectName, rProject.getName());
        assertEquals("http://server/browse/BUS", rProject.getUrl());
        assertEquals("A NotificationScheme", rProject.getNotificationScheme().getName());
        assertEquals("Default Permission Scheme", rProject.getPermissionScheme().getName());
        assertNotNull(rProject.getIssueSecurityScheme());
        assertEquals("An IssueSecurityScheme", rProject.getIssueSecurityScheme().getName());
    }

    public void testGetProjectsWithoutSchemes() throws Exception
    {
        GenericValue localproject = new MockGenericValue("Project", EasyMap.build("name", "Bus riding", "key", "BUS"));
        List expected = UtilMisc.toList(localproject);
        mockPerM.expects(atLeastOnce()).method("getProjects").with(eq(Permissions.BROWSE), ANYTHING).will(returnValue(expected));
        mockAppProps.expects(once()).method("getString").with(same(APKeys.JIRA_BASEURL)).will(returnValue("http://server"));
        mockNotificationSchemeManager.expects(once()).method("getSchemes").with(eq(localproject)).will(returnValue(null));
        mockPermissionSchemeManager.expects(once()).method("getSchemes").with(eq(localproject)).will(returnValue(null));
        mockIssueSecuritySchemeManager.expects(once()).method("getSchemes").with(eq(localproject)).will(returnValue(null));
        RemoteProject[] projects = service.getProjects(user, true);
        assertEquals("Bus riding", projects[0].getName());
        assertEquals("http://server/browse/BUS", projects[0].getUrl());
    }

    public void testGetVersions() throws Exception
    {
        addGetProjectAndBrowseExpectations(project);
        Mock mockVersion = new Mock(Version.class);
        mockVersion.expects(once()).method("getId").will(returnValue(10000L));
        mockVersion.expects(once()).method("getName").will(returnValue("Some Version"));
        mockVersion.expects(once()).method("isReleased").will(returnValue(true));
        mockVersion.expects(once()).method("isArchived").will(returnValue(false));
        mockVersion.expects(once()).method("getSequence").will(returnValue(10L));
        final Date date = new Date();
        mockVersion.expects(once()).method("getReleaseDate").will(returnValue(date));

        VersionService.VersionsResult result = new VersionService.VersionsResult(new SimpleErrorCollection(), EasyList.build(mockVersion.proxy()));

        mockVerS.expects(once()).method("getVersionsByProject").with(eq(user), eq(project)).will(returnValue(result));

        RemoteVersion[] versions = service.getVersions(user, "BUS");
        assertEquals("10000", versions[0].getId());
        assertEquals("Some Version", versions[0].getName());
        assertEquals(true, versions[0].isReleased());
        assertEquals(false, versions[0].isArchived());
        assertEquals(10L, (long) versions[0].getSequence());
        assertEquals(date, versions[0].getReleaseDate());
    }

    public void testGetVersionsNoPermissions() throws Exception
    {
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("You do not have permission to browse this project.");
        mockProjectService.expects(once()).method("getProjectByKey").with(eq(user), same("BUS")).will(returnValue(new com.atlassian.jira.bc.project.ProjectService.GetProjectResult(
                errorCollection, null)));

        try
        {
            service.getVersions(user, "BUS");
            fail("Should have barfed");
        }
        //TODO rethink exception type (was RemotePermissionException)
        catch (RemoteException rpe)
        {
            assertMessageContains(rpe, "You do not have permission to browse this project.");
        }
    }

    public void testGetComponents() throws Exception
    {
        addGetProjectAndBrowseExpectations(project);

        Mock mockProjectComponent = new Mock(ProjectComponent.class);
        mockProjectComponent.expects(once()).method("getId").will(returnValue(10000L));
        mockProjectComponent.expects(once()).method("getName").will(returnValue("Some Component"));

        ProjectComponent projectComponent = (ProjectComponent) mockProjectComponent.proxy();

        List expected = EasyList.build(projectComponent);
        mockProjectComponentManager.expects(once()).method("findAllForProject").with(eq(project.getId())).will(returnValue(expected));

        RemoteComponent[] components = service.getComponents(user, "BUS");
        assertEquals("10000", components[0].getId());
        assertEquals("Some Component", components[0].getName());
    }

    private void addGetProjectAndBrowseExpectations(Project project)
    {
        mockProjectService.expects(once()).method("getProjectByKey").with(eq(user), same("BUS")).will(returnValue(new com.atlassian.jira.bc.project.ProjectService.GetProjectResult(new SimpleErrorCollection(), project)));
    }

    public void testGetSecurityLevelsNoPermission() throws Exception
    {
        final MockProject project = new MockProject();

        final MockControl mockPermissionManagerControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mockPermissionManager = (PermissionManager) mockPermissionManagerControl.getMock();
        mockPermissionManager.hasPermission(Permissions.SET_ISSUE_SECURITY, project, (User) null);
        mockPermissionManagerControl.setReturnValue(false);
        mockPermissionManagerControl.replay();

        ProjectServiceImpl service = new ProjectServiceImpl(mockPermissionManager, null, null, null, null, null, null,
                null, null, null, null, null, remoteEntityFactory)
        {
            protected Project retrieveProjectByKey(final String projectKey, final User user)
                    throws RemotePermissionException, RemoteException
            {
                return project;
            }
        };

        final RemoteSecurityLevel[] remoteSecurityLevels = service.getSecurityLevels(null, null);
        assertEquals(0, remoteSecurityLevels.length);
    }

    public void testGetSecurityLevelsDataProblem() throws Exception
    {
        final MockGenericValue projectGV = new MockGenericValue("project");
        final MockProject project = new MockProject()
        {
            public GenericValue getGenericValue()
            {
                return projectGV;
            }
        };

        final MockControl mockPermissionManagerControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mockPermissionManager = (PermissionManager) mockPermissionManagerControl.getMock();
        mockPermissionManager.hasPermission(Permissions.BROWSE, project, (User) null);
        mockPermissionManagerControl.setReturnValue(true);
        mockPermissionManager.hasPermission(Permissions.SET_ISSUE_SECURITY, project, (User) null);
        mockPermissionManagerControl.setReturnValue(true);
        mockPermissionManagerControl.replay();

        final MockControl mockIssueSecurityLevelManagerControl = MockControl.createControl(IssueSecurityLevelManager.class);
        final IssueSecurityLevelManager mockIssueSecurityLevelManager = (IssueSecurityLevelManager) mockIssueSecurityLevelManagerControl.getMock();
        mockIssueSecurityLevelManager.getUsersSecurityLevels(projectGV, (User) null);
        mockIssueSecurityLevelManagerControl.setThrowable(new GenericEntityException());
        mockIssueSecurityLevelManagerControl.replay();

        ProjectServiceImpl service = new ProjectServiceImpl(mockPermissionManager, null, null, null, null, mockIssueSecurityLevelManager, null,
                null, null, null, null, null, remoteEntityFactory)
        {
            protected Project retrieveProjectByKey(final String projectKey, final User user)
                    throws RemotePermissionException, RemoteException
            {
                return project;
            }
        };

        try
        {
            service.getSecurityLevels(null, null);
            fail();
        }
        catch (RemoteException e)
        {
            // expected
            assertMessageContains(e, "Error getting security levels for project 'null'");
        }
    }

    public void testGetSecurityLevels() throws Exception
    {
        final MockGenericValue projectGV = new MockGenericValue("project");
        final MockProject project = new MockProject()
        {
            public GenericValue getGenericValue()
            {
                return projectGV;
            }
        };
        final List securityLevels = EasyList.build(
                createSecurityLevelGenericValue("1", "one", "uno"),
                createSecurityLevelGenericValue("2", "two", "dos"),
                createSecurityLevelGenericValue("3", "three", "tres")
        );

        final MockControl mockPermissionManagerControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mockPermissionManager = (PermissionManager) mockPermissionManagerControl.getMock();
        mockPermissionManager.hasPermission(Permissions.BROWSE, project, (User) null);
        mockPermissionManagerControl.setReturnValue(true);
        mockPermissionManager.hasPermission(Permissions.SET_ISSUE_SECURITY, project, (User) null);
        mockPermissionManagerControl.setReturnValue(true);
        mockPermissionManagerControl.replay();

        final MockControl mockIssueSecurityLevelManagerControl = MockControl.createControl(IssueSecurityLevelManager.class);
        final IssueSecurityLevelManager mockIssueSecurityLevelManager = (IssueSecurityLevelManager) mockIssueSecurityLevelManagerControl.getMock();
        mockIssueSecurityLevelManager.getUsersSecurityLevels(projectGV, (User) null);
        mockIssueSecurityLevelManagerControl.setReturnValue(securityLevels);
        mockIssueSecurityLevelManagerControl.replay();

        ProjectServiceImpl service = new ProjectServiceImpl(mockPermissionManager, null, null, null, null, mockIssueSecurityLevelManager, null,
                null, null, null, null, null, remoteEntityFactory)
        {
            protected Project retrieveProjectByKey(final String projectKey, final User user)
                    throws RemotePermissionException, RemoteException
            {
                return project;
            }
        };

        final RemoteSecurityLevel[] remoteSecurityLevels = service.getSecurityLevels(null, null);
        assertEquals(securityLevels, remoteSecurityLevels);
    }

    public void testGetProjectAvatars() throws Exception
    {
        final PermissionManager alwaysPermitted = getAlwaysPermittedPermissionManager();
        MockAvatarManager mockAvatarManager = new MockAvatarManager()
        {
            public void readLargeAvatarData(final Avatar avatar, final Consumer<InputStream> dataAccessor) throws IOException
            {
                final String string = avatar.getFileName() + " large";
                ByteArrayInputStream is = new ByteArrayInputStream(string.getBytes("UTF-8"));
                dataAccessor.consume(is);
                is.close();
            }
        };
        final MockProject myProject = new MockProject(123L, "BAT");
        mockAvatarManager.addSystemProjectAvatar(1L, "custom.jpg", "image/jpeg");
        mockAvatarManager.addSystemProjectAvatar(2L, "another.bogus", "bogus/type");
        mockAvatarManager.addCustomProjectAvatar(13L, "foo.gif", "image/gif", myProject);
        mockAvatarManager.addCustomProjectAvatar(14L, "yetanother.bogus", "bogus/type", myProject);

        ProjectServiceImpl service = new ProjectServiceImpl(alwaysPermitted, null, null, null, null, null, null, null, null, null, null, mockAvatarManager, remoteEntityFactory)
        {
            protected Project retrieveProjectByKey(final String projectKey, final User user)
                    throws RemotePermissionException, RemoteException
            {
                return myProject;
            }
        };
        RemoteAvatar[] avatars = service.getProjectAvatars(user, "BAT", false, "large");
        assertEquals(2, avatars.length);
        avatars = service.getProjectAvatars(user, "BAT", true, "large");
        assertEquals(4, avatars.length);
        HashSet<Long> expectedIDs = new HashSet<Long>();
        expectedIDs.add(1L);
        expectedIDs.add(2L);
        expectedIDs.add(13L);
        expectedIDs.add(14L);
        HashSet<String> expectedData = new HashSet<String>();
        expectedData.add("custom.jpg large");
        expectedData.add("another.bogus large");
        expectedData.add("foo.gif large");
        expectedData.add("yetanother.bogus large");

        // now assert all expected ids match ids from the list we got
        for (RemoteAvatar avatar : avatars)
        {
            assertTrue(expectedIDs.remove(avatar.getId()));
            final String decoded = base64Decode(avatar.getBase64Data());
            assertTrue(expectedData.remove(decoded));
        }
    }

    public void testSetProjectAvatarCustom() throws Exception
    {
        final MockProject myProject = new MockProject(123L, "BAT", "Fruitbat");
        myProject.setLead(user);
        final byte[] rawImageData = new byte[] { -123, 111, -34, 22, 0, 0, 66 };

        final Avatar mockCreatedAvatar = EasyMock.createMock(Avatar.class);
        MockAvatarManager mockAvatarManager = new MockAvatarManager()
        {
            public Avatar create(final Avatar avatar, final InputStream imageData, final Selection croppingSelection) throws DataAccessException, IOException
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtil.copy(imageData, baos);
                assertTrue(Arrays.equals(rawImageData, baos.toByteArray()));
                return mockCreatedAvatar;
            }
        };

        EasyMock.expect(mockCreatedAvatar.getId()).andReturn(456L);
        com.atlassian.jira.bc.project.ProjectService mockProjectService = EasyMock.createMock(com.atlassian.jira.bc.project.ProjectService.class);
        final com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult result = new com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult(new SimpleErrorCollection());
        EasyMock.expect(mockProjectService.validateUpdateProject(user, "Fruitbat", "BAT", null, user.getName(), null, null, 456L))
                .andReturn(result);
        EasyMock.expect(mockProjectService.updateProject(result)).andReturn(myProject);
        EasyMock.replay(mockCreatedAvatar, mockProjectService);
        ProjectServiceImpl service = new ProjectServiceImpl(getAlwaysPermittedPermissionManager(), null, null, null, null, null, mockProjectService, null, null, null, null, mockAvatarManager, remoteEntityFactory)
        {
            protected Project retrieveProjectByKey(final String projectKey, final User user)
                    throws RemotePermissionException, RemoteException
            {
                return myProject;
            }
        };
        service.setProjectAvatar(user, "BAT", "image/png", base64Encode(rawImageData));
        EasyMock.verify();
    }

    public void testSetProjectAvatarExisting() throws RemoteException
    {
        PermissionManager mockPermissionManager = getAlwaysPermittedPermissionManager();
        final MockProject myProject = new MockProject(123L, "BAT", "Fruitbat");
        myProject.setLead(user);
        final AtomicBoolean calledSetAvatar = new AtomicBoolean(false);
        final com.atlassian.jira.bc.project.ProjectService mps = EasyMock.createMock(com.atlassian.jira.bc.project.ProjectService.class);
        final com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult successResult = new com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult(new SimpleErrorCollection());

        EasyMock.expect(mps.updateProject(successResult)).andReturn(null); // return value is not used in ProjectServiceImpl
        EasyMock.replay(mps);

        ProjectServiceImpl service = new ProjectServiceImpl(mockPermissionManager, null, null, null, null, null, mps, null, null, null, null, null, remoteEntityFactory)
        {
            protected Project retrieveProjectByKey(final String projectKey, final User user)
                    throws RemotePermissionException, RemoteException
            {
                return myProject;
            }

            com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult validateSetAvatar(final User user, final Project project, final Long avatarId)
            {
                assertTrue(project == myProject);
                calledSetAvatar.set(true);
                return successResult;
            }
        };
        service.setProjectAvatar(user, "BAT", 111L);
        assertTrue(calledSetAvatar.get());
        EasyMock.verify();
    }

    public void testGetProjectAvatar() throws Exception
    {
        PermissionManager alwaysPermitted = getAlwaysPermittedPermissionManager();
        final Avatar myAvatar = new MockAvatar(123L, "somefile.png", "content/type", "foucault", true);
        final RemoteAvatar remoteAvatar = new RemoteAvatar(234L, "another/contenttype", "sartre", false, "project", "123");
        final MockProject myProject = new MockProject(123L, "BAT", "Fruitbat");
        myProject.setAvatar(myAvatar);
        final AtomicBoolean createCalled = new AtomicBoolean(false);
        service = new ProjectServiceImpl(alwaysPermitted, null, null, null, null, null, null, null, null, null, null, null, remoteEntityFactory)
        {
            protected Project retrieveProjectByKey(final String projectKey, final User u)
                    throws RemotePermissionException, RemoteException
            {
                assertEquals("FOO", projectKey);
                assertEquals(user, u);
                return myProject;
            }

            RemoteAvatar createRemoteAvatar(final Avatar a, final boolean large, final Base64InputStreamConsumer base64InputStreamConsumer)
            {
                createCalled.set(true);
                assertFalse(large);
                assertTrue(myAvatar == a); // asserting proper delegation
                return remoteAvatar;
            }
        };
        final RemoteAvatar retrievedRemoteAvatar = service.getProjectAvatar(user, "FOO", "nano");
        assertTrue(createCalled.get());
        assertEquals(remoteAvatar, retrievedRemoteAvatar);
    }

    public void testCreateRemoteAvatar() throws Exception
    {
        final MockAvatar a = new MockAvatar(123L, "file", "foo/bar", "kirkegaard", false);
        final Base64InputStreamConsumer streamConsumer = new Base64InputStreamConsumer(true)
        {
            public String getEncoded()
            {
                return "encodedData";
            }
        };

        MockAvatarManager mockAvatarManager = new MockAvatarManager()
        {
            public void readLargeAvatarData(final Avatar avatar, final Consumer<InputStream> dataAccessor) throws IOException
            {
                assertEquals(a, avatar);
                assertEquals(streamConsumer, dataAccessor);
            }
        };

        service = new ProjectServiceImpl(null, null, null, null, null, null, null, null, null, null, null, mockAvatarManager, remoteEntityFactory);
        final RemoteAvatar remoteAvatar = service.createRemoteAvatar(a, true, streamConsumer);
        assertEquals(123L, remoteAvatar.getId());
        assertEquals("foo/bar", remoteAvatar.getContentType());
        assertEquals("kirkegaard", remoteAvatar.getOwner());
        assertEquals(false, remoteAvatar.isSystem());
        assertEquals(Avatar.Type.PROJECT.getName(), remoteAvatar.getType());
        assertEquals("encodedData", remoteAvatar.getBase64Data());
    }

    public void testDeleteAvatar() throws Exception
    {

        PermissionManager alwaysPermitted = getAlwaysPermittedPermissionManager();

        final AtomicBoolean deleteCalled = new AtomicBoolean(false);
        MockAvatarManager mockAvatarManager = new MockAvatarManager()
        {
            public boolean delete(final Long avatarId, final boolean alsoDeleteAvatarFile)
            {
                deleteCalled.set(true);
                return true;
            }
        };
        final MockProject myProject = new MockProject(888L);
        final MockAvatar avatarToDelete = mockAvatarManager.addCustomProjectAvatar(12L, "file", "image/gif", myProject);
        myProject.setAvatar(avatarToDelete);
        mockAvatarManager.setDefaultAvatarId(111L);

        final com.atlassian.jira.bc.project.ProjectService mps = EasyMock.createMock(com.atlassian.jira.bc.project.ProjectService.class);
        final com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult successResult = new com.atlassian.jira.bc.project.ProjectService.UpdateProjectValidationResult(new SimpleErrorCollection());

        EasyMock.expect(mps.updateProject(successResult)).andReturn(null); // return value is not used in ProjectServiceImpl
        EasyMock.replay(mps);

        service = new ProjectServiceImpl(alwaysPermitted, null, null, null, null, null,mps, null, null, null, null, mockAvatarManager, remoteEntityFactory)
        {
            @Override
            protected Project retrieveProjectById(final Long id, final User user) throws RemoteException
            {
                if (!id.equals(888L))
                {
                    fail();
                }
                return myProject;
            }

            ProjectService.UpdateProjectValidationResult validateSetAvatar(final User u, final Project project, final Long avatarId)
            {
                assertEquals(user, u);
                assertEquals(myProject, project);
                assertEquals((Long)111L, avatarId); // expecting to be updated to the defalut avatar
                return successResult;
            }
        };

        service.deleteProjectAvatar(user, 12L);
        assertTrue(deleteCalled.get());
        EasyMock.verify();
    }

    /*
    * HELPERS
    */

    private PermissionManager getAlwaysPermittedPermissionManager()
    {
        return (PermissionManager) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { PermissionManager.class }, new InvocationHandler()
        {
            public Object invoke(final Object o, final Method method, final Object[] objects) throws Throwable
            {
                if (method.getReturnType().equals(Boolean.TYPE) && method.getName().startsWith("has"))
                {
                    return TRUE;
                }
                else
                {
                    throw new UnsupportedOperationException();
                }
            }
        });
    }

    private String base64Decode(final String base64Data) throws UnsupportedEncodingException
    {
        return new String(Base64.decodeBase64(base64Data.getBytes("UTF-8")), "UTF-8");
    }

    private String base64Encode(final byte[] encodeMe) throws UnsupportedEncodingException
    {
        return new String(Base64.encodeBase64Chunked(encodeMe), "UTF-8");
    }

    private void assertMessageContains(final RemoteException e, final String msg)
    {
        assertTrue(e.toString().contains(msg));
    }

    private void assertEquals(List<GenericValue> l, RemoteSecurityLevel[] levels)
    {
        assertEquals(l.size(), levels.length);
        for (RemoteSecurityLevel remoteSecurityLevel : levels)
        {
            final GenericValue genericValue = createSecurityLevelGenericValue(
                    remoteSecurityLevel.getId(),
                    remoteSecurityLevel.getName(),
                    remoteSecurityLevel.getDescription());
            assertTrue(l.contains(genericValue));
        }
    }

    private GenericValue createSecurityLevelGenericValue(String id, String name, String description)
    {
        return new MockGenericValue("securityLevel", EasyMap.build(
                "id", id,
                "name", name,
                "description", description
        ));
    }

}