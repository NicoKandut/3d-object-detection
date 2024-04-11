package nicok.bac.yolo3d.mesh;

import nicok.bac.yolo3d.voxelization.Voxelizer;

public record Line(
        Vertex v1,
        Vertex v2,
        Vertex normal // lines usually don't have normals, but here they inherit the normal from the triangle
) {
    public Voxelizer.PointX zIntersect(final double currentY) {
        final var smaller = v1.y() < v2.y() ? v1 : v2;
        final var bigger = v1.y() < v2.y() ? v2 : v1;
        final var distance = Vertex.sub(bigger, smaller);
        final var yCurrent = currentY - smaller.y();
        final var yRatio = yCurrent / distance.y();
        final var result = Vertex.add(smaller, Vertex.mul(distance, yRatio));

        return new Voxelizer.PointX(result.x(), normal.x());
    }
}