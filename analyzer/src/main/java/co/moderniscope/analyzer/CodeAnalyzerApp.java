package co.moderniscope.analyzer;

import co.moderniscope.analyzer.api.AnalysisException;
import co.moderniscope.analyzer.api.AnalysisRunner;
import co.moderniscope.analyzer.api.DependencyGraph;
import co.moderniscope.analyzer.impl.DefaultAnalysisRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Main application entry point for the code analyzer.
 */
public class CodeAnalyzerApp {
    private static final Logger logger = LoggerFactory.getLogger(CodeAnalyzerApp.class);

    public static void main(String[] args) {
        logger.info("Code Analyzer for Modernization Risk Assessment");
        logger.info("==============================================");

        try {
            String projectPath;
            if (args.length > 0) {
                projectPath = args[0];
            } else {
                System.out.println("Please enter the path to the project root:");
                Scanner scanner = new Scanner(System.in);
                projectPath = scanner.nextLine();
            }

            AnalysisRunner runner = new DefaultAnalysisRunner();
            DependencyGraph graph = runner.analyzeProject(Paths.get(projectPath));

            // Export to DOT and JSON using default paths
            runner.exportToDot(graph);
            runner.exportToJson(graph);

            if (promptForNeo4jExport()) {
                String uri = promptWithDefault("Enter Neo4j URI", "neo4j://localhost:7687");
                String username = promptWithDefault("Enter Neo4j username", "neo4j");
                String password = promptForPassword("Enter Neo4j password");

                runner.exportToNeo4j(graph, uri, username, password);
            }

            logger.info("Analysis complete!");
        } catch (AnalysisException e) {
            logger.error("Analysis failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private static boolean promptForNeo4jExport() {
        System.out.println("\nDo you want to export the graph to Neo4j? (y/n)");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    private static String promptWithDefault(String prompt, String defaultValue) {
        System.out.println(prompt + " (default: " + defaultValue + "):");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    private static String promptForPassword(String prompt) {
        System.out.println(prompt + ":");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}