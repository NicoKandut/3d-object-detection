package nicok.bac.yolo3d.util;


@FunctionalInterface
public interface ThrowingLongConsumer<E extends Exception> {
    void accept(long l) throws E;
}


