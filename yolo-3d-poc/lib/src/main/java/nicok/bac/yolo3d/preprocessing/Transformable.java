package nicok.bac.yolo3d.preprocessing;

public interface Transformable<T> {
    T transform(final Transformation transformation);
}
