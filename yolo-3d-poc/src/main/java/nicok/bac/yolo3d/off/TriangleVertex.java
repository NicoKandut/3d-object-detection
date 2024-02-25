package nicok.bac.yolo3d.off;

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
}
