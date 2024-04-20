package nicok.bac.yolo3d.storage.chunkstore;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.BinaryWriter;
import nicok.bac.yolo3d.storage.FloatRead3D;
import nicok.bac.yolo3d.storage.FloatWrite3D;
import nicok.bac.yolo3d.storage.cache.CacheStatistics;
import nicok.bac.yolo3d.storage.cache.TrackingAutoCloseableKeyValueCache;
import nicok.bac.yolo3d.util.DirectoryUtil;
import nicok.bac.yolo3d.util.RepositoryPaths;
import org.apache.commons.math3.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;


public class ChunkStore implements CacheStatistics, FloatWrite3D, FloatRead3D {

    private static final long CHUNK_SIZE = 112;

    private final TrackingAutoCloseableKeyValueCache<Vertex, RandomAccessFile> chunkFileCache;
    private final BoundingBox.Builder boundingBox;
    private final Path path;

    public ChunkStore(final String name) {
        requireNonNull(name);
        this.chunkFileCache = new TrackingAutoCloseableKeyValueCache<>(20);
        this.path = getPath(name);
        this.boundingBox = new BoundingBox.Builder();
    }

    private Path getPath(final String name) {
        final var path = Path.of(name);
        return path.isAbsolute()
                ? path
                : Path.of(RepositoryPaths.VOLUME_DATA_TEMP, name + ".chunks");
    }

    public ChunkStore(final String path, final BoundingBox boundingBox) {
        requireNonNull(path);
        requireNonNull(boundingBox);
        this.chunkFileCache = new TrackingAutoCloseableKeyValueCache<>(20);
        this.path = getPath(path);
        this.boundingBox = new BoundingBox.Builder();
        this.boundingBox.withVertices(boundingBox.min(), boundingBox.max());
    }

    private void readBoundingBox() throws IOException {
        final var boundsFile = Path.of(this.path.toString(), "bounds.bin");
        try (final var raf = new DataInputStream(new BufferedInputStream(new FileInputStream(boundsFile.toString())))) {
            final var min = new Vertex(raf.readDouble(), raf.readDouble(), raf.readDouble());
            final var max = new Vertex(raf.readDouble(), raf.readDouble(), raf.readDouble());
            this.boundingBox.withVertices(min, max);
        }
    }

    public static ChunkStore reader(final String path) throws IOException {
        final var store = new ChunkStore(path);
        store.readBoundingBox();
        return store;
    }

    public static ChunkStore writer(final String path, final BoundingBox boundingBox) throws IOException {
        final var store = new ChunkStore(path, boundingBox);
        store.reset();
        return store;
    }

    private void reset() throws IOException {
        if (Files.exists(this.path)) {
            try (final var files = Files.walk(this.path, 1)) {
                final var deleted = files
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".bin"))
                        .map(Path::toFile)
                        .map(File::delete)
                        .allMatch(Predicate.isEqual(Boolean.TRUE));
                if (!deleted) {
                    throw new IOException("Could not delete all files in " + this.path);
                }
            }
            Files.deleteIfExists(this.path);
        } else {
            Files.createDirectories(this.path);
        }
    }

    @Override
    public void set(final int x, final int y, final int z, final float value) {

        // normalize to avoid floating point errors
        final var position = new Vertex(x, y, z);
        this.boundingBox.withVertex(position);
        final var normalizedPosition = Vertex.floor(position);

        // get chunk
        final var chunkPosition = computeChunkPosition(normalizedPosition);
        final var chunkRAF = this.chunkFileCache.computeIfAbsent(chunkPosition, this::computeChunkFile);

        // get position in chunk
        final var relativePosition = Vertex.sub(normalizedPosition, Vertex.mul(chunkPosition, CHUNK_SIZE));
        final var index = getIndex(relativePosition);

        try {
            chunkRAF.seek(index);
            chunkRAF.writeByte((byte) value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.boundingBox.withVertex(normalizedPosition);
        this.boundingBox.withVertex(Vertex.add(normalizedPosition, Vertex.ONE));
    }

    public float query(final Vertex position) {
        if (!this.boundingBox.contains(position)) {
            return 0.0f;
        }

        // normalize to avoid floating point errors
        final var normalizedPosition = Vertex.floor(position);

        // if cached
        return this.computeValueAtPosition(normalizedPosition);
    }

    public Stream<Pair<Vertex, Float>> queryAll() throws IOException {
        try (final var files = Files.walk(path, 1)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().contains("bounds"))
                    .map(Path::toString)
                    .flatMap(chunkPath -> {
                        final var name = DirectoryUtil.getFilename(chunkPath);
                        final var xyz = name.split("_");
                        final var x = Long.parseLong(xyz[0]);
                        final var y = Long.parseLong(xyz[1]);
                        final var z = Long.parseLong(xyz[2]);
                        final var chunkPosition = Vertex.mul(new Vertex(x, y, z), CHUNK_SIZE);

                        try {
                            final var chunkStream = new DataInputStream(new BufferedInputStream(new FileInputStream(chunkPath)));
                            return LongStream.range(0, CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE)
                                    .mapToObj(i -> {
                                        try {
                                            final var value = chunkStream.readByte();
                                            final var position = Vertex.add(chunkPosition, new Vertex(
                                                    (double) (i % CHUNK_SIZE),
                                                    (double) ((i / CHUNK_SIZE) % CHUNK_SIZE),
                                                    (double) (i / (CHUNK_SIZE * CHUNK_SIZE))
                                            ));
                                            return new Pair<>(position, (float) value);
                                        } catch (final IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                        } catch (final FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private float computeValueAtPosition(final Vertex position) {

        // normalized
        final var normalizedPosition = Vertex.floor(position);

        // get chunk
        final var chunkPosition = computeChunkPosition(normalizedPosition);

        final var chunkRAF = this.chunkFileCache.computeIfAbsent(chunkPosition, this::computeChunkFile);

        // position in chunk
        final var relativePosition = Vertex.sub(normalizedPosition, Vertex.mul(chunkPosition, CHUNK_SIZE));
        final var index = getIndex(relativePosition);

        try {
            chunkRAF.seek(index);
            return chunkRAF.readByte();
        } catch (final IOException e) {
            // reading outside => defaults to 0
            return 0;
        }
    }

    private static Vertex computeChunkPosition(final Vertex normalizedPosition) {
        return Vertex.floor(Vertex.div(normalizedPosition, CHUNK_SIZE));
    }

    private RandomAccessFile computeChunkFile(final Vertex chunkPosition) {
        final var chunkPath = getChunkPath(chunkPosition);
        try {
            final var path = Path.of(chunkPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            final var raf = new RandomAccessFile(chunkPath, "rw");
            raf.setLength(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE);
            return raf;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getChunkPath(Vertex chunkPosition) {
        return this.path + "/" + (int) chunkPosition.x() + "_" + (int) chunkPosition.y() + "_" + (int) chunkPosition.z() + ".bin";
    }

    private long getIndex(final Vertex relativePosition) {
        return (long) relativePosition.x()
                + (long) relativePosition.y() * CHUNK_SIZE
                + (long) relativePosition.z() * CHUNK_SIZE * CHUNK_SIZE;
    }

    public BoundingBox boundingBox() {
        return this.boundingBox.build();
    }

    @Override
    public void printStatistic() {
        System.out.println("Chunk Cache:");
        this.chunkFileCache.printStatistic();
    }

    public void writeHeaderFile() throws IOException {
        final var headerFile = Path.of(this.path.toString(), "bounds.bin").toFile();

        Files.createDirectories(headerFile.toPath().getParent());
        headerFile.createNewFile();

        try (final var stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(headerFile)))) {
            final var boundingBox = this.boundingBox();
            BinaryWriter.write(stream, boundingBox);
        }
    }

    @Override
    public float get(int x, int y, int z) {
        return query(new Vertex(x, y, z));
    }
}
