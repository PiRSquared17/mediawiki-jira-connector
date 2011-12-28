package com.mjcjira.jira.plugin.jira.rpc;

import com.atlassian.jira.rpc.exception.*;
import com.atlassian.jira.rpc.auth.*;
import com.atlassian.jira.rpc.soap.beans.*;
import com.atlassian.jira.rpc.soap.service.*;
import com.atlassian.jira.rpc.soap.util.*;
import com.atlassian.jira.rpc.soap.JiraSoapService;

import java.util.Date;


public interface mjcjirasoapservice extends JiraSoapService {

	String getCustomFieldNameFromId(String token, String fieldId) throws RemoteException;

}