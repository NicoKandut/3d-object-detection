package nicok.bac.yolo3d.collection;

import nicok.bac.yolo3d.mesh.TriangleEvent;
import nicok.bac.yolo3d.storage.BinaryReader;
import nicok.bac.yolo3d.storage.BinaryWriter;

import java.io.*;
import java.util.stream.Stream;

import static nicok.bac.yolo3d.util.ExceptionUtil.unchecked;

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
                            BinaryWriter.write(stream, value);
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
            file.seek(itemSize * index);
            return BinaryReader.readTriangleEvent(file);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected void setItem(long index, TriangleEvent value) {
        try {
            file.seek(itemSize * index);
            BinaryWriter.write(file, value);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Stream<TriangleEvent> stream() {
        try {
            final DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(this.path)));
            return Stream.generate(unchecked(() -> BinaryReader.readTriangleEvent(stream))).limit(this.size);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }
}
