package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.preprocessing.Transformation;
import nicok.bac.yolo3d.storage.chunkstore.ChunkStore;
import nicok.bac.yolo3d.storage.off.OffMesh;
import nicok.bac.yolo3d.storage.off.OffReader;
import nicok.bac.yolo3d.util.DirectoryUtil;
import nicok.bac.yolo3d.voxelization.Voxelizer;

import java.io.IOException;

public class OffAdapter implements InputFile {

    private final String name;
    private OffMesh mesh;
    private TriangleEventIterator triangleEvents;

    public OffAdapter(final String path) throws Exception {
        try (final var reader = new OffReader(path)) {
            mesh = reader.readMesh();
            triangleEvents = mesh.getTriangleEvents();
        }
        this.name = DirectoryUtil.getFilename(path);
    }

    @Override
    public Volume3D read(final BoundingBox target) {
        final var voxelSize = 1.0;
        return Voxelizer.voxelize(triangleEvents, mesh, voxelSize, target, false);
    }

    @Override
    public ChunkStore createChunkStore() throws IOException {
        final var voxelSize = 1.0;
        return Voxelizer.saveChunkStore(triangleEvents, mesh, voxelSize, this.getBoundingBox(), this.name, false);
    }

    @Override
    public InputFile transform(final Transformation transformation) {
        final var boundingBox = new BoundingBox.Builder();

        mesh = new OffMesh(
                mesh.vertices().stream()
                        .map(transformation::apply)
                        .peek(boundingBox::withVertex)
                        .toList(),
                mesh.faces(),
                boundingBox.build()
        );

        triangleEvents = mesh.getTriangleEvents();

        return this;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return mesh.boundingBox();
    }
}
