package com.example.petservice.cache;

public final class CacheKeys {

    private CacheKeys() {
    }

    public static String petId(Object petId) {
        return String.valueOf(petId);
    }

    public static String petListPage(Integer currentPage, Integer pageSize) {
        int current = (currentPage != null && currentPage > 0) ? currentPage : 1;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;
        return "page:" + current + ":" + size;
    }
}

