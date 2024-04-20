package nicok.bac.yolo3d.util;

import java.util.function.*;

public final class ExceptionUtil {

    public static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R, Exception> function) {
        return (T value) -> {
            try {
                return function.apply(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> Supplier<T> unchecked(ThrowingSupplier<T, Exception> function) {
        return () -> {
            try {
                return function.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static LongSupplier unchecked(ThrowingLongSupplier<Exception> function) {
        return () -> {
            try {
                return function.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> Consumer<T> unchecked(ThrowingConsumer<T, Exception> function) {
        return (T t) -> {
            try {
                function.accept(t);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }


    public static LongConsumer unchecked(ThrowingLongConsumer<Exception> function) {
        return (long l) -> {
            try {
                function.accept(l);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
