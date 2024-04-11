package nicok.bac.yolo3d.util;

/**
 * Identical to {@link java.util.function.Consumer} but allows exceptions to be thrown.
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface ThrowingLongOperator<E extends Exception> {
    long apply(long l) throws E;
}