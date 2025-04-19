package co.moderniscope.cli;

import co.moderniscope.analyzer.api.AnalysisException;
import co.moderniscope.analyzer.api.AnalysisRunner;
import co.moderniscope.analyzer.api.DependencyGraph;
import co.moderniscope.analyzer.impl.DefaultAnalysisRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Command-line interface for the Code Analyzer using Picocli.
 */
@Command(
        name = "analyze",
        mixinStandardHelpOptions = true,
        version = "Code Analyzer 1.0",
        description = "Analyzes a project for modernization risk assessment."
)
public class CodeAnalyzerCli implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CodeAnalyzerCli.class);

    @Parameters(index = "0", description = "Path to the project root directory")
    private Path projectPath;

    @Option(names = {"-d", "--dot"}, description = "Path to export DOT file (optional)")
    private Path dotOutputPath;

    @Option(names = {"-j", "--json"}, description = "Path to export JSON file (optional)")
    private Path jsonOutputPath;

    @Option(names = {"-n", "--neo4j"}, description = "Export to Neo4j")
    private boolean exportToNeo4j;

    @Option(names = {"--uri"}, description = "Neo4j URI (default: ${DEFAULT-VALUE})")
    private String neo4jUri = "neo4j://localhost:7687";

    @Option(names = {"--username"}, description = "Neo4j username (default: ${DEFAULT-VALUE})")
    private String neo4jUsername = "neo4j";

    @Option(names = {"--password"}, description = "Neo4j password", interactive = true)
    private String neo4jPassword;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CodeAnalyzerCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        try {
            logger.info("Code Analyzer for Modernization Risk Assessment");
            logger.info("==============================================");
            logger.info("Analyzing project at: {}", projectPath.toAbsolutePath());

            AnalysisRunner runner = new DefaultAnalysisRunner();
            DependencyGraph graph = runner.analyzeProject(projectPath);

            // Export to DOT format
            if (dotOutputPath != null) {
                logger.info("Exporting to DOT at: {}", dotOutputPath);
                runner.exportToDot(graph, dotOutputPath);
            } else {
                logger.info("Exporting to DOT with default path");
                runner.exportToDot(graph);
            }

            // Export to JSON format
            if (jsonOutputPath != null) {
                logger.info("Exporting to JSON at: {}", jsonOutputPath);
                runner.exportToJson(graph, jsonOutputPath);
            } else {
                logger.info("Exporting to JSON with default path");
                runner.exportToJson(graph);
            }

            // Export to Neo4j if requested
            if (exportToNeo4j) {
                if (neo4jPassword == null) {
                    logger.error("Neo4j password is required for Neo4j export");
                    return 1;
                }
                logger.info("Exporting to Neo4j at: {}", neo4jUri);
                runner.exportToNeo4j(graph, neo4jUri, neo4jUsername, neo4jPassword);
            }

            logger.info("Analysis complete!");
            return 0;
        } catch (AnalysisException e) {
            logger.error("Analysis failed: {}", e.getMessage(), e);
            return 1;
        }
    }
}