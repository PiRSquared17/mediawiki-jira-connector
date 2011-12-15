/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 9:41:57 AM
 */
package com.atlassian.jira.rpc.exception;

import com.atlassian.jira.util.ErrorCollection;

import java.util.Iterator;

/**
 * Exception thrown when remote data does not validate properly.
 */
public class RemoteValidationException extends RemoteException
{
    ///CLOVER:OFF
    public RemoteValidationException()
    {
    }

    public RemoteValidationException(String s)
    {
        super(s);
    }

    public RemoteValidationException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public RemoteValidationException(Throwable throwable)
    {
        super(throwable);
    }

    public RemoteValidationException(String s, ErrorCollection errorCol)
    {
        super(makeNiceMessage(s, errorCol));
    }

    private static String makeNiceMessage(String s, ErrorCollection errorCol)
    {
        StringBuffer message = new StringBuffer(s);
        message.append(" ");


        if (errorCol.getErrors() != null)
        {
            for (Iterator iterator = errorCol.getErrors().keySet().iterator(); iterator.hasNext();)
            {
                String field = (String) iterator.next();
                message.append(field).append(":").append(errorCol.getErrors().get(field)).append(" ");
            }
        }

        if (errorCol.getErrorMessages() != null)
        {
            for (Iterator iterator = errorCol.getErrorMessages().iterator(); iterator.hasNext();)
            {
                String errorMessage = (String) iterator.next();
                message.append(errorMessage).append(" ");
            }
        }
        return message.toString();
    }
}