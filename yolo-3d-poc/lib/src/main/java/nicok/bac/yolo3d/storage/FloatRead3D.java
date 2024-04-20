package nicok.bac.yolo3d.storage;

/**
 * Interface for reading float values from a 3D grid storage.
 */
public interface FloatRead3D {
    float get(final int x, final int y, final int z);
}
