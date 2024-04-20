package nicok.bac.yolo3d.collection;

import nicok.bac.yolo3d.common.ResultBoundingBox;
import nicok.bac.yolo3d.storage.BinaryReader;
import nicok.bac.yolo3d.storage.BinaryWriter;
import nicok.bac.yolo3d.util.DirectoryUtil;
import nicok.bac.yolo3d.util.RepositoryPaths;
import nicok.bac.yolo3d.util.SortedMultiIterator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.util.DirectoryUtil.cleanDirectory;

public class PersistentResultBoundingBoxList extends PersistentList<ResultBoundingBox> {
    protected PersistentResultBoundingBoxList(final String path, final long size) throws IOException {
        super(path, size, Integer.BYTES + Double.BYTES + 6 * Double.BYTES);
    }

    public static PersistentResultBoundingBoxList from(
            final String path,
            final Stream<ResultBoundingBox> values
    ) {
        try (final var writer = writer(path)) {
            values.forEach(writer::write);
            writer.close(); // ensure everything is written, remove later
            return writer.getList();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Writer writer(String path) throws IOException {
        final var file = Path.of(path);
        if (Files.exists(file)) {
            Files.delete(file);
        }
        return new Writer(path);
    }

    public static class Writer implements AutoCloseable {

        final String path;
        final DataOutputStream stream;
        long size = 0;

        public Writer(final String path) throws IOException {
            this.path = requireNonNull(path);
            this.stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
        }

        public void write(final ResultBoundingBox value) {
            try {
                BinaryWriter.write(stream, value);
                ++size;
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        public PersistentResultBoundingBoxList getList() {
            try {
                return new PersistentResultBoundingBoxList(path, size);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws Exception {
            this.stream.close();
        }
    }

    @Override
    protected ResultBoundingBox getItem(long index) {
        try {
            this.file.seek(this.itemSize * index);
            return BinaryReader.readResultBoundingBox(this.file);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected void setItem(long index, ResultBoundingBox value) {
        try {
            this.file.seek(this.itemSize * index);
            BinaryWriter.write(this.file, value);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Stream<ResultBoundingBox> stream() {
        try {
            final var stream = new DataInputStream(new BufferedInputStream(new FileInputStream(this.path)));
            return Stream.generate(() -> {
                try {
                    return BinaryReader.readResultBoundingBox(stream);
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).limit(size);
        } catch (final FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public PersistentResultBoundingBoxList sort(final Comparator<ResultBoundingBox> comparator) throws IOException {
        final var batchSize = 20000;
        final var nrBatches = (long) Math.ceil((double) size / (double) batchSize);
        final var filename = DirectoryUtil.getFilename(this.path);
        final var sortingDirectory = String.format("%s/%s_boxes", RepositoryPaths.SORTING_TEMP, filename);
        cleanDirectory(sortingDirectory);

        final var sortedBatches = new ArrayList<PersistentResultBoundingBoxList>();
        {
            final var iterator = this.iterator();
            for (int batchIndex = 0; batchIndex < nrBatches; batchIndex++) {
                final var batchFile = String.format("%s/batch_%d.f.idx", sortingDirectory, batchIndex);
                final var batchStart = batchIndex * batchSize;
                final var batchEnd = Math.min(size, batchStart + batchSize);
                final var sortedBatchValues = LongStream.range(batchStart, batchEnd)
                        .mapToObj((i) -> iterator.next())
                        .sorted(comparator);
                final var sortedBatch = PersistentResultBoundingBoxList.from(batchFile, sortedBatchValues);
                sortedBatches.add(sortedBatch);
            }
        }

        final var iterators = sortedBatches.stream()
                .map(PersistentList::iterator)
                .toList();
        final var mergedIterator = new SortedMultiIterator<>(iterators, comparator);
        final var sortedValues = Stream.generate(mergedIterator::next)
                .limit(size);

        return PersistentResultBoundingBoxList.from(this.path + ".sorted", sortedValues);
    }
}
