package nicok.bac.yolo3d.common;

import com.scs.voxlib.Voxel;
import nicok.bac.yolo3d.off.Vertex;
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
        data = NdArrays.ofFloats(Shape.of(1, width, height, depth, 1));
        boundingBox = new BoundingBox(Vertex.ORIGIN, new Vertex(width, height, depth));
    }

    public static Volume3D forBoundingBox(final BoundingBox boundingBox) {
        final var size = Vertex.round(boundingBox.size());
        return new Volume3D(
                (int) size.x(),
                (int) size.y(),
                (int) size.z()
        );
    }

    public void set(int x, int y, int z, float value) {
        data.setFloat(value, 0, x, y, z, 0);
    }

    public Tensor toTensor() {
        return TFloat32.tensorOf(data);
    }

    public List<Voxel> toVoxels() {
        final var voxels = new ArrayList<Voxel>();
        for (var i = 0; i < data.shape().get(1); ++i) {
            for (var j = 0; j < data.shape().get(2); ++j) {
                for (var k = 0; k < data.shape().get(3); ++k) {
                    final var value = data.getFloat(0, i, j, k, 0);

                    if (value != 0f) {
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
