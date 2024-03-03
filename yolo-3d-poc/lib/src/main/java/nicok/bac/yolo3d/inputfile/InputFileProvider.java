package nicok.bac.yolo3d.inputfile;

import static nicok.bac.yolo3d.util.DirectoryUtil.getExtension;

public final class InputFileProvider {

    public static InputFile get(final String path) throws Exception {
        final var extension = getExtension(path);

        return switch (extension) {
            case "vox" -> new VoxAdapter(path);
            case "off" -> new OffAdapter2(path);
            case "obj" -> null;
            default -> throw new IllegalExtensionException(extension);
        };
    }

    private InputFileProvider() {
    }
}
