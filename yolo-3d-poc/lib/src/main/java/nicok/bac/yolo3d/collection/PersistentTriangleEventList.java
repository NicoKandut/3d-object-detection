package nicok.bac.yolo3d.collection;

import nicok.bac.yolo3d.mesh.TriangleEvent;

import java.io.*;
import java.util.stream.Stream;

public class PersistentTriangleEventList extends PersistentList<TriangleEvent> {
    protected PersistentTriangleEventList(String path, long size) throws IOException {
        super(path, size, Double.BYTES + Long.BYTES);
    }

    public static PersistentTriangleEventList write(
            final String path,
            final Stream<TriangleEvent> values
    ) throws IOException {
        try (final var stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)))) {
            final var size = values.peek(value -> {
                        try {
                            stream.writeDouble(value.z());
                            stream.writeLong(value.face());
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .count();

            return new PersistentTriangleEventList(path, size);
        }


    }

    @Override
    protected TriangleEvent getItem(long index) {
        try {
            this.file.seek(this.itemSize * index);
            return null;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected void setItem(long index, TriangleEvent value) {
        try {
            this.file.seek(this.itemSize * index);
            this.file.writeDouble(value.z());
            this.file.writeLong(value.face());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Stream<TriangleEvent> stream() {
        try {
            final DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(this.path)));
            return Stream.generate(() -> {
                        try {
                            final double z = stream.readDouble();
                            final var face = stream.readLong();
                            return new TriangleEvent(z, face);
                        } catch (final IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .limit(this.size);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }
}
