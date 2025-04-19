package co.moderniscope.analyzer.api;

import java.nio.file.Path;

/**
 * Main interface for a code analysis engine that builds dependency graphs
 * across multiple languages.
 */
public interface CodeAnalyzer {
    /**
     * Adds a source directory to be analyzed.
     *
     * @param sourceDir Path to a source directory
     */
    void addSourceDirectory(Path sourceDir);

    /**
     * Analyzes a project and builds its dependency graph.
     *
     * @param projectRoot Root directory of the project
     */
    void analyzeProject(Path projectRoot);

    /**
     * Gets the dependency graph built from analysis.
     *
     * @return The dependency graph
     */
    DependencyGraph getDependencyGraph();
}