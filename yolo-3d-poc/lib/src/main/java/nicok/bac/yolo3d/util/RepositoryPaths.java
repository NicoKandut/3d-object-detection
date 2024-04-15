package nicok.bac.yolo3d.util;

import static nicok.bac.yolo3d.util.DirectoryUtil.getRepositoryRoot;

/**
 * Helper class to find filesystem locations within the repository
 */
public final class RepositoryPaths {

    public static final String ROOT = getRepositoryRoot();
    public static final String DATASET_PSB = ROOT + "/dataset-psb";
    public static final String DATASET_VOX = ROOT + "/dataset-psb-vox";
    public static final String YOLO_3D_PYTHON = ROOT + "/yolo-3d-python";
    public static final String SAVED_MODEL = ROOT + "/yolo-3d-python/saved_model";
    public static final String SORTING_TEMP = ROOT + "/yolo-3d-poc/sorting";
    public static final String VOLUME_DATA_TEMP = ROOT + "/yolo-3d-poc/volume";
    public static final String BOX_DATA_TEMP = ROOT + "/yolo-3d-poc/boxes";

    private RepositoryPaths() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
}
