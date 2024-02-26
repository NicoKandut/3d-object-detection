package nicok.bac.yolo3d.mesh;

import nicok.bac.yolo3d.off.OffMesh;
import nicok.bac.yolo3d.off.TriangleVertex;
import nicok.bac.yolo3d.off.VertexMesh;

import java.util.Comparator;

public final class MeshConverter {

    public static VertexMesh getTriangleMesh(final OffMesh mesh) {
        final var triangles = mesh.faces().stream()
                .flatMap(face -> face.toTriangles().stream())
                .map(index -> new TriangleVertex(
                        mesh.vertices().get(index.index1()),
                        mesh.vertices().get(index.index2()),
                        mesh.vertices().get(index.index3())
                ))
                .sorted(Comparator.comparing(TriangleVertex::minZ))
                .toList();

        return new VertexMesh(triangles);
    }

    private MeshConverter() {
    }
}
