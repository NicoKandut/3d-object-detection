package nicok.bac.yolo3d.util;

import static nicok.bac.yolo3d.util.DirectoryUtil.getRepositoryRoot;

public final class RepositoryPaths {

    public static final String ROOT = getRepositoryRoot();
    public static final String DATASET_PSB = ROOT + "/dataset-psb";
    public static final String DATASET_VOX = ROOT + "/dataset-psb-vox";
    public static final String GENERATED_ASSETS = ROOT + "/assets-generated";
    public static final String YOLO_3D_PYTHON = ROOT + "/yolo-3d-python";
    public static final String SAVED_MODEL = ROOT + "/yolo-3d-python/saved_model";

    private RepositoryPaths() {
        throw new UnsupportedOperationException();
    }
}
