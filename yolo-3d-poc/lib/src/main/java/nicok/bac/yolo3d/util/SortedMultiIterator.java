package nicok.bac.yolo3d.util;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class SortedMultiIterator<T> implements Iterator<T> {

    private final List<Iterator<T>> iterators;
    private final Map<Integer, T> nextValues;
    private final Comparator<T> comparator;

    public SortedMultiIterator(
            final List<Iterator<T>> iterators,
            final Comparator<T> comparator
    ) {
        this.comparator = requireNonNull(comparator);
        this.iterators = requireNonNull(iterators);
        this.nextValues = new HashMap<>(iterators.size());

        for (int i = 0; i < iterators.size(); i++) {
            final var iterator = iterators.get(i);
            if (iterator.hasNext()) {
                nextValues.put(i, iterator.next());
            }
        }
    }

    @Override
    public boolean hasNext() {
        return this.iterators.stream().anyMatch(Iterator::hasNext);
    }

    @Override
    public T next() {
        final var minEntry = nextValues.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue(this.comparator));

        if (minEntry.isEmpty()) {
            throw new IllegalStateException("No more integers to read");
        }

        final var minIndex = minEntry.get().getKey();
        final var minValue = minEntry.get().getValue();

        final var iterator = iterators.get(minIndex);
        if (iterator.hasNext()) {
            nextValues.put(minIndex, iterator.next());
        } else {
            nextValues.remove(minIndex);
        }

        return minValue;
    }
}
