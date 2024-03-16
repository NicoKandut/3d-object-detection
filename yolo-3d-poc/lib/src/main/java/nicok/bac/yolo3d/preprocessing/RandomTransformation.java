package nicok.bac.yolo3d.preprocessing;

import nicok.bac.yolo3d.common.BoundingBox;
import nicok.bac.yolo3d.inputfile.InputFile;
import nicok.bac.yolo3d.off.Vertex;

import java.util.Random;

public final class RandomTransformation {

    public static InputFile randomTransformation(InputFile inputFile, final BoundingBox targetBoundingBox) {
        final var random = new Random();
        final var randomRotation = getRandomRotation(inputFile.getBoundingBox(), random);

        // scale 3d-models to cnn-input
        final var newSize = ((double) 112 / 2) + random.nextDouble() * ((double) 112 / 2);
        final var newBoundingBox = BoundingBox.fromOrigin(newSize);

        final var resize = new FitToBox()
                .withTargetBoundingBox(newBoundingBox);
        final var randomShift = getRandomShift(targetBoundingBox, newBoundingBox, random);

        // apply rotation
        inputFile = inputFile.withPreprocessing(randomRotation);

        // apply random scaling
        resize.withSourceBoundingBox(inputFile.getBoundingBox());
        inputFile = inputFile.withPreprocessing(resize);

        // apply random offset
        inputFile = inputFile.withPreprocessing(randomShift);
        return inputFile;
    }

    public static LinearTransformation getRandomShift(
            final BoundingBox space,
            final BoundingBox object,
            final Random random
    ) {
        final var availableSpace = Vertex.sub(space.size(), object.size());
        final var shiftX = random.nextDouble() * availableSpace.x();
        final var shiftY = random.nextDouble() * availableSpace.y();
        final var shiftZ = random.nextDouble() * availableSpace.z();
        return new LinearTransformation.Builder()
                .shift(new Vertex(shiftX, shiftY, shiftZ))
                .build();
    }

    public static LinearTransformation getRandomRotation(BoundingBox boundingBox, Random random) {
        final var beta = (random.nextDouble() - 0.5) * Math.PI / 8;
        final var gamma = (random.nextDouble() - 0.5) * Math.PI / 8;
        final var alpha = random.nextDouble() * 2 * Math.PI;
        return new LinearTransformation.Builder()
                .rotationCenter(boundingBox.center())
                .rotate(alpha, beta, gamma)
                .build();
    }

    private RandomTransformation() {
    }
}
