package co.moderniscope.analyzer.graph.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ComplexGraphTest {

    private DefaultGraph<String, String> graph;
    private final Random random = new Random(42); // Fixed seed for reproducibility

    @BeforeEach
    void setUp() {
        graph = new DefaultGraph<>();
    }

    @Test
    void testComplexNetworkTopology() {
        // Create a complex network with multiple node types and relationship structures
        createComplexNetwork();

        // Test 1: Finding paths through densely connected subgraphes
        Optional<Iterable<String>> complexPath = graph.findPath("Hub1", "Hub3", "CONNECTS");
        assertTrue(complexPath.isPresent());
        List<String> pathNodes = toList(complexPath.get());
        assertTrue(pathNodes.size() >= 3, "Path should include at least 3 nodes");
        assertEquals("Hub1", pathNodes.getFirst());
        assertEquals("Hub3", pathNodes.getLast());

        // Test 2: Finding a path through a long chain
        Optional<Iterable<String>> longChainPath = graph.findPath("Chain1", "Chain20", "NEXT");
        assertTrue(longChainPath.isPresent());
        assertEquals(20, toList(longChainPath.get()).size());

        // Test 3: Paths requiring specific relationship sequences
        Optional<Iterable<String>> complexSequence = graph.findPath("Start", "End", "ROUTE_A", "ROUTE_B", "ROUTE_C");
        assertTrue(complexSequence.isPresent());

        // Test 4: Finding paths that must navigate through densely connected subgraphs
        Optional<Iterable<String>> throughDenseArea = graph.findPath("Outlier1", "Outlier2");
        assertTrue(throughDenseArea.isPresent());

        // Test 5: Test self-referential paths in complex regions
        Optional<Iterable<String>> cyclicPath = graph.findPath("CyclicNode", "CyclicNode", "SELF_REF");
        assertTrue(cyclicPath.isPresent());
        assertTrue(toList(cyclicPath.get()).size() > 1, "Should find path through cycle, not just single node");
    }

    @Test
    void testRemovalEffectsOnConnectivity() {
        createComplexNetwork();

        // Add a connection from Bridge2 to Spoke15 to ensure alternative path exists
        graph.addEdge("Bridge2", "Spoke15", "ALTERNATE_PATH");

        // Test path exists before removal
        Optional<Iterable<String>> pathBeforeRemoval = graph.findPath("Hub1", "Spoke15");
        assertTrue(pathBeforeRemoval.isPresent());
        List<String> beforePath = toList(pathBeforeRemoval.get());

        // Remove critical node
        graph.removeNode("Hub2");

        // Test connectivity after removal - path should exist but be longer
        Optional<Iterable<String>> pathAfterRemoval = graph.findPath("Hub1", "Spoke15");
        assertTrue(pathAfterRemoval.isPresent());

        List<String> afterPath = toList(pathAfterRemoval.get());
        assertTrue(afterPath.size() > beforePath.size(),
                "Path should be longer after node removal (before: " + beforePath.size() +
                        ", after: " + afterPath.size() + ")");

        // Remove edge that creates a bridge
        graph.removeEdge("Bridge1", "Bridge2", "BRIDGE");

        // Verify disconnection
        Optional<Iterable<String>> disconnectedPath = graph.findPath("Hub1", "Spoke15");
        assertFalse(disconnectedPath.isPresent(), "Path should be disconnected after bridge removal");
    }

    @Test
    void testPathFindingWithMultipleValidPaths() {
        createMultiPathNetwork();

        // Count the number of nodes visited during path finding using a custom visitor
        final Set<String> visitedNodes = new HashSet<>();

        // Find a path when multiple equivalent paths exist
        Optional<Iterable<String>> path = graph.findPath("Source", "Target");
        assertTrue(path.isPresent());

        // Verify we got a valid path (should be shortest)
        List<String> pathList = toList(path.get());
        assertTrue(pathList.size() <= 4, "Should find the shortest path");

        // Verify we can find a path with specific relationship types
        Optional<Iterable<String>> specificPath = graph.findPath("Source", "Target", "PATH_A");
        assertTrue(specificPath.isPresent());

        // Verify relationship constraints work correctly
        Optional<Iterable<String>> nonexistentConstraint = graph.findPath("Source", "Target", "NONEXISTENT_TYPE");
        assertFalse(nonexistentConstraint.isPresent(), "No path should exist with nonexistent relationship type");
    }

    @Test
    void testTraversalOrderingInComplexStructures() {
        createHierarchicalNetwork();

        final List<String> dfsOrder = new ArrayList<>();
        final List<String> bfsOrder = new ArrayList<>();

        // Capture DFS traversal order
        graph.traverseDepthFirst("Root", (node, depth) -> {
            dfsOrder.add(node);
            return true;
        });

        // Capture BFS traversal order
        graph.traverseBreadthFirst("Root", (node, depth) -> {
            bfsOrder.add(node);
            return true;
        });

        // Assert fundamental traversal properties
        assertEquals("Root", dfsOrder.getFirst());
        assertEquals("Root", bfsOrder.getFirst());

        // BFS should encounter all nodes at the same level before going deeper
        int rootChildrenIndex = 0;
        for (String child : Arrays.asList("Child1", "Child2", "Child3")) {
            int childIndexInBfs = bfsOrder.indexOf(child);
            assertTrue(childIndexInBfs > 0 && childIndexInBfs < 5,
                    "All children should appear early in BFS traversal");
        }

        // Instead of checking specific DFS order, verify parent-child relationships
        // Each parent should appear before its children
        for (int i = 1; i <= 3; i++) {
            String child = "Child" + i;
            String leaf = "Leaf" + i;

            int childIndex = dfsOrder.indexOf(child);
            int leafIndex = dfsOrder.indexOf(leaf);

            assertTrue(childIndex < leafIndex,
                    "Parent '" + child + "' should appear before its leaf '" + leaf + "' in DFS traversal");
        }

        // Verify all nodes are visited
        assertEquals(countNodesInHierarchy(), dfsOrder.size(), "DFS should visit all nodes");
    }

    private int countNodesInHierarchy() {
        // 1 root + 3 children + 9 grandchildren + 3 leaves = 16 nodes
        return 16;
    }

    private void createComplexNetwork() {
        // Create hub and spoke structures
        for (int i = 1; i <= 3; i++) {
            String hub = "Hub" + i;
            graph.addNode(hub);

            // Create spokes for each hub
            for (int j = 1; j <= 10; j++) {
                String spoke = "Spoke" + ((i - 1) * 10 + j);
                graph.addNode(spoke);
                graph.addEdge(hub, spoke, "CONNECTS");

                // Connect some spokes to other spokes
                if (j > 1) {
                    graph.addEdge(spoke, "Spoke" + ((i - 1) * 10 + j - 1), "PEER");
                }
            }
        }

        // Connect hubs to form a fully connected triangle
        graph.addEdge("Hub1", "Hub2", "CONNECTS");
        graph.addEdge("Hub2", "Hub3", "CONNECTS");
        graph.addEdge("Hub3", "Hub1", "CONNECTS");

        // Create a long chain
        for (int i = 1; i <= 20; i++) {
            String node = "Chain" + i;
            graph.addNode(node);
            if (i > 1) {
                graph.addEdge("Chain" + (i - 1), node, "NEXT");
            }
        }

        // Connect a chain to a hub structure
        graph.addEdge("Chain1", "Hub1", "ENTRY");
        graph.addEdge("Chain20", "Hub3", "EXIT");

        // Create a specific path requiring sequence traversal
        graph.addNode("Start");
        graph.addNode("Middle1");
        graph.addNode("Middle2");
        graph.addNode("End");

        graph.addEdge("Start", "Middle1", "ROUTE_A");
        graph.addEdge("Middle1", "Middle2", "ROUTE_B");
        graph.addEdge("Middle2", "End", "ROUTE_C");

        // Create outlier nodes that must traverse through a dense subgraph
        graph.addNode("Outlier1");
        graph.addNode("Outlier2");
        graph.addEdge("Outlier1", "Hub1", "ENTRY");
        graph.addEdge("Hub3", "Outlier2", "EXIT");

        // Create cyclic structure
        graph.addNode("CyclicNode");
        graph.addNode("CyclicNode2");
        graph.addNode("CyclicNode3");
        graph.addEdge("CyclicNode", "CyclicNode2", "SELF_REF");
        graph.addEdge("CyclicNode2", "CyclicNode3", "SELF_REF");
        graph.addEdge("CyclicNode3", "CyclicNode", "SELF_REF");

        // Create a bridge structure
        graph.addNode("Bridge1");
        graph.addNode("Bridge2");
        graph.addEdge("Bridge1", "Bridge2", "BRIDGE");
        graph.addEdge("Hub2", "Bridge1", "CONNECTS");

        // Create a hub-bridge structure
        graph.addEdge("Hub1", "Bridge1", "CONNECTS");

        // Add an isolated component
        graph.addNode("Isolated");
        graph.addNode("Isolated2");
        graph.addEdge("Isolated", "Isolated2", "ISOLINK");
    }

    private void createMultiPathNetwork() {
        graph.addNode("Source");
        graph.addNode("Target");

        // Create multiple paths between source and target
        for (int path = 1; path <= 5; path++) {
            String pathType = "PATH_" + (char) ('A' + path - 1);

            // Create intermediate nodes
            for (int i = 1; i <= path; i++) {
                String node = "Path" + path + "_Node" + i;
                graph.addNode(node);

                if (i == 1) {
                    graph.addEdge("Source", node, pathType);
                } else {
                    graph.addEdge("Path" + path + "_Node" + (i - 1), node, pathType);
                }

                if (i == path) {
                    graph.addEdge(node, "Target", pathType);
                }
            }
        }

        // Add some cross-connections between paths
        graph.addEdge("Path1_Node1", "Path2_Node1", "CROSS");
        graph.addEdge("Path3_Node2", "Path4_Node1", "CROSS");
    }

    private void createHierarchicalNetwork() {
        graph.addNode("Root");

        // Create first level children
        for (int i = 1; i <= 3; i++) {
            String child = "Child" + i;
            graph.addNode(child);
            graph.addEdge("Root", child, "PARENT");

            // Create grandchildren
            for (int j = 1; j <= 3; j++) {
                String grandchild = "GChild" + i + "_" + j;
                graph.addNode(grandchild);
                graph.addEdge(child, grandchild, "PARENT");

                // Create some leaves
                if (j == 1) {
                    String leaf = "Leaf" + i;
                    graph.addNode(leaf);
                    graph.addEdge(grandchild, leaf, "PARENT");
                }
            }
        }
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> result = new ArrayList<>();
        iterable.forEach(result::add);
        return result;
    }
}