package nicok.bac.yolo3d.off;

import nicok.bac.yolo3d.common.Point;

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

    public static Vertex cross(final Vertex v1, final Vertex v2) {
        return new Vertex(
                v1.y() * v2.z() - v1.z() * v2.y(),
                v1.z() * v2.x() - v1.x() * v2.z(),
                v1.x() * v2.y() - v1.y() * v2.x()
        );
    }
}
