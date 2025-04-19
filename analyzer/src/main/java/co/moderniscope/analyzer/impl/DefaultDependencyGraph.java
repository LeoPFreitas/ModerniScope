package co.moderniscope.analyzer.impl;

import co.moderniscope.analyzer.api.DependencyGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Default implementation of DependencyGraph.
 */
public class DefaultDependencyGraph implements DependencyGraph {
    // Structure: from -> (to -> relationshipTypes)
    private final Map<String, Map<String, Set<String>>> graph = new HashMap<>();

    @Override
    public void addDependency(String from, String to, String relationshipType) {
        if (from == null || to == null || relationshipType == null ||
                from.isEmpty() || to.isEmpty() || from.equals(to)) {
            return;
        }

        graph.computeIfAbsent(from, k -> new HashMap<>())
                .computeIfAbsent(to, k -> new HashSet<>())
                .add(relationshipType);
    }

    @Override
    public void exportToDot(Path outputPath) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("digraph G {");
        lines.add("  node [shape=box];");

        // Group nodes by package
        Map<String, Set<String>> packageToNodes = new HashMap<>();
        graph.keySet().forEach(node -> {
            String pkg = getPackage(node);
            packageToNodes.computeIfAbsent(pkg, k -> new HashSet<>()).add(node);
        });
        graph.values().forEach(dests ->
                dests.keySet().forEach(node -> {
                    String pkg = getPackage(node);
                    packageToNodes.computeIfAbsent(pkg, k -> new HashSet<>()).add(node);
                })
        );

        // Create subgraphs for packages
        packageToNodes.forEach((pkg, nodes) -> {
            if (!pkg.isEmpty()) {
                lines.add("  subgraph \"cluster_" + pkg + "\" {");
                lines.add("    label = \"" + pkg + "\";");
                lines.add("    color = lightgrey;");

                nodes.forEach(node ->
                        lines.add("    \"" + node + "\" [label=\"" + getSimpleName(node) + "\"];")
                );

                lines.add("  }");
            }
        });

        // Add edges with relationship types
        graph.forEach((from, destinations) -> {
            destinations.forEach((to, relationships) -> {
                String relationshipStr = String.join(",", relationships);
                lines.add(String.format("  \"%s\" -> \"%s\" [label=\"%s\"];", from, to, relationshipStr));
            });
        });

        lines.add("}");
        Files.write(outputPath, lines);
    }

    @Override
    public void exportToJson(Path outputPath) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"nodes\": [\n");

        // Collect unique nodes
        Set<String> allNodes = new HashSet<>();
        graph.keySet().forEach(allNodes::add);
        graph.values().forEach(dests -> allNodes.addAll(dests.keySet()));

        // Write nodes
        Iterator<String> nodeIt = allNodes.iterator();
        while (nodeIt.hasNext()) {
            String node = nodeIt.next();
            json.append("    { \"id\": \"").append(node).append("\", ");
            json.append("\"package\": \"").append(getPackage(node)).append("\", ");
            json.append("\"name\": \"").append(getSimpleName(node)).append("\" }");
            if (nodeIt.hasNext()) json.append(",");
            json.append("\n");
        }

        json.append("  ],\n");
        json.append("  \"edges\": [\n");

        // Write edges
        boolean firstEdge = true;
        for (Map.Entry<String, Map<String, Set<String>>> fromEntry : graph.entrySet()) {
            String from = fromEntry.getKey();

            for (Map.Entry<String, Set<String>> toEntry : fromEntry.getValue().entrySet()) {
                String to = toEntry.getKey();
                Set<String> relationships = toEntry.getValue();

                for (String relationship : relationships) {
                    if (!firstEdge) json.append(",\n");
                    json.append("    { \"source\": \"").append(from).append("\", ");
                    json.append("\"target\": \"").append(to).append("\", ");
                    json.append("\"type\": \"").append(relationship).append("\" }");
                    firstEdge = false;
                }
            }
        }

        json.append("\n  ]\n}");

        Files.write(outputPath, Collections.singleton(json.toString()));
    }

    @Override
    public Map<String, Map<String, Set<String>>> getRawGraph() {
        return Collections.unmodifiableMap(graph);
    }

    private String getPackage(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? fullyQualifiedName.substring(0, lastDot) : "";
    }

    private String getSimpleName(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
    }
}