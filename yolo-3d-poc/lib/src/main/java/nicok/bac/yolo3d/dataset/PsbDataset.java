package nicok.bac.yolo3d.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.util.StringUtil.requireNonBlank;

public class PsbDataset {

    public static final String CLA_TRAIN = "/classification/train.cla";
    public static final String CLA_TEST = "/classification/test.cla";

    private List<Category> categories = new ArrayList<>();
    private List<Model> trainModels = new ArrayList<>();
    private List<Label> trainLabels = new ArrayList<>();
    private List<Model> testModels = new ArrayList<>();
    private List<Label> testLabels = new ArrayList<>();
    private String path;

    public PsbDataset withPath(final String path) throws IOException {
        requireNonBlank(path, "PSB dataset path must not be empty");

        this.path = path;
        scanClaFile(this.path + CLA_TRAIN, trainModels, trainLabels);
        scanClaFile(this.path + CLA_TEST, testModels, testLabels);
        return this;
    }

    private void scanClaFile(
            final String claPath,
            final List<Model> models,
            final List<Label> labels
    ) throws IOException {
        final var file = new File(claPath);
        try (final var reader = new BufferedReader(new FileReader(file))) {
            Category currentCategory = null;
            for (final var line : reader.lines().skip(3).filter(Predicate.not(String::isBlank)).map(String::trim).toList()) {

                // category header
                if (line.matches(".+ .+ .+")) {
                    final var parts = line.split(" ");
                    final var name = parts[0];

                    // check if category exists
                    final var category = categories.stream()
                            .filter(c -> c.name().equals(name))
                            .findFirst();
                    if (category.isPresent()) {
                        currentCategory = category.get();
                    } else {
                        currentCategory = new Category(categories.size(), name);
                        categories.add(currentCategory);
                    }
                }

                // model entry
                if (line.matches("\\d+")) {
                    final var id = Integer.parseInt(line);
                    final var subFolder = id / 100;
                    final var modelPath = this.path + "/db/" + subFolder + "/m" + id + "/m" + id + ".off";
                    models.add(new Model(id, modelPath));
                    labels.add(new Label(id, requireNonNull(currentCategory).id()));
                }
            }
        }
    }

    public PsbDataset withSelectedCategories(final List<String> selectedCategories) {
        final var newCategories = this.categories.stream()
                .filter(c -> selectedCategories.contains(c.name()))
                .toList();
        final var newTrainLabels = trainLabels.stream()
                .filter(label -> selectedCategories.contains(categories.get(label.categoryId()).name()))
                .map(label -> new Label(label.modelId(), selectedCategories.indexOf(categories.get(label.categoryId()).name())))
                .toList();
        final var newTestLabels = testLabels.stream()
                .filter(label -> selectedCategories.contains(categories.get(label.categoryId()).name()))
                .map(label -> new Label(label.modelId(), selectedCategories.indexOf(categories.get(label.categoryId()).name())))
                .toList();

        final var newTrainIds = newTrainLabels.stream().map(Label::modelId).toList();
        final var newTestIds = newTestLabels.stream().map(Label::modelId).toList();

        final var newTrainModels = trainModels.stream()
                .filter(m -> newTrainIds.contains(m.id()))
                .toList();
        final var newTestModels = testModels.stream()
                .filter(m -> newTestIds.contains(m.id()))
                .toList();

        this.categories = newCategories;
        this.trainLabels = newTrainLabels;
        this.trainModels = newTrainModels;
        this.testLabels = newTestLabels;
        this.testModels = newTestModels;

        return this;
    }

    public Dataset build() {
        return new Dataset(
                "Princeton Shape Benchmark",
                categories,
                trainModels,
                trainLabels,
                testModels,
                testLabels
        );
    }
}
