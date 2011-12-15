package com.atlassian.jira.rpc;

import org.apache.commons.beanutils.BeanUtils;

import java.util.Hashtable;
import java.util.Vector;

public class RpcUtils
{
    public static Vector makeVector(Object[] objects)
    {
        Vector result = new Vector(objects.length);

        for (int i = 0; i < objects.length; i++)
        {
            result.add(makeStruct(objects[i]));
        }

        return result;
    }

    public static Hashtable makeStruct(Object object)
    {
        try
        {
            // make a Hashtable, removing all null values from the Hashtable
            // note we also don't add class as it's not a real property!
            Hashtable result = new Hashtable(BeanUtils.describe(object))
            {
                public synchronized Object put(Object key, Object value)
                {
                    if (value == null || key == null || "class".equals(key))
                    {
                        return null;
                    }
                    return super.put(key, value);
                }
            };

            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
