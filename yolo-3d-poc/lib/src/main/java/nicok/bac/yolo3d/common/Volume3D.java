package nicok.bac.yolo3d.common;

import com.scs.voxlib.Voxel;
import nicok.bac.yolo3d.off.Vertex;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.BooleanNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TBool;

import java.util.ArrayList;
import java.util.List;

public class Volume3D {

    private final BooleanNdArray data;
    private final BoundingBox boundingBox;

    public Volume3D(int width, int height, int depth) {
        data = NdArrays.ofBooleans(Shape.of(1, width, height, depth, 1));
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

    public void set(int x, int y, int z, boolean value) {
        data.setBoolean(value, 0, x, y, z, 0);
    }

    public Tensor toTensor() {
        return TBool.tensorOf(data);
    }

    public List<Voxel> toVoxels() {
        final var voxels = new ArrayList<Voxel>();
        for (var i = 0; i < data.shape().get(1); ++i) {
            for (var j = 0; j < data.shape().get(2); ++j) {
                for (var k = 0; k < data.shape().get(3); ++k) {
                    final var value = data.getBoolean(0, i, j, k, 0);

                    if (value) {
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
