package nicok.bac.yolo3d.util;

import nicok.bac.yolo3d.inputfile.IllegalPathException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import static java.lang.Runtime.getRuntime;
import static java.util.Objects.requireNonNull;
import static nicok.bac.yolo3d.util.StringUtil.requireNonBlank;

public final class DirectoryUtil {

    public static String stripExtension(final String filename) {
        return filename.split("\\.")[0];
    }

    public static String getFilename(final String path) {
        final var pathSegments = path.split("/");
        return stripExtension(pathSegments[pathSegments.length - 1]);
    }

    public static String getExtension(final String path) {
        final var parts = path.split("\\.");

        if (parts.length < 2) {
            throw new IllegalPathException(path);
        }

        return parts[parts.length - 1];
    }

    public static void requireExtension(final String path, final String... extensions) {
        requireNonBlank(path);
        requireNonNull(extensions);
        for (final String extension : extensions) {
            requireNonNull(extension);
            if (path.endsWith(extension)) {
                return;
            }
        }

        throw new IllegalArgumentException(
                String.format("File must be one of (%s)", Arrays.toString(extensions))
        );
    }

    public static String getRepositoryRoot() {
        final var command = new String[]{"cmd.exe", "/c", "git rev-parse --show-toplevel"};
        try {
            final var process = getRuntime().exec(command);
            final var stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final var output = stdInput.readLine();
            return output.trim();
        } catch (final IOException exception) {
            return null;
        }
    }

    private DirectoryUtil() {
        throw new UnsupportedOperationException();
    }
}
