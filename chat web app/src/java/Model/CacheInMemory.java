package Model;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;

/**
 *
 * @author AMMAROV
 */
public class CacheInMemory<K, T>
{

    private long timeToLive;
    private LRUMap cacheMap;

    protected class CacheObject
    {

        public long lastAccessed = System.currentTimeMillis();
        public T value;

        protected CacheObject(T value)
        {
            this.value = value;
        }
    }

    public CacheInMemory(int maxItems)
    {
        cacheMap = new LRUMap(maxItems);
    }
    
    public CacheInMemory(long timeToLive, final long timerInterval, int maxItems)
    {
        this.timeToLive = timeToLive * 1000;

        cacheMap = new LRUMap(maxItems);

        if (timeToLive > 0 && timerInterval > 0)
        {

            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    while (true)
                    {
                        try
                        {
                            Thread.sleep(timerInterval * 1000);
                        } 
                        catch (InterruptedException ex)
                        {}
                        cleanup();
                    }
                }
            });

            t.setDaemon(true);
            t.start();
        }
    }

    public void put(K key, T value)
    {
        synchronized (cacheMap)
        {
            cacheMap.put(key, new CacheObject(value));
        }
    }

    @SuppressWarnings("unchecked")
    public T get(K key)
    {
        synchronized (cacheMap)
        {
            CacheObject c = (CacheObject) cacheMap.get(key);

            if (c == null)
            {
                return null;
            }
            else
            {
                c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }

    public void remove(K key)
    {
        synchronized (cacheMap)
        {
            cacheMap.remove(key);
        }
    }

    public int size()
    {
        synchronized (cacheMap)
        {
            return cacheMap.size();
        }
    }

    @SuppressWarnings("unchecked")
    public void cleanup()
    {

        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;

        synchronized (cacheMap)
        {
            MapIterator itr = cacheMap.mapIterator();

            deleteKey = new ArrayList<K>((cacheMap.size() / 2) + 1);
            K key = null;
            CacheObject c = null;

            while (itr.hasNext())
            {
                key = (K) itr.next();
                c = (CacheObject) itr.getValue();

                if (c != null && (now > (timeToLive + c.lastAccessed)))
                {
                    deleteKey.add(key);
                }
            }
        }

        for (K key : deleteKey)
        {
            synchronized (cacheMap)
            {
                cacheMap.remove(key);
            }

            Thread.yield();
        }
    }
}
