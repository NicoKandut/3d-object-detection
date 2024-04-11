package nicok.bac.yolo3d.mesh;

import nicok.bac.yolo3d.storage.off.OffMesh;
import nicok.bac.yolo3d.util.Triangulation;

import static java.util.Comparator.comparing;

public final class MeshConverter {

    public static VertexMesh getTriangleMesh(final OffMesh mesh) {
        final var triangles = mesh.faces().stream()
                .flatMap(Triangulation::shell)
                .map(index -> new TriangleVertex(
                        mesh.getVertex(index.index1()),
                        mesh.getVertex(index.index2()),
                        mesh.getVertex(index.index3())
                ))
                .sorted(comparing(TriangleVertex::minZ))
                .toList();

        return new VertexMesh(triangles);
    }

    private MeshConverter() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
}
