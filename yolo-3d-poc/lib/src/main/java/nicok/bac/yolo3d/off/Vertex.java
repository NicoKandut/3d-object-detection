package nicok.bac.yolo3d.off;

/**
 * Represents a point in 3D space.
 */
public record Vertex(
        double x,
        double y,
        double z
) {
    public static final Vertex ORIGIN = new Vertex(0, 0, 0);

    public static Vertex add(final Vertex a, final Vertex b) {
        return new Vertex(
                a.x + b.x,
                a.y + b.y,
                a.z + b.z
        );
    }

    public static Vertex sub(final Vertex a, final Vertex b) {
        return new Vertex(
                a.x - b.x,
                a.y - b.y,
                a.z - b.z
        );
    }

    public static Vertex mul(final Vertex p, final double d) {
        return new Vertex(
                p.x * d,
                p.y * d,
                p.z * d
        );
    }

    public static Vertex div(final Vertex p, final double d) {
        return new Vertex(
                p.x / d,
                p.y / d,
                p.z / d
        );
    }

    public static Vertex componentWiseMultiply(final Vertex a, final Vertex b) {
        return new Vertex(
                a.x * b.x,
                a.y * b.y,
                a.z * b.z
        );
    }

    public static Vertex min(final Vertex a, final Vertex b) {
        return new Vertex(
                Double.min(a.x, b.x),
                Double.min(a.y, b.y),
                Double.min(a.z, b.z)
        );
    }

    public static Vertex max(final Vertex a, final Vertex b) {
        return new Vertex(
                Double.max(a.x, b.x),
                Double.max(a.y, b.y),
                Double.max(a.z, b.z)
        );
    }

    public double squaredLength() {
        return dot(this, this);
    }

    private double length() {
        return Math.sqrt(squaredLength());
    }

    public static double dot(final Vertex a, final Vertex b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static Vertex cross(final Vertex v1, final Vertex v2) {
        return new Vertex(
                v1.y() * v2.z() - v1.z() * v2.y(),
                v1.z() * v2.x() - v1.x() * v2.z(),
                v1.x() * v2.y() - v1.y() * v2.x()
        );
    }

    public Vertex normalize() {
        return div(this, length());
    }

    public static Vertex round(final Vertex point) {
        return new Vertex(
                Math.round(point.x()),
                Math.round(point.y()),
                Math.round(point.z())
        );
    }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f, %.1f)", x, y, z);
    }
}
