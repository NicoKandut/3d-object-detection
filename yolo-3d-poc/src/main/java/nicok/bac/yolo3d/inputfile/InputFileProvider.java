package nicok.bac.yolo3d.inputfile;

import static java.lang.String.format;

public final class InputFileProvider {
    public static InputFile get(String path) {
        final var extension = getExtension(path);

        return switch (extension) {
            case "vox" -> new VoxAdapter(path);
            case "off" -> new OffAdapter(path);
            default -> throw new IllegalArgumentException(
                    format("InputFileProvider does not support .%s files", extension)
            );
        };
    }

    private static String getExtension(String path) {
        final var parts = path.split("\\.");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Path must have extension");
        }

        return parts[parts.length - 1];
    }
}
