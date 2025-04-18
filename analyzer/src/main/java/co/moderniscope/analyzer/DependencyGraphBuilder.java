package co.moderniscope.analyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DependencyGraphBuilder {

    private final Map<String, Set<String>> graph = new HashMap<>();

    public void addDependency(String from, String to) {
        graph.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    public void printGraph() {
        System.out.println("Dependency Graph:");
        graph.forEach((from, tos) -> {
            for (String to : tos) {
                System.out.printf("%s --> %s%n", from, to);
            }
        });
    }
}
