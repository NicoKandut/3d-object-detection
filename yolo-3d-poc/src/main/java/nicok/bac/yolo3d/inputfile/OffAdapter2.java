package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.mesh.MeshConverter;
import nicok.bac.yolo3d.off.OffMesh;
import nicok.bac.yolo3d.off.OffReader;
import nicok.bac.yolo3d.off.VertexMesh;
import nicok.bac.yolo3d.preprocessing.PreProcessing;
import nicok.bac.yolo3d.voxelization.Voxelizer;

import java.util.Comparator;
import java.util.List;

public class OffAdapter2 implements InputFile {

    private final OffMesh mesh;
    private final List<VertexMesh.TriangleEvent> triangleEvents;

    public OffAdapter2(
            final String path,
            final PreProcessing preProcessing
    ) throws Exception {
        try (final var reader = new OffReader(path)) {
            mesh = reader.readMesh(preProcessing);
            System.out.println("indexed mesh: " + mesh.vertices().size() + " vertices, " + mesh.faces().size() + " faces");

            final var triangleMesh = MeshConverter.getTriangleMesh(mesh);
            System.out.println("triangles: " + triangleMesh.triangles().size());

            triangleEvents = triangleMesh.getEvents().stream()
                    .sorted(Comparator.comparing(VertexMesh.TriangleEvent::z))
                    .toList();

            System.out.println("Starting with " + triangleEvents.size() + " events");
        }
    }


    @Override
    public Volume3D read(final BoundingBox target) {
        System.out.printf("Reading %s\n", target);
        final var voxelSize = 0.05;
        return Voxelizer.voxelize(triangleEvents, voxelSize, target);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return mesh.boundingBox();
    }
}
