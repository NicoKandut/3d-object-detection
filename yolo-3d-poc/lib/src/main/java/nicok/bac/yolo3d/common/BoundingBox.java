package nicok.bac.yolo3d.common;

import com.scs.voxlib.GridPoint3;
import nicok.bac.yolo3d.off.Vertex;

public record BoundingBox(Point min, Point max) {

    public Point size() {
        return Point.sub(max, min);
    }

    public static double getIntersectOverUnion(BoundingBox a, BoundingBox b) {
        final var intersectMin = Point.max(a.min, b.min);
        final var intersectMax = Point.min(a.max, b.max);
        final var intersectWhd = Point.max(Point.sub(intersectMax, intersectMin), Point.ZERO);
        final var intersectVolume = intersectWhd.x() * intersectWhd.y() * intersectWhd.z();

        final var aWhd = Point.sub(a.max, a.min);
        final var bWhd = Point.sub(b.max, b.min);
        final var predVolume = aWhd.x() * aWhd.y() * aWhd.z();
        final var trueVolume = bWhd.x() * bWhd.y() * bWhd.z();

        final var unionVolume = predVolume + trueVolume - intersectVolume;

        return intersectVolume / unionVolume;
    }

    public static BoundingBox addOffset(BoundingBox box, Point offset) {
        return new BoundingBox(
                Point.add(box.min, offset),
                Point.add(box.max, offset)
        );
    }

    public boolean contains(final GridPoint3 point) {
        return point.x >= min.x() && point.x < max.x() &&
                point.y >= min.y() && point.y < max.y() &&
                point.z >= min.z() && point.z < max.z();
    }

    public boolean contains(final Vertex vertex) {
        return vertex.x() >= min.x() && vertex.x() < max.x() &&
                vertex.y() >= min.y() && vertex.y() < max.y() &&
                vertex.z() >= min.z() && vertex.z() < max.z();
    }

    public Point center() {
        return new Point(
                (min.x() + max.x()) / 2f,
                (min.y() + max.y()) / 2f,
                (min.z() + max.z()) / 2f
        );
    }

    @Override
    public String toString() {
        return String.format("from %s to %s", min, max);
    }

    public static final class Builder {
        private double minX = Double.MAX_VALUE;
        private double minY = Double.MAX_VALUE;
        private double minZ = Double.MAX_VALUE;
        private double maxX = Double.MIN_VALUE;
        private double maxY = Double.MIN_VALUE;
        private double maxZ = Double.MIN_VALUE;

        public void withVertex(final Vertex vertex) {
            minX = Double.min(minX, vertex.x());
            minY = Double.min(minY, vertex.y());
            minZ = Double.min(minZ, vertex.z());
            maxX = Double.max(maxX, vertex.x());
            maxY = Double.max(maxY, vertex.y());
            maxZ = Double.max(maxZ, vertex.z());
        }

        public BoundingBox build() {
            return new BoundingBox(
                    new Point(minX, minY, minZ),
                    new Point(maxX, maxY, maxZ)
            );
        }
    }
}
