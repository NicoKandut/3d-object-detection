package nicok.bac.yolo3d.bff;

import nicok.bac.yolo3d.off.Face;
import nicok.bac.yolo3d.off.Header;
import nicok.bac.yolo3d.off.Vertex;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.bff.BffFormat.*;

public class BffReader implements AutoCloseable {

    private final Header header;
    private final KeyValueCache<Long, Vertex> vertexCache;
    private final KeyValueCache<Long, Face> faceCache;
    private final FileInputStream stream;
    private final ByteBuffer vertexBytes;
    private final ByteBuffer faceBytes;
    private long vertexCacheQueries = 0;
    private long vertexCacheMisses = 0;
    private long faceCacheQueries = 0;
    private long faceCacheMisses = 0;

    public BffReader(final String path) throws IOException {
        this(path, 1000, 100);
    }

    public BffReader(
            final String path,
            final int vertexCacheSize,
            final int faceCacheSize
    ) throws IOException {
        requireNonNull(path);
        this.vertexCache = new KeyValueCache<>(vertexCacheSize);
        this.faceCache = new KeyValueCache<>(faceCacheSize);
        this.stream = new FileInputStream(path);
        this.header = readHeader();
        this.vertexBytes = ByteBuffer.allocate(VERTEX_BYTES);
        this.faceBytes = ByteBuffer.allocate(FACE_BYTES);
    }

    public Header readHeader() throws IOException {
        if (this.header != null) {
            return header;
        }

        final var headerBytes = ByteBuffer.allocate(HEADER_BYTES);
        final var read = this.stream.read(headerBytes.array(), 0, HEADER_BYTES);

        if (read < HEADER_BYTES) {
            throw new IllegalStateException("File did not contain enough bytes to read the header");
        }

        final var fileType = "%c%c%c%c".formatted(
                headerBytes.get(),
                headerBytes.get(),
                headerBytes.get(),
                headerBytes.get()
        );

        if (!fileType.equals(FILE_TYPE)) {
            throw new IllegalStateException("File did not contain '" + FILE_TYPE + "' marker");
        }

        final var intBuffer = headerBytes.asIntBuffer();

        return new Header(
                intBuffer.get(0),
                intBuffer.get(1),
                intBuffer.get(2)
        );
    }

    public Stream<Vertex> getVertices() {
        return IntStream.range(0, header.vertexCount())
                .mapToObj(this::getVertex);
    }

    public Vertex getVertex(final long index) {
        ++vertexCacheQueries;
        return vertexCache.computeIfAbsent(index, this::readVertex);
    }

    public Vertex readVertex(final long index) {
        ++vertexCacheMisses;
        final long position = HEADER_BYTES + VERTEX_BYTES * index;
        try {
            stream.getChannel().position(position);
            stream.readNBytes(vertexBytes.array(), 0, VERTEX_BYTES);

            final var doubles = vertexBytes.asDoubleBuffer();

            return new Vertex(
                    doubles.get(0),
                    doubles.get(1),
                    doubles.get(2)
            );
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            vertexBytes.rewind();
        }
    }

    public Stream<Face> getFaces() {
        return IntStream.range(0, header.faceCount())
                .mapToObj(this::getFace);
    }

    public Face getFace(final long index) {
        ++faceCacheQueries;
        return faceCache.computeIfAbsent(index, this::readFace);
    }

    public Face readFace(final long index) {
        ++faceCacheMisses;
        final long position = HEADER_BYTES + VERTEX_BYTES * (long) header.vertexCount() + FACE_BYTES * index;
        try {
            stream.getChannel().position(position);
            stream.readNBytes(faceBytes.array(), 0, FACE_BYTES);

            final var integers = faceBytes.asIntBuffer();

            return new Face(
                    List.of(
                            integers.get(0),
                            integers.get(1),
                            integers.get(2)
                    )
            );
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            faceBytes.rewind();
        }
    }

    public long vertexCacheHits() {
        return vertexCacheQueries - vertexCacheMisses;
    }

    public long vertexCacheMisses() {
        return vertexCacheMisses;
    }

    public long vertexCacheQueries() {
        return vertexCacheQueries;
    }

    public long faceCacheHits() {
        return faceCacheQueries - faceCacheMisses;
    }

    public long faceCacheMisses() {
        return faceCacheMisses;
    }

    public long faceCacheQueries() {
        return faceCacheQueries;
    }

    @Override
    public void close() throws Exception {
        if (this.stream != null) {
            stream.close();
        }
    }
}
