package nicok.bac.yolo3d.common;

import static java.util.Arrays.copyOfRange;

public record CellOutput(
        float[] classConfidence,
        float x,
        float y,
        float z,
        float w,
        float h,
        float d,
        float confidence
) {
    public static CellOutput fromOutputArray(float[] output) {
        assert(output.length == 6 + 1 + Category.values().length);
        return new CellOutput(
                copyOfRange(output, 0, 2),
                output[2],
                output[3],
                output[4],
                output[5],
                output[6],
                output[7],
                output[8]
        );
    }
}
