package com.rm.cameraphone.util;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex
 */
public class SharedMap {

    private static volatile HashMap<String, WeakReference<Object>> sDataPool = null;
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
        sDataPool = new HashMap<>();
    }

    public synchronized void put(String key, @NonNull WeakReference<Object> value) {
        flush();
        sDataPool.put(key, value);
    }

    public synchronized Object get(String key) {
        WeakReference<Object> ref = sDataPool.get(key);
        if (ref != null) return ref.get();

        return null;
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
