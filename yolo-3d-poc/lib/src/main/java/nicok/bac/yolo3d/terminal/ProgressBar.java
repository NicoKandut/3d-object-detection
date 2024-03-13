package nicok.bac.yolo3d.terminal;

public record ProgressBar(int width, int total) {

    public void printProgress(int progress) {
        final var ratio = (double) progress / (double) total;
        final var filled = (int) Math.round(ratio * (double) width);

        final var barFilled = "=".repeat(filled);
        final var barEmpty = " ".repeat(width - filled);
        final var percent = ratio * 100.0;

        System.out.printf("\r%d/%d [%s%s] %.2f%%", progress, total, barFilled, barEmpty, percent);

        if (ratio == 1.0) {
            System.out.print("\n");
        }
    }
}
