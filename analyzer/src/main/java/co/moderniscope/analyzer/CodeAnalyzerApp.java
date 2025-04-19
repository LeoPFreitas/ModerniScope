package co.moderniscope.analyzer;


import co.moderniscope.analyzer.api.AnalyzerFactory;
import co.moderniscope.analyzer.api.CodeAnalyzer;
import co.moderniscope.analyzer.api.DependencyGraph;
import co.moderniscope.analyzer.impl.DefaultAnalyzerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Main application entry point for the code analyzer.
 */
public class CodeAnalyzerApp {

    public static void main(String[] args) {
        System.out.println("Code Analyzer for Modernization Risk Assessment");
        System.out.println("==============================================");

        String projectPath;
        if (args.length > 0) {
            projectPath = args[0];
        } else {
            System.out.println("Please enter the path to the project root:");
            Scanner scanner = new Scanner(System.in);
            projectPath = scanner.nextLine();
        }

        try {
            // Create analyzer using factory
            AnalyzerFactory factory = new DefaultAnalyzerFactory();
            CodeAnalyzer analyzer = factory.createAnalyzer();

            // Analyze the project
            Path projectRoot = Paths.get(projectPath);
            analyzer.analyzeProject(projectRoot);

            // Export the graph
            DependencyGraph graph = analyzer.getDependencyGraph();
            Path dotOutputPath = Paths.get("dependency-graph.dot");
            Path jsonOutputPath = Paths.get("dependency-graph.json");

            graph.exportToDot(dotOutputPath);
            System.out.println("Graph exported to DOT format at: " + dotOutputPath);

            graph.exportToJson(jsonOutputPath);
            System.out.println("Graph exported to JSON format at: " + jsonOutputPath);

            // Ask if user wants to export to Neo4j
            System.out.println("\nDo you want to export the graph to Neo4j? (y/n)");
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("y") || response.equals("yes")) {
                System.out.println("Enter Neo4j URI (default: neo4j://localhost:7687):");
                String uri = scanner.nextLine().trim();
                if (uri.isEmpty()) uri = "neo4j://localhost:7687";

                System.out.println("Enter Neo4j username (default: neo4j):");
                String username = scanner.nextLine().trim();
                if (username.isEmpty()) username = "neo4j";

                System.out.println("Enter Neo4j password:");
                String password = scanner.nextLine();

                // Use Neo4j exporter
                try (Neo4jDependencyExporter neo4jExporter = new Neo4jDependencyExporter(uri, username, password)) {
                    System.out.println("Exporting to Neo4j...");
                    neo4jExporter.exportGraph(graph);
                    System.out.println("Graph successfully exported to Neo4j!");
                } catch (Exception e) {
                    System.err.println("Error exporting to Neo4j: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("\nAnalysis complete!");

        } catch (IOException e) {
            System.err.println("Error during analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}