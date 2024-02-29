package nicok.bac.yolo3d.inputfile;

import nicok.bac.yolo3d.preprocessing.PreProcessing;

import static nicok.bac.yolo3d.util.DirectoryUtil.getExtension;

public final class InputFileProvider {

    public static InputFile get(
            final String path,
            final PreProcessing preProcessing
    ) throws Exception {
        final var extension = getExtension(path);

        return switch (extension) {
            case "vox" -> new VoxAdapter(path);
            case "off" -> new OffAdapter2(path, preProcessing);
            case "obj" -> null;
            default -> throw new IllegalExtensionException(extension);
        };
    }

    private InputFileProvider() {
    }
}
