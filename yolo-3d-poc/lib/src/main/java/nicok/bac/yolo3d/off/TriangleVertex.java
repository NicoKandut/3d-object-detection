package nicok.bac.yolo3d.off;

import nicok.bac.yolo3d.common.Point;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public record TriangleVertex(
        Vertex vertex1,
        Vertex vertex2,
        Vertex vertex3
) {
    public double minZ() {
        return Double.min(vertex1.z(), Double.min(vertex2.z(), vertex3.z()));
    }

    public List<Vertex> sortedVerticesZ() {
        return Stream.of(vertex1, vertex2, vertex3)
                .sorted(Comparator.comparing(Vertex::z))
                .toList();
    }

    public Vertex normal() {
        final var v1 = new Vertex(
                vertex2.x() - vertex1.x(),
                vertex2.y() - vertex1.y(),
                vertex2.z() - vertex1.z()
        );
        final var v2 = new Vertex(
                vertex2.x() - vertex3.x(),
                vertex2.y() - vertex3.y(),
                vertex2.z() - vertex3.z()
        );

        return Vertex.cross(v1, v2);
    }
}
