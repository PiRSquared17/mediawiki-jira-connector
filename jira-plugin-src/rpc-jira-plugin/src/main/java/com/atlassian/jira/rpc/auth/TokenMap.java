/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

/*
 */
package com.atlassian.jira.rpc.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A simple map with a timeout on the get/put methods.
 */
public class TokenMap<K, V> extends HashMap<K, V>
{
    private final long tokenTimeout;
    private final Map<Object, Long> tokenTimeouts;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock r = rwLock.readLock();
    private final Lock w = rwLock.writeLock();

    public TokenMap(long tokenTimeout)
    {
        this.tokenTimeout = tokenTimeout;
        this.tokenTimeouts = new HashMap<Object, Long>();
    }

    public V put(final K key, final V value)
    {
        w.lock();
        try
        {
            tokenTimeouts.put(key, nextExpiryTime());
            return super.put(key, value);
        }
        finally
        {
            w.unlock();
        }
    }

    private Long nextExpiryTime()
    {
        return System.currentTimeMillis() + tokenTimeout;
    }

    public V get(Object key)
    {
        r.lock();
        try
        {
            if (!super.containsKey(key))
            {
                return null;
            }

            Long expiryTime = tokenTimeouts.get(key);
            if (expiryTime == null)
            {
                tokenTimeouts.remove(key);
                super.remove(key);
                return null;
            }
            else if (expiryTime < System.currentTimeMillis()) // expired!
            {
                tokenTimeouts.remove(key);
                super.remove(key);
                return null;
            }
            else // we're still timed in, extend another timeout
            {
                tokenTimeouts.put(key, nextExpiryTime());
            }

            return super.get(key);
        }
        finally
        {
            r.unlock();
        }
    }

    public boolean containsKey(final Object key)
    {
        r.lock();
        try
        {
            return super.containsKey(key);
        }
        finally
        {
            r.unlock();
        }
    }

    public V remove(Object key)
    {
        w.lock();
        try
        {
            tokenTimeouts.remove(key);
            return super.remove(key);
        }
        finally
        {
            w.unlock();
        }
    }

    public void clear()
    {
        w.lock();
        try
        {
            tokenTimeouts.clear();
            super.clear();
        }
        finally
        {
            w.unlock();
        }
    }
}
