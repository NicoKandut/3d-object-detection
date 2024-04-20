package nicok.bac.yolo3d.storage;

/**
 * Interface for writing float values to a 3D grid storage.
 */
public interface FloatWrite3D {
    void set(final int x, final int y, final int z, final float value);
}
