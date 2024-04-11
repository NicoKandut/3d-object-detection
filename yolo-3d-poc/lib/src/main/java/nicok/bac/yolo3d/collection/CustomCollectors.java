package nicok.bac.yolo3d.collection;

import nicok.bac.yolo3d.mesh.TriangleEvent;

import java.io.*;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class CustomCollectors {

    public static PersistentLongListCollector toPersistentLongList(final String path) {
        return new PersistentLongListCollector(path);
    }

    public static PersistentTriangleEventListCollector toPersistentTriangleEventList(final String path) {
        return new PersistentTriangleEventListCollector(path);
    }

    private static abstract class PersistentListCollector<T, L extends PersistentList<T>> implements Collector<T, DataOutputStream, L> {

        protected final String path;
        protected long size;

        public PersistentListCollector(final String path) {
            this.path = path;
            this.size = 0;
        }

        @Override
        public Supplier<DataOutputStream> supplier() {
            return () -> {
                try {
                    return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
                } catch (final FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            };
        }


        @Override
        public BiConsumer<DataOutputStream, T> accumulator() {
            return (stream, value) -> {
                this.append(stream, value);
                size++;
            };
        }

        @Override
        public BinaryOperator<DataOutputStream> combiner() {
            return (stream1, stream2) -> {
                throw new UnsupportedOperationException();
            };
        }

        public abstract void append(DataOutputStream stream, T value);

        @Override
        public abstract Function<DataOutputStream, L> finisher();

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }

    public static class PersistentLongListCollector
            extends PersistentListCollector<Long, PersistentLongList> {

        public PersistentLongListCollector(final String path) {
            super(path);
        }

        @Override
        public void append(DataOutputStream stream, Long value) {
            try {
                stream.writeLong(value);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Function<DataOutputStream, PersistentLongList> finisher() {
            return stream -> {
                try {
                    stream.close();
                    return new PersistentLongList(path, size);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    public static class PersistentTriangleEventListCollector
            extends PersistentListCollector<TriangleEvent, PersistentTriangleEventList> {

        public PersistentTriangleEventListCollector(final String path) {
            super(path);
        }

        @Override
        public void append(DataOutputStream stream, TriangleEvent value) {
            try {
                stream.writeDouble(value.z());
                stream.writeLong(value.face());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Function<DataOutputStream, PersistentTriangleEventList> finisher() {
            return stream -> {
                try {
                    stream.close();
                    return new PersistentTriangleEventList(path, size);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    private CustomCollectors() {
        throw new UnsupportedOperationException();
    }
}
