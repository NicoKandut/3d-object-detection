package nicok.bac.yolo3d.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class PsbDataset {

    public static final String CLA_TRAIN = "/classification/train.cla";
    public static final String CLA_TEST = "/classification/test.cla";

    private final List<Category> categories = new ArrayList<>();
    private final List<Model> trainModels = new ArrayList<>();
    private final List<Label> trainLabels = new ArrayList<>();
    private final List<Model> testModels = new ArrayList<>();
    private final List<Label> testLabels = new ArrayList<>();

    public PsbDataset withTrainClaFile(final String path) throws IOException {
        scanClaFile(path, trainModels, trainLabels);
        return this;
    }

    public PsbDataset withTestClaFile(final String path) throws IOException {
        scanClaFile(path, testModels, testLabels);
        return this;
    }

    public PsbDataset withPath(final String path) throws IOException {
        withTrainClaFile(path + CLA_TRAIN);
        withTestClaFile(path + CLA_TEST);
        return this;
    }

    private void scanClaFile(
            final String path,
            final List<Model> models,
            final List<Label> labels
    ) throws IOException {
        final var file = new File(path);
        try (final var reader = new BufferedReader(new FileReader(file))) {
            Category currentCategory = null;
            for (final var line : reader.lines().skip(3).filter(Predicate.not(String::isBlank)).map(String::trim).toList()) {

                // category header
                if (line.matches(".+ .+ .+")) {
                    final var parts = line.split(" ");
                    final var name = parts[0];
                    final var parent = parts[1];

                    // only use top level categories
                    if (parent.equals("0")) {
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
                }

                // model entry
                if (line.matches("\\d+")) {
                    final var id = Integer.parseInt(line);
                    final var subFolder = id / 100;
                    final var modelPath = path + "/db/" + subFolder + "/m" + id + "/m" + id + ".off";
                    models.add(new Model(id, modelPath));
                    labels.add(new Label(id, requireNonNull(currentCategory).id()));
                }
            }
        }
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
