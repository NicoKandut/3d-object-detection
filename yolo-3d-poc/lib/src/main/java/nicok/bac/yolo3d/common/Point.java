package nicok.bac.yolo3d.common;

public record Point(double x, double y, double z) {

    public static final Point ZERO = new Point(0, 0, 0);

    public static Point min(final Point a, final Point b) {
        return new Point(
                Double.min(a.x, b.x),
                Double.min(a.y, b.y),
                Double.min(a.z, b.z)
        );
    }

    public static Point max(final Point a, final Point b) {
        return new Point(
                Double.max(a.x, b.x),
                Double.max(a.y, b.y),
                Double.max(a.z, b.z)
        );
    }

    public static Point add(final Point a, final Point b) {
        return new Point(
                a.x + b.x,
                a.y + b.y,
                a.z + b.z
        );
    }

    public static Point sub(final Point a, final Point b) {
        return new Point(
                a.x - b.x,
                a.y - b.y,
                a.z - b.z
        );
    }

    public static Point mul(double d, Point p) {
        return new Point(
                d * p.x,
                d * p.y,
                d * p.z
        );
    }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f, %.1f)", x, y, z);
    }

    public double squaredLength() {
        return x * x + y * y + z * z;
    }

    public static double dot(final Point a, final Point b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public Point normalize() {
        final var length = Math.sqrt(squaredLength());
        return new Point(
                x / length,
                y / length,
                z / length
        );
    }

    public static Point cross(final Point v1, final Point v2) {
        return new Point(
                v1.y() * v2.z() - v1.z() * v2.y(),
                v1.z() * v2.x() - v1.x() * v2.z(),
                v1.x() * v2.y() - v1.y() * v2.x()
        );
    }

    public  static Point round(final Point point) {
        return new Point(
                Math.round(point.x()),
                Math.round(point.y()),
                Math.round(point.z())
        );
    }
}
