package nicok.bac.yolo3d.storage.off;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.inputfile.TriangleEventIterator;
import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.TriangleEvent;
import nicok.bac.yolo3d.mesh.TriangleVertex;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.preprocessing.Transformation;
import nicok.bac.yolo3d.storage.RandomAccessMeshReader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public record OffMesh(
        List<Vertex> vertices,
        List<Face> faces,
        BoundingBox boundingBox
) implements RandomAccessMeshReader {

    public OffMesh {
        requireNonNull(vertices);
        requireNonNull(faces);
        requireNonNull(boundingBox);
    }

    @Override
    public Vertex getVertex(final long index) {
        return this.vertices.get((int) index);
    }

    @Override
    public Face getFace(final long index) {
        return this.faces.get((int) index);
    }

    public OffMesh applyTransformation(final Transformation transformation) {
        final var boundingBox = new BoundingBox.Builder();
        final var newVertices = vertices.stream()
                .map(transformation::apply)
                .peek(boundingBox::withVertex)
                .toList();

        return new OffMesh(
                newVertices,
                faces,
                boundingBox.build()
        );
    }

    public TriangleEventIterator getTriangleEvents() {
        return new TriangleEventIterator(LongStream.range(0, faces.size())
                .boxed()
                .flatMap(index -> {
                    final var face = this.getFace(index);
                    final var triangle = new TriangleVertex(
                            this.getVertex(face.vertexIndices().get(0)),
                            this.getVertex(face.vertexIndices().get(1)),
                            this.getVertex(face.vertexIndices().get(2))
                    );
                    final var vertices = triangle.sortedVerticesZ();
                    return Stream.of(
                            // face start
                            new TriangleEvent(vertices.get(0).z(), index),
                            // face end
                            new TriangleEvent(vertices.get(2).z(), index)
                    );
                })
                .sorted(Comparator.comparing(TriangleEvent::z))
                .iterator()
        );
    }

    public static class Builder {
        private final List<Vertex> vertices;
        private final List<Face> faces;
        private final BoundingBox.Builder boundingBoxBuilder = new BoundingBox.Builder();

        public Builder() {
            this.vertices = new ArrayList<>();
            this.faces = new ArrayList<>();
        }

        public Builder(final long vertexCount, final long faceCount) {
            // initialize with correct size to avoid re-allocations
            this.vertices = new ArrayList<>((int) vertexCount);
            this.faces = new ArrayList<>((int) faceCount);
        }

        public Builder addVertex(final Vertex vertex) {
            this.vertices.add(vertex);
            this.boundingBoxBuilder.withVertex(vertex);
            return this;
        }

        public Builder addFace(final Face face) {
            this.faces.add(face);
            return this;
        }

        public OffMesh build() {
            return new OffMesh(vertices, faces, boundingBoxBuilder.build());
        }
    }
}
