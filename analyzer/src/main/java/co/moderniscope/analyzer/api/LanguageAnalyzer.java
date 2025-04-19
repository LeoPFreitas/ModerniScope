package co.moderniscope.analyzer.api;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for language-specific source code analyzers.
 */
public interface LanguageAnalyzer {
    /**
     * Gets the file extensions supported by this analyzer.
     *
     * @return List of supported file extensions (e.g., ".java", ".py")
     */
    List<String> getSupportedFileExtensions();

    /**
     * Adds a source directory to help with symbol resolution.
     *
     * @param sourceDir Path to a source directory
     */
    void addSourceDirectory(Path sourceDir);

    /**
     * Analyzes a single source file and updates the dependency graph.
     *
     * @param file  Path to the source file
     * @param graph The dependency graph to update
     * @return true if analysis was successful, false otherwise
     */
    boolean analyzeFile(Path file, DependencyGraph graph);
}