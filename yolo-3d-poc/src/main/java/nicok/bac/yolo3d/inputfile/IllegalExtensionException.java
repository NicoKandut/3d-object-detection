package nicok.bac.yolo3d.inputfile;

import static java.lang.String.format;

public class IllegalExtensionException extends IllegalArgumentException{

    public IllegalExtensionException(String extension) {
        super(format("Files with extension .%s are not supported", extension));
    }
}
