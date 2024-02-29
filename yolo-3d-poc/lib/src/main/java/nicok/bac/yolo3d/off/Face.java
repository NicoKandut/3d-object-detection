package nicok.bac.yolo3d.off;

import nicok.bac.yolo3d.common.Point;

import java.util.ArrayList;
import java.util.List;

public record Face(
        List<Integer> vertexIndices
) {
    public Point normal(final List<Vertex> vertices) {
        final var a = vertices.get(vertexIndices().get(0));
        final var b = vertices.get(vertexIndices().get(1));
        final var c = vertices.get(vertexIndices().get(2));

        final var v1 = new Point(
                b.x() - a.x(),
                b.y() - a.y(),
                b.z() - a.z()
        );
        final var v2 = new Point(
                b.x() - c.x(),
                b.y() - c.y(),
                b.z() - c.z()
        );

        return Point.cross(v1, v2);
    }

    public Point center(final List<Vertex> vertices) {
        var xSum = 0.0;
        var ySum = 0.0;
        var zSum = 0.0;

        for (final var index : vertexIndices) {
            final var vertex = vertices.get(index);
            xSum += vertex.x();
            ySum += vertex.y();
            zSum += vertex.z();
        }

        return new Point(
                xSum / vertexIndices.size(),
                zSum / vertexIndices.size(),
                ySum / vertexIndices.size()
        );
    }

    /**
     * Converts a face with any number of vertices to a triangle fan.
     */
    public List<TriangleIndex> toTriangles() {
        if (vertexIndices.size() < 3) {
            System.out.println("WARNING: face with less than 3 vertices detected");
        }

        final var triangles = new ArrayList<TriangleIndex>(vertexIndices.size() - 2);
        final var i0 = 0;
        for (var i1 = 1; i1 < vertexIndices.size() - 1; ++i1) {
            final var i2 = i1 + 1;
            triangles.add(new TriangleIndex(
                    vertexIndices.get(i0),
                    vertexIndices.get(i1),
                    vertexIndices.get(i2)
            ));
        }
        return triangles;
    }
}
