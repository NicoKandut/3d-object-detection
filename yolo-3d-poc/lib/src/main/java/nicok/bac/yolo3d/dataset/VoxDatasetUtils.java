package nicok.bac.yolo3d.dataset;

import nicok.bac.yolo3d.common.BoundingBox;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static nicok.bac.yolo3d.util.DirectoryUtil.requireExtension;

public final class VoxDatasetUtils {

    public static void saveCategoriesFile(
            final String path,
            final List<Category> categories
    ) throws IOException {
        requireExtension(path, ".txt");

        final var content = categories.stream()
                .map(category -> String.format("%d %s", category.id(), category.name()))
                .collect(Collectors.joining("\n"));

        try (final var writer = new FileWriter(path)) {
            writer.write(content);
        }
    }

    public static void saveSetFile(
            final String filename,
            final List<Integer> ids
    ) throws IOException {
        requireExtension(filename, ".txt");

        final var content = ids.stream()
                .map(id -> String.format("%s.vox %s.txt", id, id))
                .collect(Collectors.joining("\n"));

        try (final var writer = new FileWriter(filename)) {
            writer.write(content);
        }
    }

    public static void saveLabelFile(
            final String filename,
            final Label label,
            final BoundingBox boundingBox
    ) throws IOException {
        final var min = boundingBox.min();
        final var max = boundingBox.max();
        final var labelLine = String.format(
                "%d %s %s %s %s %s %s\n",
                label.categoryId(),
                min.x(),
                min.y(),
                min.z(),
                max.x(),
                max.y(),
                max.z()
        );

        try (final var labelWriter = new FileWriter(filename)) {
            labelWriter.write(labelLine);
        }
    }
}
