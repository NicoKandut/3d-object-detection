package nicok.bac.yolo3d.dataset;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record Dataset(
        String name,
        List<Category> categories,
        List<Model> trainModels,
        List<Label> trainLabels,
        List<Model> testModels,
        List<Label> testLabels
) {
    public Dataset {
        requireNonNull(name);
        requireNonNull(categories);
        requireNonNull(trainModels);
        requireNonNull(trainLabels);
        requireNonNull(testModels);
        requireNonNull(testLabels);
    }

    public void printSummary() {
        System.out.printf("Dataset: %s\n", name);
        System.out.printf("  - %d models\n", trainModels.size() + testModels.size());
        System.out.printf("  - %d categories\n", categories.size());
    }
}
