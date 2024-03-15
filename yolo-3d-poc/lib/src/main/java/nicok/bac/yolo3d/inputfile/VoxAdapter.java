package nicok.bac.yolo3d.inputfile;

import com.scs.voxlib.VoxFile;
import com.scs.voxlib.VoxReader;
import com.scs.voxlib.Voxel;
import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.off.Vertex;
import nicok.bac.yolo3d.common.Volume3D;
import nicok.bac.yolo3d.preprocessing.Transformation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class VoxAdapter implements InputFile {

    private List<Voxel> voxels;
    private final int[] palette;
    private BoundingBox boundingBox;


    public VoxAdapter(final String path) throws IOException {
        try (final var reader = new VoxReader(new FileInputStream(path))) {
            final var voxFile = reader.read();

            palette = voxFile.getPalette();
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
                .filter(voxel -> target.contains(voxel.getPosition()))
                .forEach(v -> {
                    final var material = palette[v.getColourIndex()];
                    final var r = (float) (material & 0xFF) / 255f;
                    final var g = (float) (material >> 8 & 0xFF) / 255f;
                    final var b = (float) (material >> 16 & 0xFF) / 255f;
                    final var x = v.getPosition().y - (int) target.min().y();
                    final var y = (int) target.size().z() - (v.getPosition().z % (int) target.size().z()) - 1;
                    final var z = v.getPosition().x - (int) target.min().x();
                    volume.set(x, y, z, true);
                });

        return volume;
    }

    @Override
    public InputFile withPreprocessing(final Transformation preProcessing) {
        final var boundingBoxBuilder = new BoundingBox.Builder();
        this.voxels = this.voxels.stream()
                .map(preProcessing::apply)
                .peek(boundingBoxBuilder::withVoxel)
                .toList();
        this.boundingBox = boundingBoxBuilder.build();
        return this;
    }

    private static List<Voxel> getVoxels(final VoxFile voxFile) {
        assert (voxFile.getModelInstances().size() == 1);
        return Arrays.stream(voxFile.getModelInstances().get(0).model.getVoxels()).toList();
    }

    private static BoundingBox getBoundingBox(final VoxFile voxFile) {
        final var size = voxFile.getModelInstances().get(0).model.getSize();
        return new BoundingBox(
                new Vertex(0, 0, 0),
                new Vertex(size.x, size.y, size.z)
        );
    }

    @Override
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }
}
