package org.stuctmapper.utils;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <K, V> void addNew(final Map<K, V> map, final K key, final V value) {
        final V prev = map.put(key, value);
        Preconditions.checkArgument(prev == null, "already added %s to %s", key, map);
    }

    public static <V> void addNew(final Set<V> set, final V value) {
        final boolean added = set.add(value);
        Preconditions.checkArgument(added, "already added %s to %s", value, set);
    }
}
