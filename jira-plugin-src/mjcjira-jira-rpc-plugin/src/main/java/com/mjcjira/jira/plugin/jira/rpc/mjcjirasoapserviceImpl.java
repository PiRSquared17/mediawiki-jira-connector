package com.mjcjira.jira.plugin.jira.rpc;

import com.mjcjira.jira.plugin.jira.rpc.mjcjirasoapservice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rpc.exception.*;
import com.atlassian.jira.rpc.auth.*;
import com.atlassian.jira.rpc.soap.beans.*;
import com.atlassian.jira.rpc.soap.service.*;
import com.atlassian.jira.rpc.soap.util.*;
import com.atlassian.jira.rpc.soap.JiraSoapServiceImpl;
import com.atlassian.jira.soap.axis.JiraSoapTokenResolver;
import org.apache.axis.encoding.Base64;

import java.util.Date;


public class mjcjirasoapserviceImpl extends JiraSoapServiceImpl implements mjcjirasoapservice{
    private static final Logger log = LoggerFactory.getLogger(mjcjirasoapserviceImpl.class);
	
    private TokenManager tokenManager;
    private final ProjectService projectService;
    private final IssueService issueService;
    private final UserService userService;
    private final SchemeService schemeService;
    private final AdminService adminService;
    private final SearchService searchService;
    private final ProjectRoleService projectRoleService;
    private final IssueConstantsService issueConstantsService;
	
    public mjcjirasoapserviceImpl(TokenManager tokenManager, ProjectService projectService, IssueService issueService, UserService userService, SchemeService schemeService, AdminService adminService, SearchService searchService, ProjectRoleService projectRoleService, IssueConstantsService issueConstantsService) {
		super(tokenManager, projectService, issueService, userService, schemeService, adminService, searchService, projectRoleService, issueConstantsService);
        this.tokenManager = tokenManager;
        this.projectService = projectService;
        this.issueService = issueService;
        this.schemeService = schemeService;
        this.userService = userService;
        this.adminService = adminService;
        this.searchService = searchService;
        this.projectRoleService = projectRoleService;
        this.issueConstantsService = issueConstantsService;
		
    }
	
	public String getAttachmentIdFromIssueKeyAndName(String token, String issueKey, String attachmentName) throws RemoteException
	{
        User user = tokenManager.retrieveUser(token);
		
		RemoteAttachment[] remoteAttachmentArray = issueService.getAttachmentsFromIssue(user, issueKey);
		
		String attachmentId = "FALSE";
		
		for (RemoteAttachment rmAttm : remoteAttachmentArray) {
			String attachmentNameFromSoap = rmAttm.getFilename();
			
			if (attachmentNameFromSoap.equals(attachmentName)) {
				attachmentId = rmAttm.getId();
			}
		}

//		String attachmentId = (remoteAttachmentArray[0]).getId();
		
		return attachmentId;
	}

}