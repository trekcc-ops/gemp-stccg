package com.gempukku.stccg.async;

import java.util.Collection;
import java.util.HashSet;

public class CacheManager {
    private final Collection<Cached> _caches = new HashSet<>();

    public CacheManager(Cached... params) {
        for (Cached cachedObject : params)
            addCache(cachedObject);
    }

    public void addCache(Cached cached) {
        _caches.add(cached);
    }

    public void clearCaches() {
        for (Cached cache : _caches)
            cache.clearCache();
    }

    public int getTotalCount() {
        int total = 0;
        for (Cached cache : _caches)
            total+=cache.getItemCount();
        return total;
    }
}