package com.mjcjira.jira.plugin.jira.rpc;

import com.atlassian.jira.rpc.exception.*;
import com.atlassian.jira.rpc.auth.*;
import com.atlassian.jira.rpc.soap.beans.*;
import com.atlassian.jira.rpc.soap.service.*;
import com.atlassian.jira.rpc.soap.util.*;
import com.atlassian.jira.rpc.soap.JiraSoapService;

import java.util.Date;


public interface mjcjirasoapservice extends JiraSoapService {

    String getAttachmentIdFromIssueKeyAndName(String token, String issueKey, String attachmentName) throws RemoteException;

}