package nicok.bac.yolo3d.mesh;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public record TriangleVertex(
        Vertex vertex1,
        Vertex vertex2,
        Vertex vertex3
) {

    public List<Vertex> sortedVerticesZ() {
        return Stream.of(vertex1, vertex2, vertex3)
                .sorted(Comparator.comparing(Vertex::z))
                .toList();
    }

    public Vertex normal() {
        final var v1 = Vertex.sub(vertex2, vertex1);
        final var v2 = Vertex.sub(vertex2, vertex3);
        return Vertex.cross(v1, v2);
    }

    public Line zIntersect(final double z) {
        final var vertices = sortedVerticesZ();
        final var origin = vertices.get(0);
        final var mid = vertices.get(1);
        final var target = vertices.get(2);
        final var normal = normal();

        final var d1 = Vertex.sub(target, origin);
        final var d2 = Vertex.sub(mid, origin);
        final var d3 = Vertex.sub(target, mid);

        if (z > mid.z()) {
            final var z1Current = z - origin.z();
            final var z2Current = z - mid.z();
            final var z1Length = target.z() - origin.z();
            final var z2Length = target.z() - mid.z();
            assert (z1Length >= 0);
            assert (z2Length >= 0);
            final var z1Ratio = clamp(z1Current / z1Length, 0, 1);
            final var z2Ratio = clamp(z2Current / z2Length, 0, 1);
            final var currentD1 = Vertex.mul(d1, z1Ratio);
            final var currentD2 = Vertex.mul(d3, z2Ratio);
            assert (currentD1.z() >= 0);
            assert (currentD2.z() >= 0);
            final var v1 = Vertex.add(origin, currentD1);
            final var v2 = Vertex.add(mid, currentD2);
            return new Line(v1, v2, normal);
        } else {
            final var zCurrent = z - origin.z();
            final var z1Length = target.z() - origin.z();
            final var z2Length = mid.z() - origin.z();
            assert (z1Length >= 0);
            assert (z2Length >= 0);
            final var z1Ratio = clamp(zCurrent / z1Length, 0, 1);
            final var z2Ratio = clamp(zCurrent / z2Length, 0, 1);
            final var currentD1 = Vertex.mul(d1, z1Ratio);
            final var currentD2 = Vertex.mul(d2, z2Ratio);
            assert (currentD1.z() >= 0);
            assert (currentD2.z() >= 0);
            final var v1 = Vertex.add(origin, currentD1);
            final var v2 = Vertex.add(origin, currentD2);
            return new Line(v1, v2, normal);
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
