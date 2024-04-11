package nicok.bac.yolo3d.storage.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Key value cache with a fixed capacity.
 * This implementation keeps track of cache hits and cache misses.
 * @see KeyValueCache for a more efficent implementation without tracking.
 */
public class TrackingKeyValueCache<K, V> extends LinkedHashMap<K, V> implements CacheStatistics {

    protected final long capacity;

    private long cacheMisses = 0;
    private long cacheQueries = 0;

    public TrackingKeyValueCache(final int capacity) {
        super(capacity + 1, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    public V computeIfAbsent(
            final K key,
            final Function<? super K, ? extends V> compute
    ) {
        ++cacheQueries;
        return super.computeIfAbsent(key, this.wrappedCompute(compute));
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    @Override
    public void printStatistic() {
        if (cacheQueries == 0) {
            System.out.println("No cache queries");
            return;
        }

        final var hits = cacheQueries - cacheMisses;
        final var ratio = (double) hits / (double) cacheQueries * 100.0;
        System.out.printf("Cache hits: %d / %d (%.2f%%)\n", hits, cacheQueries, ratio);
    }

    private Function<? super K, ? extends V> wrappedCompute(
            final Function<? super K, ? extends V> compute
    ) {
        ++cacheMisses;
        return compute;
    }
}
