package nicok.bac.yolo3d;

import nicok.bac.yolo3d.dataset.PrincetonShapeBenchmark;
import nicok.bac.yolo3d.model.Yolo3D;
import nicok.bac.yolo3d.util.CommandLineUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.tensorflow.types.TFloat32;

import static nicok.bac.yolo3d.model.Yolo3D.INPUT_NAME;
import static nicok.bac.yolo3d.util.CommandLineUtil.parseCommandLine;

public final class Main {

    public static final String REPOSITORY_PATH = "C:/src/bac";
    public static final String OFF_PATH = REPOSITORY_PATH + "/yolo-3d-poc/assets/mushroom.off";
    public static final Options OPTIONS = new Options()
            .addOption("e", "epochs", true, "Number of training epochs");

    public static void main(final String[] args) throws Exception {
        final var commandLine = parseCommandLine(args, OPTIONS);
        final var epochs = getEpochs(commandLine);

        final var dataset = new PrincetonShapeBenchmark();

        final var model = new Yolo3D();

        for (var i = 0; i < epochs; ++i) {
            for (final var batch : dataset.getTrainingBatches(100)) {
                final var x = batch.x();
                final var y = batch.y();
                try (final var session = model.session();
                     final var result = session.runner()
                             .feed(TARGET, y)
                             .feed(INPUT_NAME, x)
                             .addTarget(TRAIN)
                             .fetch(TRAINING_LOSS)
                             .run()
                ) {
                    final var loss = (TFloat32) result.get(0);
                    System.out.println("Iteration = " + i + ", training loss = " + loss.getFloat());
                    model.save();
                }
            }
        }
    }

    private static int getEpochs(final CommandLine commandLine) {
        final var epochString = commandLine.getOptionValue("epochs", "1");
        try {
            return Integer.parseInt(epochString);
        } catch (final NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
            return 1;
        }
    }
}