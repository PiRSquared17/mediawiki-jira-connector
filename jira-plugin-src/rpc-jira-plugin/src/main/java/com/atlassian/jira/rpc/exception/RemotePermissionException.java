/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 9:21:50 AM
 */
package com.atlassian.jira.rpc.exception;


/**
 * Exception thrown when permissions are violated remotely.
 */
public class RemotePermissionException extends RemoteException
{
    ///CLOVER:OFF
    public RemotePermissionException()
    {
    }

    public RemotePermissionException(String s)
    {
        super(s);
    }

    public RemotePermissionException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public RemotePermissionException(Throwable throwable)
    {
        super(throwable);
    }
}