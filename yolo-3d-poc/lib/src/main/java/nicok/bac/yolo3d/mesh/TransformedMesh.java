package nicok.bac.yolo3d.mesh;

import nicok.bac.yolo3d.preprocessing.Transformation;
import nicok.bac.yolo3d.storage.RandomAccessMeshReader;

import java.util.Objects;

public record TransformedMesh(
        RandomAccessMeshReader reader,
        Transformation transformation
) implements RandomAccessMeshReader {
    public TransformedMesh {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(transformation);
    }

    @Override
    public Vertex getVertex(long index) {
        final var vertex = reader.getVertex(index);
        return transformation.apply(vertex);
    }

    @Override
    public Face getFace(long index) {
        return reader.getFace(index);
    }
}
