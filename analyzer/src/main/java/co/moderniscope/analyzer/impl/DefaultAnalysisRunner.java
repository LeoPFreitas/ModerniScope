
package co.moderniscope.analyzer.impl;

import co.moderniscope.analyzer.Neo4jDependencyExporter;
import co.moderniscope.analyzer.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Default implementation of the AnalysisRunner
 */
public class DefaultAnalysisRunner implements AnalysisRunner {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAnalysisRunner.class);
    private final AnalyzerFactory factory;

    public DefaultAnalysisRunner() {
        this(new DefaultAnalyzerFactory());
    }

    public DefaultAnalysisRunner(AnalyzerFactory factory) {
        this.factory = factory;
    }

    @Override
    public DependencyGraph analyzeProject(Path projectPath) throws AnalysisException {
        logger.info("Starting analysis of project at: {}", projectPath.toAbsolutePath());
        CodeAnalyzer analyzer = factory.createAnalyzer();
        analyzer.analyzeProject(projectPath);
        return analyzer.getDependencyGraph();
    }

    @Override
    public Path exportToDot(DependencyGraph graph) throws AnalysisException {
        return exportToDot(graph, Paths.get("dependency-graph.dot"));
    }

    @Override
    public Path exportToDot(DependencyGraph graph, Path outputPath) throws AnalysisException {
        try {
            graph.exportToDot(outputPath);
            logger.info("Graph exported to DOT format at: {}", outputPath);
            return outputPath;
        } catch (IOException e) {
            throw new AnalysisException("Failed to export graph to DOT format", e);
        }
    }

    @Override
    public Path exportToJson(DependencyGraph graph) throws AnalysisException {
        return exportToJson(graph, Paths.get("dependency-graph.json"));
    }

    @Override
    public Path exportToJson(DependencyGraph graph, Path outputPath) throws AnalysisException {
        try {
            graph.exportToJson(outputPath);
            logger.info("Graph exported to JSON format at: {}", outputPath);
            return outputPath;
        } catch (IOException e) {
            throw new AnalysisException("Failed to export graph to JSON format", e);
        }
    }

    @Override
    public void exportToNeo4j(DependencyGraph graph, String uri, String username, String password) throws AnalysisException {
        try (Neo4jDependencyExporter exporter = new Neo4jDependencyExporter(uri, username, password)) {
            logger.info("Exporting to Neo4j at {}", uri);
            exporter.exportGraph(graph);
            logger.info("Graph successfully exported to Neo4j!");
        } catch (Exception e) {
            throw new AnalysisException("Failed to export graph to Neo4j", e);
        }
    }
}