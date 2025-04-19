
package co.moderniscope.analyzer.api;

import java.nio.file.Path;

/**
 * API for running code analysis operations
 */
public interface AnalysisRunner {
    /**
     * Analyzes a project at the specified path
     *
     * @param projectPath Path to the project root
     * @return The dependency graph resulting from the analysis
     * @throws AnalysisException if analysis fails
     */
    DependencyGraph analyzeProject(Path projectPath) throws AnalysisException;

    /**
     * Exports the dependency graph to DOT format using a default path
     *
     * @param graph The dependency graph to export
     * @return The actual path where the file was written
     * @throws AnalysisException if export fails
     */
    Path exportToDot(DependencyGraph graph) throws AnalysisException;

    /**
     * Exports the dependency graph to DOT format
     *
     * @param graph      The dependency graph to export
     * @param outputPath Path where the DOT file should be written
     * @return The actual path where the file was written
     * @throws AnalysisException if export fails
     */
    Path exportToDot(DependencyGraph graph, Path outputPath) throws AnalysisException;

    /**
     * Exports the dependency graph to JSON format using a default path
     *
     * @param graph The dependency graph to export
     * @return The actual path where the file was written
     * @throws AnalysisException if export fails
     */
    Path exportToJson(DependencyGraph graph) throws AnalysisException;

    /**
     * Exports the dependency graph to JSON format
     *
     * @param graph      The dependency graph to export
     * @param outputPath Path where the JSON file should be written
     * @return The actual path where the file was written
     * @throws AnalysisException if export fails
     */
    Path exportToJson(DependencyGraph graph, Path outputPath) throws AnalysisException;

    /**
     * Exports the dependency graph to Neo4j
     *
     * @param graph    The dependency graph to export
     * @param uri      Neo4j URI (e.g., neo4j://localhost:7687)
     * @param username Neo4j username
     * @param password Neo4j password
     * @throws AnalysisException if export fails
     */
    void exportToNeo4j(DependencyGraph graph, String uri, String username, String password) throws AnalysisException;
}