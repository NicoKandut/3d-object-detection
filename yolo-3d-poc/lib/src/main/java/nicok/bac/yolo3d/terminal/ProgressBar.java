package nicok.bac.yolo3d.terminal;

/**
 * Simple progress bar for the terminal.
 *
 * @param width the width of the progress bar in characters
 * @param total the total number of steps to reach completion
 */
public record ProgressBar(long width, long total) {

    /**
     * Prints the progress bar to the terminal.
     *
     * @param progress the current progress in steps
     */
    public void printProgress(final long progress) {
        this.printProgress(progress, "");
    }

    public void printProgress(final long progress, final String details) {
        final var ratio = Math.min((double) progress / (double) total, 1);
        final var filled = Math.round(ratio * (double) width);

        final var barFilled = "=".repeat((int) Math.min(filled, total));
        final var barEmpty = " ".repeat((int) Math.max(width - filled, 0));
        final var percent = ratio * 100.0;
        final var detailsString = details == null || details.isEmpty() ? "" : 2 + details;

        System.out.printf("\r%d/%d [%s%s] %.2f%% %s", progress, total, barFilled, barEmpty, percent, detailsString);

        if (ratio >= 1.0) {
            System.out.print("\n");
        }
    }
}
