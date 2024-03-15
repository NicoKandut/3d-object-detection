package nicok.bac.yolo3d.off;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.preprocessing.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record OffMesh(
        List<Vertex> vertices,
        List<Face> faces,
        BoundingBox boundingBox
) {

    public OffMesh {
        Objects.requireNonNull(vertices);
        Objects.requireNonNull(faces);
        Objects.requireNonNull(boundingBox);
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

    public static class Builder {
        private final List<Vertex> vertices;
        private final List<Face> faces;
        private final BoundingBox.Builder boundingBoxBuilder = new BoundingBox.Builder();

        public Builder() {
            this.vertices = new ArrayList<>();
            this.faces = new ArrayList<>();
        }

        public Builder(final int vertexCount, final int faceCount) {
            // initialize with correct size to avoid re-allocations
            this.vertices = new ArrayList<>(vertexCount);
            this.faces = new ArrayList<>(faceCount);
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
