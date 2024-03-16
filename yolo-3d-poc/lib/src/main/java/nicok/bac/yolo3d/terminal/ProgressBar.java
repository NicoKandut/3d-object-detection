package nicok.bac.yolo3d.terminal;

/**
 * Simple progress bar for the terminal.
 *
 * @param width the width of the progress bar in characters
 * @param total the total number of steps to reach completion
 */
public record ProgressBar(int width, int total) {

    /**
     * Prints the progress bar to the terminal.
     *
     * @param progress the current progress in steps
     */
    public void printProgress(final int progress) {
        final var ratio = (double) progress / (double) total;
        final var filled = (int) Math.round(ratio * (double) width);

        final var barFilled = "=".repeat(filled);
        final var barEmpty = " ".repeat(width - filled);
        final var percent = ratio * 100.0;

        System.out.printf("\r%d/%d [%s%s] %.2f%%", progress, total, barFilled, barEmpty, percent);

        if (ratio >= 1.0) {
            System.out.print("\n");
        }
    }
}
