package co.moderniscope.analyzer.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Represents a dependency graph that can be built, queried, and exported.
 */
public interface DependencyGraph {
    /**
     * Adds a dependency relationship to the graph.
     *
     * @param from             Source node
     * @param to               Target node
     * @param relationshipType Type of relationship
     */
    void addDependency(String from, String to, String relationshipType);

    /**
     * Exports the graph to DOT format.
     *
     * @param outputPath Path to write the DOT file
     * @throws IOException If an error occurs during writing
     */
    void exportToDot(Path outputPath) throws IOException;

    /**
     * Exports the graph to JSON format.
     *
     * @param outputPath Path to write the JSON file
     * @throws IOException If an error occurs during writing
     */
    void exportToJson(Path outputPath) throws IOException;

    /**
     * Gets the raw graph data structure.
     *
     * @return Map representing the graph
     */
    Map<String, Map<String, Set<String>>> getRawGraph();
}