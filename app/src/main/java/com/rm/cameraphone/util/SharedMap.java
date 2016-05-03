package com.rm.cameraphone.util;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alex
 */
public class SharedMap {

    private static volatile ConcurrentHashMap<String, WeakReference<Object>> sDataPool = null;
    private static volatile SharedMap sHolder = null;

    public static SharedMap holder() {
        if (sHolder == null) {
            synchronized (SharedMap.class) {
                if (sHolder == null) {
                    sHolder = new SharedMap();
                }
            }
        }

        return sHolder;
    }

    private SharedMap() {
        sDataPool = new ConcurrentHashMap<>();
    }

    public synchronized void put(String key, @NonNull WeakReference<Object> value) {
        flush();
        sDataPool.put(key, value);
    }

    public synchronized Object get(String key) {
        return sDataPool.get(key).get();
    }

    public synchronized void clear() {
        sDataPool.clear();
    }

    private void flush() {
        for (Map.Entry<String, WeakReference<Object>> entry : sDataPool.entrySet()) {
            if (entry.getValue().get() == null) {
                sDataPool.remove(entry.getKey());
            }
        }
    }
}
