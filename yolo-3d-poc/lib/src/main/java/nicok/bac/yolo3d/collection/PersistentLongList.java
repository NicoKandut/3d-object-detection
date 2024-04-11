package nicok.bac.yolo3d.collection;

import nicok.bac.yolo3d.util.SortedMultiIterator;

import java.io.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static nicok.bac.yolo3d.util.ExceptionUtil.unchecked;

public class PersistentLongList extends PersistentList<Long> implements AutoCloseable {

    public PersistentLongList(final String path, final long size) throws IOException {
        super(path, size, Long.BYTES);
    }

    public static PersistentLongList from(final String path, final LongStream values, final long size) throws IOException {
        write(path, values);
        return new PersistentLongList(path, size);
    }

    public static PersistentLongList from(final String path, final List<Long> values) throws IOException {
        return from(path, values.stream().mapToLong(Long::longValue), values.size());
    }

    public static void write(final String path, final LongStream values) throws IOException {
        try (final var stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)))) {
            values.forEach(v -> {
                try {
                    stream.writeLong(v);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static void write(final String path, final List<Long> values) throws IOException {
        write(path, values.stream().mapToLong(Long::longValue));
    }

    @Override
    public Long getItem(long index) {
        try {
            this.file.seek(index * this.itemSize);
            return this.file.readLong();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void setItem(long index, Long value) {
        try {
            this.file.seek(index * this.itemSize);
            this.file.writeLong(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Iterator<Long> iterator() {
        return this.stream().iterator();
    }

    public Stream<Long> stream() {
        try {
            final DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(this.path)));
            return LongStream.generate(unchecked(stream::readLong))
                    .boxed()
                    .limit(this.size);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static PersistentLongList mergeSorted(
            final String destination,
            final List<PersistentLongList> lists,
            final Comparator<Long> comparator
    ) throws IOException {
        final var totalSize = lists.stream()
                .mapToLong(PersistentLongList::size)
                .sum();
        final var iterators = lists.stream()
                .map(PersistentLongList::iterator)
                .toList();
        final var mergedIterator = new SortedMultiIterator<>(iterators, comparator);
        final var sortedValues = Stream.generate(mergedIterator::next)
                .limit(totalSize)
                .mapToLong(Long::longValue);

        return PersistentLongList.from(destination, sortedValues, totalSize);
    }
}
