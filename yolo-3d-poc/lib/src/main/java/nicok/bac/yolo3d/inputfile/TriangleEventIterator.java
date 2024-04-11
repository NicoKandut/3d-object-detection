package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.collection.CustomCollectors;
import nicok.bac.yolo3d.collection.PersistentList;
import nicok.bac.yolo3d.collection.PersistentTriangleEventList;
import nicok.bac.yolo3d.mesh.TriangleEvent;
import nicok.bac.yolo3d.util.SortedMultiIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.LongStream;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

public class TriangleEventIterator implements Iterator<TriangleEvent> {

    private final Iterator<TriangleEvent> iterator;
    private TriangleEvent storedNext = null;

    public TriangleEventIterator(final Iterator<TriangleEvent> iterator) {
        this.iterator = requireNonNull(iterator);
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    public TriangleEvent next() {
        final var next = this.storedNext != null
                // If next has already been read, return it
                ? this.storedNext
                // Otherwise read it from the iterator
                : this.iterator.next();
        this.storedNext = null;
        return next;
    }

    public List<TriangleEvent> takeWhile(final Predicate<TriangleEvent> predicate) {
        final var eventsToTake = new ArrayList<TriangleEvent>();

        // read events until the predicate is false
        var current = this.next();
        while (current != null && predicate.test(current)) {
            eventsToTake.add(current);
            current = this.next();
        }

        // afterward store the next event to not lose it
        if (current != null) {
            this.storedNext = current;
        }

        return eventsToTake;
    }

    public static TriangleEventIterator sorted(
            final String path,
            final TriangleEventIterator source,
            final long size
    ) throws IOException {

        // decide number and size of batches
        final var maxBatchSize = 1000;
        final var numberOfBatches = (long) Math.ceil((double) size / (double) maxBatchSize);
        final var sortedBatches = new ArrayList<PersistentTriangleEventList>();

        // sort each batch and store it
        for (int batchIndex = 0; batchIndex < numberOfBatches; batchIndex++) {
            final var batchStart = batchIndex * maxBatchSize;
            final var batchEnd = Math.min(size, batchStart + maxBatchSize);
            final var batchSize = batchEnd - batchStart;
            final var batchPath = String.format("%s/batch_%d.f.idx", path, batchIndex);
            final var sortedBatch = LongStream.range(0, batchSize)
                    .mapToObj(i -> source.next())
                    .sorted(comparing(TriangleEvent::z))
                    .collect(CustomCollectors.toPersistentTriangleEventList(batchPath));

            sortedBatches.add(sortedBatch);
        }

        // merge the batches into a single sorted iterator
        final var iterators = sortedBatches.stream()
                .map(PersistentList::iterator)
                .toList();
        final var mergedIterator = new SortedMultiIterator<>(iterators, comparing(TriangleEvent::z));

        return new TriangleEventIterator(mergedIterator);
    }
}
