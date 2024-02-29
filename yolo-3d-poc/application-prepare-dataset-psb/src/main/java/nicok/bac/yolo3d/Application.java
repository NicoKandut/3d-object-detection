package nicok.bac.yolo3d;

import nicok.bac.yolo3d.dataset.PsbDataset;

import java.io.IOException;

import static nicok.bac.yolo3d.util.DirectoryUtil.getRepositoryRoot;

public class Application {

    private static final String DATASET_PSB_PATH = "/dataset-psb";
    public static final String CLA_TRAIN = "/classification/train.cla";
    public static final String CLA_TEST = "/classification/test.cla";

    public static void main(final String[] args) {
        try {
            final var rootDir = getRepositoryRoot();
            final var dataset = new PsbDataset.Builder()
                    .withTrainClaFile(rootDir + DATASET_PSB_PATH + CLA_TRAIN)
                    .withTestClaFile(rootDir + DATASET_PSB_PATH + CLA_TEST)
                    .build();

            dataset.printSummary();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
