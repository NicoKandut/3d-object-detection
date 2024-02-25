package nicok.bac.yolo3d.inputfile;

public final class InputFileProvider {

    public static InputFile get(final String path) throws Exception {
        final var extension = getExtension(path);

        return switch (extension) {
            case "vox" -> new VoxAdapter(path);
            case "off" -> new OffAdapter(path);
            case "obj" -> null;
            default -> throw new IllegalExtensionException(extension);
        };
    }

    private static String getExtension(final String path) {
        final var parts = path.split("\\.");

        if (parts.length < 2) {
            throw new IllegalPathException(path);
        }

        return parts[parts.length - 1];
    }

    private InputFileProvider() {
    }
}
