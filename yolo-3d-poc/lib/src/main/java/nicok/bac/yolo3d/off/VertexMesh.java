package nicok.bac.yolo3d.off;

import nicok.bac.yolo3d.voxelization.Voxelizer;

import java.util.List;
import java.util.stream.Stream;

public record VertexMesh(List<TriangleVertex> triangles) {

    public static record Line2D(Vertex v1, Vertex v2, Vertex normal) {

        public Voxelizer.PointX atY(final double currentY) {
            final var smallerY = v1.y() < v2.y() ? v1 : v2;
            final var biggerY = v1.y() < v2.y() ? v2 : v1;
            final var total = biggerY.y() - smallerY.y();
            final var current = currentY - smallerY.y();
            final var ratio = current / total;
            final var diff = Vertex.sub(biggerY, smallerY);
            final var result = Vertex.add(smallerY, Vertex.mul(ratio, diff));

            return new Voxelizer.PointX(result.x(), normal.x());
        }
    }

    public static class Line {
        public Vertex origin;
        public Vertex d1;
        public Vertex d2;
        public Vertex mid;
        public Vertex d3;
        public Vertex target;
        private final Vertex normal;

        public boolean isUpperHalf = false;

        public Line(final Vertex origin, final Vertex mid, final Vertex target, final Vertex normal) {
            this.origin = origin;
            this.mid = mid;
            this.target = target;
            this.normal = normal;
            this.d1 = Vertex.sub(target, origin);
            this.d2 = Vertex.sub(mid, origin);
            this.d3 = Vertex.sub(target, mid);
        }

        private static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }

        public Line2D atZ(final double z) {
            assert (z >= origin.z() && z <= target.z());

            if (z > mid.z()) {
                assert (isUpperHalf);
                final var z1Current = z - origin.z();
                final var z2Current = z - mid.z();
                final var z1Length = target.z() - origin.z();
                final var z2Length = target.z() - mid.z();
                assert (z1Length >= 0);
                assert (z2Length >= 0);
                final var z1Ratio = clamp(z1Current / z1Length, 0, 1);
                final var z2Ratio = clamp(z2Current / z2Length, 0, 1);
                final var currentD1 = Vertex.mul(z1Ratio, d1);
                final var currentD2 = Vertex.mul(z2Ratio, d3);
                assert (currentD1.z() >= 0);
                assert (currentD2.z() >= 0);
                final var v1 = Vertex.add(origin, currentD1);
                final var v2 = Vertex.add(mid, currentD2);
//                if (v1.z() != z)
//                    throw new IllegalStateException("Expected " + z + ", got " + v1.z());
//                if (v2.z() != z)
//                    throw new IllegalStateException("Expected " + z + ", got " + v2.z());
                return new Line2D(
                        v1,
                        v2,
                        normal
                );
            } else {
                assert (!isUpperHalf);
                final var zCurrent = z - origin.z();
                final var z1Length = target.z() - origin.z();
                final var z2Length = mid.z() - origin.z();
                assert (z1Length >= 0);
                assert (z2Length >= 0);
                final var z1Ratio = clamp(zCurrent / z1Length, 0, 1);
                final var z2Ratio = clamp(zCurrent / z2Length, 0, 1);
                final var currentD1 = Vertex.mul(z1Ratio, d1);
                final var currentD2 = Vertex.mul(z2Ratio, d2);
                assert (currentD1.z() >= 0);
                assert (currentD2.z() >= 0);
                final var v1 = Vertex.add(origin, currentD1);
                final var v2 = Vertex.add(origin, currentD2);
//                if (v1.z() != z)
//                    throw new IllegalStateException("Expected " + z + ", got " + v1.z());
//                if (v2.z() != z)
//                    throw new IllegalStateException("Expected " + z + ", got " + v2.z());
                return new Line2D(
                        v1,
                        v2,
                        normal
                );
            }
        }
    }

    public static enum EventType {
        BEGIN,
        MID,
        END,
    }

    public static record TriangleEvent(double z, EventType type, Line line) {
    }


    public List<TriangleEvent> getEvents() {
        return triangles.stream().flatMap(triangle -> {
                    var vertices = triangle.sortedVerticesZ();
                    var line = new Line(
                            vertices.get(0),
                            vertices.get(1),
                            vertices.get(2),
                            triangle.normal()
                    );
                    return Stream.of(
                            new TriangleEvent(vertices.get(0).z(), EventType.BEGIN, line),
                            new TriangleEvent(vertices.get(1).z(), EventType.MID, line),
                            new TriangleEvent(vertices.get(2).z(), EventType.END, line)
                    );
                })
                .toList();

    }
}
