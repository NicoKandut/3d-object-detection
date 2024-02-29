package nicok.bac.yolo3d.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.Runtime.getRuntime;

public final class DirectoryUtil {

    public static String stripExtension(String filename) {
        return filename.split("\\.")[0];
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
