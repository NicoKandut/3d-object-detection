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

    public static class Builder {
        private final List<Category> categories = new ArrayList<>();
        private final List<Model> trainModels = new ArrayList<>();
        private final List<Label> trainLabels = new ArrayList<>();
        private final List<Model> testModels = new ArrayList<>();
        private final List<Label> testLabels = new ArrayList<>();

        public Builder withTrainClaFile(final String path) throws IOException {
            scanClaFile(path, trainModels, trainLabels);
            return this;
        }

        public Builder withTestClaFile(final String path) throws IOException {
            scanClaFile(path, testModels, testLabels);
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

//    public static Dataset load(final String path) throws IOException {
//        final var claFileTrain = new File(path + "/classification/train.cla");
//        final var claFileTest = new File(path + "/classification/test.cla");
//
//        final var claCategories = new ArrayList<ClaCategory>();
//        final var claTrainModels = new ArrayList<ClaModel>();
//        final var claTestModels = new ArrayList<ClaModel>();
//
//
//        scanClaFile(claFileTest, claCategories, claTestModels);
//
//        final var topLevelCategories = claCategories.stream()
//                .filter(cat -> cat.parent().equals("0"))
//                .toList();
//
//        System.out.printf("Number of top-level-categories: %d\n", topLevelCategories.size());
//
//        final var categories = IntStream.range(0, topLevelCategories.size())
//                .mapToObj(id -> new Category(id, topLevelCategories.get(id).name()))
//                .toList();
//
//        final var trainModels = new ArrayList<Model>(claTrainModels.size());
//        final var trainLabels = new ArrayList<Label>(claTrainModels.size());
//        final var testModels = new ArrayList<Model>(claTestModels.size());
//        final var testLabels = new ArrayList<Label>(claTestModels.size());
//
//        toStandardFormat(path, claTrainModels, trainModels, categories, trainLabels);
//        toStandardFormat(path, claTestModels, testModels, categories, testLabels);
//
//        return new Dataset(
//                "Princeton Shape Benchmark",
//                categories,
//                trainModels,
//                trainLabels,
//                testModels,
//                testLabels
//        );
//    }

//    private static void toStandardFormat(
//            final String path,
//            final ArrayList<ClaModel> claModels,
//            final ArrayList<Model> models,
//            final List<Category> categories,
//            final ArrayList<Label> labels
//    ) {
//        for (final var claModel : claModels) {
//            final var name = claModel.name();
//            final var id = Integer.parseInt(name);
//            final var subFolder = id / 100;
//            final var modelPath = path + "/db/" + subFolder + "/m" + name + "/m" + name + ".off";
//            final var model = new Model(id, modelPath);
//            models.add(model);
//
//            final var categoryId = categories.stream()
//                    .filter(c -> c.name().equals(claModel.category().name()))
//                    .findFirst()
//                    .orElseThrow()
//                    .id();
//            final var label = new Label(id, categoryId);
//            labels.add(label);
//        }
//    }
}
