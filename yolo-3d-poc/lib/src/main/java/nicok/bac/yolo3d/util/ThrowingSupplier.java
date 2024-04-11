package nicok.bac.yolo3d.util;

/**
 * Identical to {@link java.util.function.Consumer} but allows exceptions to be thrown.
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    T get() throws E;
}