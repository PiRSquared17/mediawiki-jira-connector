package com.atlassian.jira.rpc.soap.beans;

import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentImpl;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.rpc.mock.MockProjectRoleManager;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.ImmutableException;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.sql.Timestamp;
import java.util.Date;

public class TestRemoteComment extends MockObjectTestCase
{
    private Timestamp currentDateTime = null;
    private static final String COMMENT_BODY = "The comment";
    private static final String COMMENT_AUTHOR = "commenter";
    private static final Long COMMENT_ID = new Long(10);
    private static final Long COMMENT_ROLE_LEVEL = MockProjectRoleManager.PROJECT_ROLE_TYPE_1.getId();

    protected void setUp() throws Exception
    {
        super.setUp();
        currentDateTime = new Timestamp(System.currentTimeMillis());
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        currentDateTime = null;
    }

    public void testObjectConstructorAndSetters() throws ImmutableException, DuplicateEntityException
    {
        Mock mockCommentManager = new Mock(CommentManager.class);
        mockCommentManager.expects(once()).method("getProjectRole").with(eq(COMMENT_ROLE_LEVEL)).will(returnValue(MockProjectRoleManager.PROJECT_ROLE_TYPE_1));

        // Role level is a string in the remote comment, so this cannot be validated
        Comment comment = new CommentImpl((CommentManager) mockCommentManager.proxy(), COMMENT_AUTHOR, COMMENT_AUTHOR, COMMENT_BODY, null, COMMENT_ROLE_LEVEL, currentDateTime, currentDateTime, null)
        {
            public Long getId()
            {
                return COMMENT_ID;
            }
        };

        RemoteComment rComment = new RemoteComment(comment);

        assertEquals(COMMENT_BODY, rComment.getBody());
        assertEquals(COMMENT_ID.toString(), rComment.getId());
        assertEquals(MockProjectRoleManager.PROJECT_ROLE_TYPE_1.getName(), rComment.getRoleLevel());
        assertEquals(COMMENT_AUTHOR, rComment.getAuthor());
        assertEquals(new Date(currentDateTime.getTime()), rComment.getCreated());

        // set and check
        rComment.setBody("foo");
        assertEquals("foo", rComment.getBody());
        rComment.setGroupLevel("foo");
        assertEquals("foo", rComment.getGroupLevel());
    }
}