package co.moderniscope.analyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AnalyzerApp {
    public static void main(String[] args) {
        String projectPath = args.length > 0 ? args[0] : ".";

        Path path = Paths.get(projectPath);

        if (!Files.exists(path)) {
            System.err.println("Error: The specified path does not exist: " + path.toAbsolutePath());
            System.exit(1);
        }

        if (!Files.isDirectory(path)) {
            System.err.println("Error: The specified path is not a directory: " + path.toAbsolutePath());
            System.exit(1);
        }

        JavaFileScanner scanner = new JavaFileScanner();
        scanner.scanAndBuildGraph(projectPath);
    }
}