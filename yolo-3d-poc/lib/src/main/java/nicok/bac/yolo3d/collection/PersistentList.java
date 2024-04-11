package nicok.bac.yolo3d.collection;

import nicok.bac.yolo3d.storage.cache.KeyValueCache;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public abstract class PersistentList<T> implements AutoCloseable {

    private final KeyValueCache<Long, T> cache;
    protected final String path;
    protected final RandomAccessFile file;
    public long size;
    protected long itemSize;

    protected PersistentList(final String path, final long size, final long itemSize) throws IOException {
        this.path = requireNonNull(path);
        this.cache = new KeyValueCache<>(1000);
        this.file = new RandomAccessFile(path, "rw");
        this.itemSize = itemSize;

        final var fileSize = Files.size(Path.of(path));
        this.size = size;

        if (fileSize % itemSize != 0) {
            throw new IOException("File size is not a multiple of item size");
        }

//        if (fileSize / itemSize != size) {
//            throw new IOException("File size does not match expected size. Expected: " + size + ", actual: " + fileSize / itemSize);
//        }
    }

    public int size() {
        return (int) this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public T get(long index) throws IOException {
        checkBounds(index);
        return this.cache.computeIfAbsent(index, this::getItem);
    }

    public void set(long index, T value) {
        checkBounds(index);
        setItem(index, value);
        this.cache.put(index, value);
    }

    private void checkBounds(long index) {
        if (index < 0 || index >= this.size) {
            throw new IndexOutOfBoundsException("Index out of bounds(0," + this.size + "): " + index);
        }
    }

    protected abstract T getItem(long index);

    protected abstract void setItem(long index, T value);

    @Override
    public void close() throws Exception {
        this.file.close();
    }

    public Iterator<T> iterator() {
        return this.stream().iterator();
    }

    public abstract Stream<T> stream();
}
