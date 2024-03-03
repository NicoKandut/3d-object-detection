package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.mesh.MeshConverter;
import nicok.bac.yolo3d.off.OffMesh;
import nicok.bac.yolo3d.off.OffReader;
import nicok.bac.yolo3d.off.VertexMesh;
import nicok.bac.yolo3d.preprocessing.Transformation;
import nicok.bac.yolo3d.voxelization.Voxelizer;

import java.util.Comparator;
import java.util.List;

public class OffAdapter2 implements InputFile {

    private OffMesh mesh;
    private List<VertexMesh.TriangleEvent> triangleEvents;

    public OffAdapter2(final String path) throws Exception {
        try (final var reader = new OffReader(path)) {
            mesh = reader.readMesh();
//            System.out.println("indexed mesh: " + mesh.vertices().size() + " vertices, " + mesh.faces().size() + " faces");

            triangleEvents = getTriangleEvents();

//            System.out.println("Starting with " + triangleEvents.size() + " events");
        }
    }

    private List<VertexMesh.TriangleEvent> getTriangleEvents() {
        final var triangleMesh = MeshConverter.getTriangleMesh(mesh);
//            System.out.println("triangles: " + triangleMesh.triangles().size());
        return triangleMesh.getEvents().stream()
                .sorted(Comparator.comparing(VertexMesh.TriangleEvent::z))
                .toList();
    }

    @Override
    public Volume3D read(final BoundingBox target) {
        final var voxelSize = 1.0;
        return Voxelizer.voxelize(triangleEvents, voxelSize, target);
    }

    @Override
    public InputFile withPreprocessing(final Transformation preProcessing) {
        mesh = new OffMesh(
                mesh.vertices().stream()
                        .map(preProcessing::apply)
                        .toList(),
                mesh.faces(),
                preProcessing.apply(mesh.boundingBox())
        );

        triangleEvents = getTriangleEvents();

        return this;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return mesh.boundingBox();
    }
}
