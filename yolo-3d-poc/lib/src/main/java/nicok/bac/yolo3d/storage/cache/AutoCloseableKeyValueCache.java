package nicok.bac.yolo3d.storage.cache;

import java.util.Map;

public class AutoCloseableKeyValueCache<K, V extends AutoCloseable> extends KeyValueCache<K, V> implements AutoCloseable {

    public AutoCloseableKeyValueCache(final int capacity) {
        super(capacity);
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
        try {
            final var shouldRemove = super.removeEldestEntry(eldest);
            if (shouldRemove) {
                eldest.getValue().close();
            }
            return shouldRemove;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        for (final var value : values()) {
            value.close();
        }
    }
}
