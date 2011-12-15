package com.atlassian.jira.rpc.soap.util;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.rpc.mock.MockUser;
import com.atlassian.jira.rpc.soap.beans.RemoteAttachment;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.map.EasyMap;
import com.atlassian.multitenant.MultiTenantContext;
import com.opensymphony.user.UserManager;
import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

public class TestSoapUtils extends TestCase
{
    protected void setUp()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
    }

    protected void tearDown()
    {
        UserManager.reset();
        MultiTenantContext.setFactory(null);
    }

    public void testGetAttachments()
    {
        RemoteAttachment[] remoteAttachments = SoapUtils.getAttachments(Collections.EMPTY_LIST);
        assertEquals(0, remoteAttachments.length);

        List attachments = new ArrayList();
        Attachment attachment = createAttachment(1);
        attachments.add(attachment);

        remoteAttachments = SoapUtils.getAttachments(attachments);
        assertEquals(1, remoteAttachments.length);
        assertEquals(new RemoteAttachment(attachment), remoteAttachments[0]);

        attachments.add(createAttachment(2));
        attachments.add(createAttachment(3));
        attachments.add(createAttachment(4));
        remoteAttachments = SoapUtils.getAttachments(attachments);
        assertEquals(4, remoteAttachments.length);
        assertEquals("1", remoteAttachments[0].getId());
        assertEquals("2", remoteAttachments[1].getId());
        assertEquals("3", remoteAttachments[2].getId());
        assertEquals("4", remoteAttachments[3].getId());
    }

    private Attachment createAttachment(long id)
    {
        MockGenericValue attachmentGV = new MockGenericValue("Attachment", EasyMap.build("id", new Long(id), "author", "admin", "filesize", new Long(11), "created", new Timestamp(22), "filename", "file.txt", "mimetype", "text"));
        return new Attachment(null, attachmentGV, null);
    }

    public void testSetUser()
    {
        final User oldUser = new MockUser("oldUser", "old.user", "old@example.com");
        final User targetUser = new MockUser("newUser", "target.user", "target@example.com");

        JiraAuthenticationContext authenticationContext = EasyMock.createMock(JiraAuthenticationContext.class);
        expect(authenticationContext.getLoggedInUser()).andStubReturn(oldUser);
        authenticationContext.setLoggedInUser(targetUser);
        expectLastCall();
        replay(authenticationContext);

        SoapUtilsBean soapUtilsBean = new SoapUtilsBean(null, null, null, authenticationContext, null);

        // did it return the right previous user
        User previousUser = soapUtilsBean.setRemoteUserInJira(targetUser);
        assertSame(previousUser, oldUser);
        verify(authenticationContext); // verify setUser was called
    }
}
