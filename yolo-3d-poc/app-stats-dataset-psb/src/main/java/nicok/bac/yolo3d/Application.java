package nicok.bac.yolo3d;

import nicok.bac.yolo3d.dataset.PsbDataset;

import java.io.IOException;

import static nicok.bac.yolo3d.util.RepositoryPaths.DATASET_PSB;

public class Application {

    public static void main(final String[] args) throws IOException {
        new PsbDataset()
                .withPath(DATASET_PSB)
                .build()
                .printSummary();
    }
}
