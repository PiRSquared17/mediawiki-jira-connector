/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 9:25:05 AM
 */
package com.atlassian.jira.rpc.exception;


/**
 * An exception thrown for remote authentication failures or errors.
 */
public class RemoteAuthenticationException extends RemoteException
{
    ///CLOVER:OFF
    public RemoteAuthenticationException()
    {
    }

    public RemoteAuthenticationException(String s)
    {
        super(s);
    }

    public RemoteAuthenticationException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public RemoteAuthenticationException(Throwable throwable)
    {
        super(throwable);
    }
}