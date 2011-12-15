/*
* Created by IntelliJ IDEA.
* User: Mike
* Date: Aug 19, 2004
* Time: 9:21:31 AM
*/
package com.atlassian.jira.rpc.exception;

import org.apache.commons.lang.exception.ExceptionUtils;


/**
 * A general exception that occurs remotely.
 */
public class RemoteException extends Exception
{
    ///CLOVER:OFF
    public RemoteException()
    {
    }

    public RemoteException(String s)
    {
        super(s);
    }

    public RemoteException(String s, Throwable throwable)
    {
        super(s + "Caused by " + ExceptionUtils.getStackTrace(throwable));
    }

    public RemoteException(Throwable throwable)
    {
        super(ExceptionUtils.getStackTrace(throwable));
    }
}