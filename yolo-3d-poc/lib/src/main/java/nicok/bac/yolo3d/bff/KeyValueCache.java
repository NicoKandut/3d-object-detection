package nicok.bac.yolo3d.bff;

import java.util.LinkedHashMap;
import java.util.Map;

public class KeyValueCache<K, V> extends LinkedHashMap<K, V> {

    private final long capacity;

    public KeyValueCache(final int capacity) {
        super(capacity + 1, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
