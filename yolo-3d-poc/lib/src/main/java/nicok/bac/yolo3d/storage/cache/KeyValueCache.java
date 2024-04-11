package nicok.bac.yolo3d.storage.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Key value cache with a fixed capacity.
 */
public class KeyValueCache<K, V> extends LinkedHashMap<K, V> {

    protected final long capacity;

    public KeyValueCache(final int capacity) {
        super(capacity + 1, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
