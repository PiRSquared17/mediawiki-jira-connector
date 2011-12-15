package com.atlassian.jira.rpc.soap.beans;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Represents the time zone information of the JIRA server.
 *
 * @since v3.13.3
 */
public class RemoteTimeInfo
{
    public static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    String serverTime;
    String timeZoneId;

    public RemoteTimeInfo()
    {
        this.serverTime = new SimpleDateFormat(ISO8601_FORMAT).format(new Date());
        this.timeZoneId = TimeZone.getDefault().getID();
    }

    /**
     * @return the current time on the server, in ISO8601 format: yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public String getServerTime()
    {
        return serverTime;
    }

    /**
     * @return the time zone id of the server; used for {@link TimeZone#getTimeZone(String)}
     */
    public String getTimeZoneId()
    {
        return timeZoneId;
    }
}
