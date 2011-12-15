package com.atlassian.jira.rpc.soap.service;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import com.atlassian.jira.rpc.soap.beans.RemoteProjectRole;
import com.atlassian.jira.rpc.soap.util.MockRemoteEntityFactory;
import com.atlassian.jira.rpc.soap.util.RemoteEntityFactory;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.util.EasyList;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: detkin Date: Jun 14, 2006 Time: 11:46:24 AM To change this template use File |
 * Settings | File Templates.
 */
public class TestProjectRoleService extends MockObjectTestCase
{
    private com.atlassian.jira.rpc.soap.service.ProjectRoleService remoteProjectRoleService = null;

    private Mock projectRoleService = null;
    private Mock projectManager = null;
    private Mock projectFactory = null;

    private User user = null;
    private ProjectRole projectRole;
    private RemoteProjectRole remoteProjectRole;
    private GenericValue projectGV;
    private RemoteProject remoteProject;
    private Project project;


    protected void setUp() throws Exception
    {
        super.setUp();

        user = ImmutableUser.newUser().name("driver").emailAddress("test@test.com").displayName("Test User").toUser();

        projectRoleService = new Mock(com.atlassian.jira.bc.projectroles.ProjectRoleService.class);
        projectManager = new Mock(ProjectManager.class);
        projectFactory = new Mock(ProjectFactory.class);
        final RemoteEntityFactory remoteEntityFactory = new MockRemoteEntityFactory();

        remoteProjectRoleService = new com.atlassian.jira.rpc.soap.service.ProjectRoleServiceImpl(
                (com.atlassian.jira.bc.projectroles.ProjectRoleService) projectRoleService.proxy(),
                (ProjectManager) projectManager.proxy(),
                (ProjectFactory) projectFactory.proxy(),
                remoteEntityFactory);
        projectRole = new ProjectRoleImpl("role1", "desc");
        remoteProjectRole = new RemoteProjectRole(projectRole.getId(), projectRole.getName(), projectRole.getDescription());

        Map projectFields = UtilMisc.toMap("id", "1",
                "name", "Bus riding",
                "url", "http://www.project.com",
                "description", "Bus riding",
                "key", "BUS",
                "lead", "driver");

        projectGV = new MockGenericValue("Project", projectFields);
        remoteProject = new RemoteProject();
        remoteProject.setId("1");

        project = new ProjectImpl(projectGV);

    }


    // public Collection getProjectRoles(User currentUser) throws RemoteException;
    public void testGetProjectRoles() throws RemoteException
    {
        // Setup the roles to return
        ProjectRole role = new ProjectRoleImpl("role1", "desc");
        ProjectRole role2 = new ProjectRoleImpl("role2", "desc");
        ProjectRole role3 = new ProjectRoleImpl("role3", "desc");

        // Set up the expectations of the service called by the remoteProjectRoleService
        List roles = EasyList.build(role, role2, role3);

        projectRoleService.expects(once()).method("getProjectRoles").with(eq(user), ANYTHING).will(returnValue(roles));

        // Make the actual call
        RemoteProjectRole[] remoteRoles = remoteProjectRoleService.getProjectRoles(user);

        assertNotNull(remoteRoles);
    }

    public void testGetProjectRole() throws RemoteException
    {
        // The id for the RemoteProjectRole
        Long id = new Long(0);

        // Prepare the internal call
        ProjectRole projectRole = new ProjectRoleImpl(id, "role1", "desc");
        projectRoleService.expects(once()).method("getProjectRole").with(eq(user), eq(id), ANYTHING).will(returnValue(projectRole));

        RemoteProjectRole remoteProjectRole = remoteProjectRoleService.getProjectRole(user, id);

        assertNotNull(remoteProjectRole);
        assertEquals(remoteProjectRole.getId(), projectRole.getId());

    }

    public void testCreateRole() throws RemoteException
    {
        projectRoleService.expects(once()).method("createProjectRole").with(eq(user), eq(projectRole), ANYTHING).will(returnValue(projectRole));

        RemoteProjectRole returnedProjectRole = remoteProjectRoleService.createProjectRole(user, remoteProjectRole);

        assertNotNull(returnedProjectRole);

        assertEquals(returnedProjectRole.getName(), remoteProjectRole.getName());

    }

    public void testIsRoleNameUnique() throws RemoteException
    {
        String projectRoleName = "test";
        projectRoleService.expects(once()).method("isProjectRoleNameUnique").with(eq(user), eq(projectRoleName), ANYTHING).will(returnValue(true));

        boolean returnVal = remoteProjectRoleService.isProjectRoleNameUnique(user, projectRoleName);
        assertTrue(returnVal);

    }

    public void testDeleteRole() throws RemoteException
    {
        projectRoleService.expects(once()).method("deleteProjectRole").with(eq(user), eq(projectRole), ANYTHING);

        remoteProjectRoleService.deleteProjectRole(user, remoteProjectRole, true);
    }

    public void testAddActorsToProjectRole() throws RemoteException
    {
        // Build and setup the mocks
        String[] actors = new String[] { "admin", "fred", "tom" };

        Constraint[] constraints = { eq(user), eq(Arrays.asList(actors)), eq(projectRole), eq(project), eq(UserRoleActorFactory.TYPE), ANYTHING };

        projectRoleService.expects(once()).method("addActorsToProjectRole").with(constraints);
        projectManager.expects(once()).method("getProject").with(eq(new Long(remoteProject.getId()))).will(returnValue(projectGV));
        projectFactory.expects(once()).method("getProject").with(eq(projectGV)).will(returnValue(project));

        remoteProjectRoleService.addActorsToProjectRole(user, actors, remoteProjectRole, remoteProject, UserRoleActorFactory.TYPE);
    }

    public void testRemoveActorsFromProjectRole() throws RemoteException
    {
        // Build and setup the mocks
        String[] actors = new String[] { "admin", "fred", "tom" };
        Constraint[] constraints = { eq(user), eq(Arrays.asList(actors)), eq(projectRole), eq(project), eq(UserRoleActorFactory.TYPE), ANYTHING };

        projectRoleService.expects(once()).method("removeActorsFromProjectRole").with(constraints);
        projectManager.expects(once()).method("getProject").with(eq(new Long(remoteProject.getId()))).will(returnValue(projectGV));
        projectFactory.expects(once()).method("getProject").with(eq(projectGV)).will(returnValue(project));

        remoteProjectRoleService.removeActorsFromProjectRole(user, actors, remoteProjectRole, remoteProject, UserRoleActorFactory.TYPE);
    }

    public void testUpdateRole() throws RemoteException
    {
        projectRoleService.expects(once()).method("updateProjectRole").with(eq(user), eq(projectRole), ANYTHING);

        remoteProjectRoleService.updateProjectRole(user, remoteProjectRole);
    }

}
