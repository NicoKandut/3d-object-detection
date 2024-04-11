package nicok.bac.yolo3d.storage.bff;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.RandomAccessMeshReader;
import nicok.bac.yolo3d.storage.cache.CacheStatistics;
import nicok.bac.yolo3d.storage.cache.TrackingKeyValueCache;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static nicok.bac.yolo3d.storage.bff.BffFormat.*;
import static nicok.bac.yolo3d.util.DirectoryUtil.requireExtension;

public class BffReaderRAF implements AutoCloseable, CacheStatistics, RandomAccessMeshReader {

    private final BffHeader header;
    private final TrackingKeyValueCache<Long, Vertex> vertexCache;
    private final TrackingKeyValueCache<Long, Face> faceCache;
    private final RandomAccessFile file;
    private final ByteBuffer vertexBytes;
    private final ByteBuffer faceBytes;

    public BffReaderRAF(final String path, final int vertexCacheSize, final int faceCacheSize) throws IOException {
        requireExtension(path, "bff");

        // caches
        this.vertexCache = new TrackingKeyValueCache<>(vertexCacheSize);
        this.faceCache = new TrackingKeyValueCache<>(faceCacheSize);

        // read header
        this.file = new RandomAccessFile(path, "r");
        this.header = readHeader();

        // buffers
        this.vertexBytes = ByteBuffer.allocate((int) vertexBytes(header.precisionBytes()));
        this.faceBytes = ByteBuffer.allocate((int) faceBytes(header.indexBytes()));
    }

    public BffReaderRAF(final String path) throws IOException {
        this(path, 1000, 100);
    }

    public BffHeader header() throws IOException {
        return this.header;
    }

    public Stream<Vertex> vertices() {
        return LongStream.range(0, header.vertexCount())
                .mapToObj(this::getVertex);
    }

    public Stream<Face> getFaces() {
        return LongStream.range(0, header.faceCount())
                .mapToObj(this::getFace);
    }

    public Vertex getVertex(final long index) {
        return vertexCache.computeIfAbsent(index, this::readVertex);
    }

    public Face getFace(final long index) {
        return faceCache.computeIfAbsent(index, this::readFace);
    }

    public BffHeader readHeader() throws IOException {
        file.seek(0);

        // validate file type marker
        final var fileType = "%c%c%c%c".formatted(
                file.readByte(),
                file.readByte(),
                file.readByte(),
                file.readByte()
        );
        if (!fileType.equals(FILE_TYPE)) {
            throw new IllegalStateException("File did not contain '" + FILE_TYPE + "' marker");
        }

        final var precisionBytes = file.readByte();
        final var indexBytes = file.readByte();

        final var vertexCount = file.readLong();
        final var faceCount = file.readLong();
        final var edgeCount = file.readLong();

        final var min = new Vertex(file.readDouble(), file.readDouble(), file.readDouble());
        final var max = new Vertex(file.readDouble(), file.readDouble(), file.readDouble());
        final var boundingBox = new BoundingBox(min, max);

        return new BffHeader(
                precisionBytes,
                indexBytes,
                vertexCount,
                faceCount,
                edgeCount,
                boundingBox
        );
    }

    public Vertex readVertex(final long index) {
        final var position = getVertexPosition(index, this.header.precisionBytes());
        try {
            file.seek(position);
            file.read(vertexBytes.array());
            return this.header.precisionBytes() == 8
                    ? new Vertex(vertexBytes.getDouble(), vertexBytes.getDouble(), vertexBytes.getDouble())
                    : new Vertex(vertexBytes.getFloat(), vertexBytes.getFloat(), vertexBytes.getFloat());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            vertexBytes.rewind();
        }
    }

    public Face readFace(final long index) {
        final var position = getFacePosition(
                index,
                this.header.precisionBytes(),
                this.header.indexBytes(),
                header.vertexCount()
        );
        try {
            file.seek(position);
            file.read(faceBytes.array());
            final var indices = this.header.indexBytes() == 4
                    ? List.of((long) faceBytes.getInt(), (long) faceBytes.getInt(), (long) faceBytes.getInt())
                    : List.of(faceBytes.getLong(), faceBytes.getLong(), faceBytes.getLong());

            return new Face(indices);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            faceBytes.rewind();
        }
    }

    @Override
    public void printStatistic() {
        System.out.println("Vertex Cache Statistics:");
        this.vertexCache.printStatistic();
        System.out.println("Face Cache Statistics:");
        this.faceCache.printStatistic();
    }

    @Override
    public void close() throws Exception {
        if (this.file != null) {
            file.close();
        }
    }
}
