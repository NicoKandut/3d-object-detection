package nicok.bac.yolo3d.storage.bff;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.BinaryReader;
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

    public BffReaderRAF(final String path, final int vertexCacheSize, final int faceCacheSize) throws IOException {
        requireExtension(path, "bff");

        // caches
        this.vertexCache = new TrackingKeyValueCache<>(vertexCacheSize);
        this.faceCache = new TrackingKeyValueCache<>(faceCacheSize);

        // read header
        this.file = new RandomAccessFile(path, "r");
        this.header = readHeader();
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
        return BinaryReader.readBffHeader(file);
    }

    public Vertex readVertex(final long index) {
        final var position = getVertexPosition(index, this.header.precisionBytes());
        try {
            file.seek(position);
            return BinaryReader.readVertex(file, this.header.precisionBytes());
        } catch (final IOException e) {
            throw new RuntimeException(e);
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
            return BinaryReader.readFace(file, this.header.indexBytes());
        } catch (final IOException e) {
            throw new RuntimeException(e);
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
