package nicok.bac.yolo3d.util;

import java.io.IOException;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T t) throws IOException;
}