package com.example.petservice.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PetCacheInvalidator {

    private final CacheManager cacheManager;

    public void evictPetDetail(Object petId) {
        evict(CacheNames.PET_DETAIL, CacheKeys.petId(petId));
    }

    public void evictPetListPages() {
        clear(CacheNames.PET_LIST_PAGE);
    }

    private void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }
        cache.evict(key);
    }

    private void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }
        cache.clear();
    }
}

