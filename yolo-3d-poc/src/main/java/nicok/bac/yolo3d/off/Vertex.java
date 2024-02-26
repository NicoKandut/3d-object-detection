package nicok.bac.yolo3d.off;

public record Vertex(
        double x,
        double y,
        double z
) {
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

    public static Vertex mul(double d, Vertex p) {
        return new Vertex(
                d * p.x,
                d * p.y,
                d * p.z
        );
    }

    public static Vertex componentWiseMultiply(Vertex a, Vertex b) {
        return new Vertex(
                a.x * b.x,
                a.y * b.y,
                a.z * b.z
        );
    }
}
