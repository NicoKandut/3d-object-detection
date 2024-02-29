package nicok.bac.yolo3d.tensor;

import org.tensorflow.Tensor;

public class TensorConverter {

    public static <T> T fromTensor(Class<T> targetClass, Tensor tensor) {
//        return switch (targetClass) {
//            case ResultBoundingBox.class -> new ResultBoundingBox(
//                    Category.BIKE,
//                    1.0,
//                    new BoundingBox(new Point(0,0,0), new Point(1,1,1))
//            );
//            case BoundingBox.class -> new BoundingBox(new Point(2,2,2), new Point(3,3,3));
//            default -> throw new UnsupportedOperationException("Cannot parse " + targetClass.getName() + " from tensor");
//        };
        return null;
    }

    public static <T> Tensor toTensor(T object) {
        return null;
    }
}
