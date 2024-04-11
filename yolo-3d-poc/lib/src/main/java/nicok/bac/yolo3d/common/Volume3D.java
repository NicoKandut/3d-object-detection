package nicok.bac.yolo3d.common;

import com.scs.voxlib.Voxel;
import nicok.bac.yolo3d.boundingbox.BoundingBox;
import nicok.bac.yolo3d.boundingbox.HasBoundingBox;
import nicok.bac.yolo3d.mesh.Vertex;
import nicok.bac.yolo3d.storage.FloatRead3D;
import nicok.bac.yolo3d.storage.FloatWrite3D;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.impl.dense.FloatDenseNdArray;
import org.tensorflow.types.TFloat32;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.boundingbox.BoundingBox.fromOrigin;

public class Volume3D implements HasBoundingBox, FloatWrite3D, FloatRead3D {

    private final float[] data;
    private final BoundingBox boundingBox;
    private final int width;
    private final int height;
    private final int depth;

    public Volume3D(final float[] data, final int width, final int height, final int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.data = requireNonNull(data);
        this.boundingBox = fromOrigin(new Vertex(width, height, depth));
    }

    public Volume3D(final int width, final int height, final int depth) {
        this(new float[width * height * depth], width, height, depth);
    }

    public static Volume3D forBoundingBox(final BoundingBox boundingBox) {
        final var size = Vertex.round(boundingBox.size());
        return new Volume3D((int) size.x(), (int) size.y(), (int) size.z());
    }

    @Override
    public void set(int x, int y, int z, float value) {
        final var index = z * width * height + y * width + x;
        this.data[index] = value;
    }

    @Override
    public float get(int x, int y, int z) {
        final var index = z * width * height + y * width + x;
        return this.data[index];
    }

    public Tensor toTensor() {
        final var shape = Shape.of(1, width, height, depth, 1);
        final var buffer = DataBuffers.of(this.data);
        final var ndArray = FloatDenseNdArray.create(buffer, shape);
        return TFloat32.tensorOf(ndArray);
    }

    public List<Voxel> toVoxels() {
        final var voxels = new ArrayList<Voxel>();

        for (var z = 0; z < depth; ++z) {
            for (var y = 0; y < height; ++y) {
                for (var x = 0; x < width; ++x) {
                    final var index = z * width * height + y * width + x;
                    final var value = this.data[index];
                    if (value != 0f) {
                        voxels.add(new Voxel(x, y, z, (byte) 80));
                    }
                }
            }
        }

        return voxels;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }
}
