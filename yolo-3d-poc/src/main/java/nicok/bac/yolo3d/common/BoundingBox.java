package nicok.bac.yolo3d.common;

public record BoundingBox(Point min, Point max) {

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
}
