package nicok.bac.yolo3d.mesh;

/**
 * Represents a point in 3D space.
 */
public record Vertex(
        double x,
        double y,
        double z
) {
    public static final Vertex ORIGIN = new Vertex(0, 0, 0);
    public static final Vertex ONE = new Vertex(1, 1, 1);

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

    public static Vertex componentWiseDiv(final Vertex a, final Vertex b) {
        return new Vertex(
                a.x / b.x,
                a.y / b.y,
                a.z / b.z
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

    public static Vertex floor(final Vertex position) {
        return new Vertex(
                Math.floor(position.x),
                Math.floor(position.y),
                Math.floor(position.z)
        );
    }

    public static Vertex round(final Vertex point) {
        return new Vertex(
                Math.round(point.x()),
                Math.round(point.y()),
                Math.round(point.z())
        );
    }

    public static Vertex ceil(final Vertex point) {
        return new Vertex(
                Math.ceil(point.x()),
                Math.ceil(point.y()),
                Math.ceil(point.z())
        );
    }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f, %.1f)", x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Vertex vertex) {
            return x == vertex.x && y == vertex.y && z == vertex.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final var xBits = Double.doubleToLongBits(x);
        final var yBits = Double.doubleToLongBits(y);
        final var zBits = Double.doubleToLongBits(z);

        final var longHash = xBits ^ yBits ^ zBits;

        return (int) (longHash & 0xffffffffL) ^ (int) (longHash >> 32);
    }
}
