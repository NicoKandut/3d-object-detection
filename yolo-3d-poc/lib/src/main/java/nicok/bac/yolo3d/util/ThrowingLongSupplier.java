package nicok.bac.yolo3d.util;


@FunctionalInterface
public interface ThrowingLongSupplier<E extends Exception> {
    long get() throws E;
}


