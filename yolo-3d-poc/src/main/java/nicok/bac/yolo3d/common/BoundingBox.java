package nicok.bac.yolo3d.common;

import com.scs.voxlib.GridPoint3;

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

    @Override
    public String toString() {
        return String.format("from %s to %s", min, max);
    }
}
