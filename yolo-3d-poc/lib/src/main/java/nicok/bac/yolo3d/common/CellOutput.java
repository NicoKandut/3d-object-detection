package nicok.bac.yolo3d.common;

import static java.util.Arrays.copyOfRange;
import static java.util.Objects.requireNonNull;

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
    public CellOutput {
        requireNonNull(classConfidence);
    }

    public static CellOutput fromOutputArray(float[] output) {
        requireNonNull(output);

        final var size = output.length;
        final var nrClasses = size - 7;

        return new CellOutput(
                copyOfRange(output, 0, nrClasses),
                output[nrClasses],
                output[nrClasses + 1],
                output[nrClasses + 2],
                output[nrClasses + 3],
                output[nrClasses + 4],
                output[nrClasses + 5],
                output[nrClasses + 6]
        );
    }
}
