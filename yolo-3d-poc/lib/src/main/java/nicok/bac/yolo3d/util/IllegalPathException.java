package nicok.bac.yolo3d.util;

import static java.lang.String.format;

public class IllegalPathException extends IllegalArgumentException{
    public IllegalPathException(final String path) {
        super(format("Path must (at least) have filename and extension. '%s' did not.", path));
    }
}
