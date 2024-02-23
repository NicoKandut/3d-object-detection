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

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
