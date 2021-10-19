package com.touscm.deliver.base.utils;

import java.util.List;
import java.util.Map;

public class CollectionUtils {

    public static <T> boolean isEmpty(List<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    public static <T> boolean isNotEmpty(List<T> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return map != null && !map.isEmpty();
    }

}
