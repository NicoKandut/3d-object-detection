package nicok.bac.yolo3d.util;

import nicok.bac.yolo3d.mesh.Face;
import nicok.bac.yolo3d.mesh.TriangleIndex;

import java.util.Arrays;
import java.util.stream.Stream;

public final class Triangulation {

    public static Stream<TriangleIndex> shell(final Face face) {
        final var indices = face.vertexIndices();
        final var vertexCount = indices.size();

        if (vertexCount < 3) {
            throw new IllegalArgumentException("A face must have at least 3 vertices");
        }

        final var triangles = new TriangleIndex[vertexCount - 2];
        final var i0 = 0;
        for (int i = 0; i < vertexCount - 2; i++) {
            triangles[i] = new TriangleIndex(
                    indices.get(i0),
                    indices.get(i + 1),
                    indices.get(i + 2)
            );
        }

        return Arrays.stream(triangles);
    }

    private Triangulation() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
}
