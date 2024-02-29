package nicok.bac.yolo3d;

import nicok.bac.yolo3d.dataset.PsbDataset;

import java.io.IOException;

import static nicok.bac.yolo3d.util.DirectoryUtil.getRepositoryRoot;

public class Application {

    private static final String DATASET_PSB_PATH = "/dataset-psb";

    public static void main(final String[] args) {
        try {
            final var rootDir = getRepositoryRoot();
            final var datasetPath = rootDir + DATASET_PSB_PATH;
            final var dataset = new PsbDataset()
                    .withPath(datasetPath)
                    .build();

            dataset.printSummary();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
