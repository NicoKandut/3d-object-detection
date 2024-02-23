package nicok.bac.yolo3d.common;

import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;

public class Volume3D {

    private final FloatNdArray data;

    public Volume3D(int width, int height, int depth) {
        data = NdArrays.ofFloats(Shape.of(1, width, height, depth, 3));
    }

    public void set(int x, int y, int z, float r, float g, float b) {
        data.setFloat(r, 0, x, y, z, 0);
        data.setFloat(g, 0, x, y, z, 1);
        data.setFloat(b, 0, x, y, z, 2);
    }

    public Tensor toTensor() {
        return TFloat32.tensorOf(data);
    }

}
