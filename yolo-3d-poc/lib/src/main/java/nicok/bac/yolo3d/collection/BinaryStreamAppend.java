package nicok.bac.yolo3d.collection;

import java.io.DataOutputStream;

public interface BinaryStreamAppend {
    void appendTo(DataOutputStream target);
}
