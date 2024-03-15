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

public class OffAdapter implements InputFile {

    private OffMesh mesh;
    private List<VertexMesh.TriangleEvent> triangleEvents;

    public OffAdapter(final String path) throws Exception {
        try (final var reader = new OffReader(path)) {
            mesh = reader.readMesh();
            triangleEvents = getTriangleEvents();
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
        final var boundingBox = new BoundingBox.Builder();

        mesh = new OffMesh(
                mesh.vertices().stream()
                        .map(preProcessing::apply)
                        .peek(boundingBox::withVertex)
                        .toList(),
                mesh.faces(),
                boundingBox.build()
        );

        triangleEvents = getTriangleEvents();

        return this;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return mesh.boundingBox();
    }
}
