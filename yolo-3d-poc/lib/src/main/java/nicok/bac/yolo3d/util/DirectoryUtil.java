package nicok.bac.yolo3d.util;

import nicok.bac.yolo3d.inputfile.IllegalPathException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.Runtime.getRuntime;

public final class DirectoryUtil {

    public static String stripExtension(final String filename) {
        return filename.split("\\.")[0];
    }

    public static String getFilename(final String path) {
        final var pathSegments = path.split("/");
        return stripExtension(pathSegments[pathSegments.length -1]);
    }

    public static String getExtension(final String path) {
        final var parts = path.split("\\.");

        if (parts.length < 2) {
            throw new IllegalPathException(path);
        }

        return parts[parts.length - 1];
    }

    public static String getRepositoryRoot() throws IOException {
        final var command = new String[]{"cmd.exe", "/c", "git rev-parse --show-toplevel"};
        final var process = getRuntime().exec(command);
        final var stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final var output = stdInput.readLine();
        return output.trim();
    }

    private DirectoryUtil() {
    }
}
