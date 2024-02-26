package nicok.bac.yolo3d.common;

import com.scs.voxlib.Voxel;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;

import java.util.ArrayList;
import java.util.List;

public class Volume3D {

    private final FloatNdArray data;
    private final BoundingBox boundingBox;

    public Volume3D(int width, int height, int depth) {
        data = NdArrays.ofFloats(Shape.of(1, width, height, depth, 3));
        boundingBox = new BoundingBox(Point.ZERO, new Point(width, height, depth));
    }

    public static Volume3D forBoundingBox(final BoundingBox boundingBox) {
        // TODO: remove + 1?
        return new Volume3D(
                (int) Math.ceil(boundingBox.size().x()) + 1,
                (int) Math.ceil(boundingBox.size().y()) + 1,
                (int) Math.ceil(boundingBox.size().z()) + 1
        );
    }

    public void set(int x, int y, int z, float r, float g, float b) {
        data.setFloat(r, 0, x, y, z, 0);
        data.setFloat(g, 0, x, y, z, 1);
        data.setFloat(b, 0, x, y, z, 2);
    }

    public Tensor toTensor() {
        return TFloat32.tensorOf(data);
    }

    public List<Voxel> toVoxels() {
        final var voxels = new ArrayList<Voxel>();
        for (var i = 0; i < data.shape().get(1); ++i) {
            for (var j = 0; j < data.shape().get(2); ++j) {
                for (var k = 0; k < data.shape().get(3); ++k) {
                    final var value = data.get(0, i, j, k);
                    final var r = value.getFloat(0);
                    final var g = value.getFloat(1);
                    final var b = value.getFloat(2);

                    if (r + g + b > 0) {
                        voxels.add(new Voxel(i, j, k, (byte) 80));
                    }
                }
            }
        }
        return voxels;
    }

    public BoundingBox boundingBox() {
        return boundingBox;
    }
}
