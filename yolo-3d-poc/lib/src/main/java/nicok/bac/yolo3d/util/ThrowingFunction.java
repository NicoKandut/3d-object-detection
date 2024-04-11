package nicok.bac.yolo3d.util;

/**
 * Identical to {@link java.util.function.Consumer} but allows exceptions to be thrown.
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}