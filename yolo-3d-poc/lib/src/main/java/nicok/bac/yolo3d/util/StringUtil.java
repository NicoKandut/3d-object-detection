package nicok.bac.yolo3d.util;

import static java.util.Objects.requireNonNull;

public final class StringUtil {

    public static String requireNonBlank(final String value) {
        if (requireNonNull(value).isBlank()) {
            throw new IllegalArgumentException("String value cannot be blank");
        }
        return value;
    }

    public static <T> T[] requireLength(final T[] value, final int length) {
        if (requireNonNull(value).length != length) {
            throw new IllegalArgumentException("Array must have length " + length);
        }
        return value;
    }

    private StringUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
}
