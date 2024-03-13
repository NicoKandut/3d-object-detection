package nicok.bac.yolo3d.preprocessing;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.common.Point;
import nicok.bac.yolo3d.inputfile.InputFile;
import nicok.bac.yolo3d.off.Vertex;

import java.util.Random;

public final class RandomTransformation {

    public static InputFile randomTransformation(InputFile inputFile, final BoundingBox targetBoundingBox) {
        // scale 3d-models to cnn-input
        final var random = new Random();
        final var beta = (random.nextDouble() - 0.5) * Math.PI / 8;
        final var gamma = (random.nextDouble() - 0.5) * Math.PI / 8;
        final var alpha = random.nextDouble() * 2 * Math.PI;
        final var randomRotation = new LinearTransformation.Builder()
                .rotationCenter(inputFile.getBoundingBox().center())
                .rotate(alpha, beta, gamma)
                .scaling(80.0)
                .build();

        final var newSize = ((double) 112 / 2) + random.nextDouble() * ((double) 112 / 2);
        final var newBoundingBox = new BoundingBox(
                Point.ZERO,
                new Point(newSize, newSize, newSize)
        );

        final var resize = new FitToBox()
                .withTargetBoundingBox(newBoundingBox);
        final var availableSpace = Point.sub(targetBoundingBox.size(), newBoundingBox.size());
        final var shiftX = random.nextDouble() * availableSpace.x();
        final var shiftY = random.nextDouble() * availableSpace.y();
        final var shiftZ = random.nextDouble() * availableSpace.z();
        final var randomShift = new LinearTransformation.Builder()
                .shift(new Vertex(shiftX, shiftY, shiftZ))
                .build();

        // apply rotation
        inputFile = inputFile.withPreprocessing(randomRotation);

        // apply random scaling
        resize.withSourceBoundingBox(inputFile.getBoundingBox());
        inputFile = inputFile.withPreprocessing(resize);

        // apply random offset
        inputFile = inputFile.withPreprocessing(randomShift);
        return inputFile;
    }

    private RandomTransformation() {
    }
}
