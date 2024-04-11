package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.mesh.TransformedMesh;
import nicok.bac.yolo3d.mesh.TriangleEvent;
import nicok.bac.yolo3d.mesh.TriangleVertex;
import nicok.bac.yolo3d.preprocessing.IdentityTransformation;
import nicok.bac.yolo3d.preprocessing.Transformation;
import nicok.bac.yolo3d.storage.bff.BffHeader;
import nicok.bac.yolo3d.storage.bff.BffReaderRAF;
import nicok.bac.yolo3d.storage.chunkstore.ChunkStore;
import nicok.bac.yolo3d.util.DirectoryUtil;
import nicok.bac.yolo3d.util.RepositoryPaths;
import nicok.bac.yolo3d.voxelization.Voxelizer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.util.DirectoryUtil.requireExtension;

public class BffAdapter implements InputFile, AutoCloseable {

    private final BffReaderRAF reader;
    private final BffHeader header;
    private final String name;
    private TransformedMesh transformed;

    public BffAdapter(final String path) throws Exception {
        requireExtension(path, ".bff");

        this.name = DirectoryUtil.getFilename(path);
        this.reader = new BffReaderRAF(path);
        this.header = this.reader.header();
        this.transformed = new TransformedMesh(reader, new IdentityTransformation());
    }

    private TriangleEventIterator getTriangleEvents() {
        final var events = new TriangleEventIterator(LongStream.range(0, header.faceCount())
                .boxed()
                .flatMap(index -> {
                    final var face = reader.readFace(index);
                    final var triangle = new TriangleVertex(
                            transformed.getVertex(face.vertexIndices().get(0)),
                            transformed.getVertex(face.vertexIndices().get(1)),
                            transformed.getVertex(face.vertexIndices().get(2))
                    );
                    final var vertices = triangle.sortedVerticesZ();
                    return Stream.of(
                            new TriangleEvent(vertices.get(0).z(), index),
                            new TriangleEvent(vertices.get(2).z(), index)
                    );
                })
                .sorted(Comparator.comparing(TriangleEvent::z))
                .iterator()
        );

        final var sortingPath = RepositoryPaths.SORTING_TEMP + "/" + this.name + "/events";
        try {
            DirectoryUtil.cleanDirectory(sortingPath);
            return TriangleEventIterator.sorted(sortingPath, events, header.faceCount() * 2);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Volume3D read(final BoundingBox target) {
        final var voxelSize = 1.0;
        return Voxelizer.voxelize(getTriangleEvents(), transformed, voxelSize, target);
    }

    @Override
    public ChunkStore createChunkStore() throws IOException {
        final var voxelSize = 1.0;
        return Voxelizer.saveChunkStore(getTriangleEvents(), transformed, voxelSize, this.getBoundingBox(), this.name);
    }

    @Override
    public InputFile transform(final Transformation transformation) {
        requireNonNull(transformation);
        this.transformed = new TransformedMesh(reader, transformation);
        return this;
    }

    @Override
    public BoundingBox getBoundingBox() {
        if (this.transformed.transformation() instanceof IdentityTransformation) {
            return this.header.boundingBox();
        }

        final var transformedBoundingBox = new BoundingBox.Builder();

        LongStream.range(0, this.header.vertexCount())
                .mapToObj(this.transformed::getVertex)
                .forEach(transformedBoundingBox::withVertex);

        return transformedBoundingBox.build();
    }

    @Override
    public void close() throws Exception {
        if (this.reader != null) {
            this.reader.close();
        }
    }
}
