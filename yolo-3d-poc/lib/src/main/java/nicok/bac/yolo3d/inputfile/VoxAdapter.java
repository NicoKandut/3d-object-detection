package nicok.bac.yolo3d.inputfile;

import com.scs.voxlib.VoxFile;
import com.scs.voxlib.VoxReader;
import com.scs.voxlib.Voxel;
import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.preprocessing.Transformation;
import nicok.bac.yolo3d.storage.chunkstore.ChunkStore;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class VoxAdapter implements InputFile {

    private List<Voxel> voxels;
    private BoundingBox boundingBox;

    public VoxAdapter(final String path) throws IOException {
        try (final var reader = new VoxReader(new FileInputStream(path))) {
            final var voxFile = reader.read();

            voxels = getVoxels(voxFile);
            boundingBox = getBoundingBox(voxFile);
        }
    }

    @Override
    public Volume3D read(final BoundingBox target) {
        final var volume = new Volume3D(
                (int) (target.max().y() - target.min().y()),
                (int) (target.max().z() - target.min().z()),
                (int) (target.max().x() - target.min().x())
        );

        voxels.stream()
                .filter(voxel -> target.contains(
                        new Vertex(voxel.getPosition().x, voxel.getPosition().y, voxel.getPosition().z)
                ))
                .forEach(v -> {
                    final var x = v.getPosition().y - (int) target.min().y();
                    final var y = (int) target.size().z() - (v.getPosition().z % (int) target.size().z()) - 1;
                    final var z = v.getPosition().x - (int) target.min().x();
                    volume.set(x, y, z, 1f);
                });

        return volume;
    }

    @Override
    public ChunkStore createChunkStore() {
        return null;
    }

    @Override
    public InputFile transform(final Transformation transformation) {
        final var boundingBoxBuilder = new BoundingBox.Builder();
        this.voxels = this.voxels.stream()
                .map(transformation::apply)
                .peek(voxel -> boundingBoxBuilder.withVertex(
                        new Vertex(voxel.getPosition().x, voxel.getPosition().y, voxel.getPosition().z)
                ))
                .toList();
        this.boundingBox = boundingBoxBuilder.build();
        return this;
    }

    private static List<Voxel> getVoxels(final VoxFile voxFile) {
        return Arrays.stream(voxFile.getModelInstances().get(0).model.getVoxels()).toList();
    }

    private static BoundingBox getBoundingBox(final VoxFile voxFile) {
        final var size = voxFile.getModelInstances().get(0).model.getSize();
        return BoundingBox.fromOrigin(new Vertex(size.x, size.y, size.z));
    }

    @Override
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }
}
