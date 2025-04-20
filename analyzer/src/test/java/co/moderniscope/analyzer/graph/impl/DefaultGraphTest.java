package co.moderniscope.analyzer.graph.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DefaultGraphTest {

    private DefaultGraph<String, String> graph;

    @BeforeEach
    void setUp() {
        graph = new DefaultGraph<>();
    }

    @Test
    void testAddNode() {
        // Add a simple node
        assertTrue(graph.addNode("A"));

        // Adding the same node should return false
        assertFalse(graph.addNode("A"));

        // Check node exists in graph
        Set<String> nodes = graph.getNodes();
        assertEquals(1, nodes.size());
        assertTrue(nodes.contains("A"));
    }

    @Test
    void testAddNodeWithPropertiesAndLabels() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "Node A");
        properties.put("value", 42);

        // Add a node with properties and labels
        assertTrue(graph.addNode("A", properties, "Person", "Employee"));

        // Verify properties were set
        Map<String, Object> retrievedProps = graph.getNodeProperties("A");
        assertEquals("Node A", retrievedProps.get("name"));
        assertEquals(42, retrievedProps.get("value"));

        // Verify labels
        Set<String> nodesByLabel = graph.getNodesByLabel("Person");
        assertTrue(nodesByLabel.contains("A"));
        nodesByLabel = graph.getNodesByLabel("Employee");
        assertTrue(nodesByLabel.contains("A"));

        // Adding the same node with different properties should return false but update the node
        Map<String, Object> newProperties = new HashMap<>();
        newProperties.put("name", "Updated Node A");

        assertFalse(graph.addNode("A", newProperties, "Manager"));

        // Verify properties were updated
        retrievedProps = graph.getNodeProperties("A");
        assertEquals("Updated Node A", retrievedProps.get("name"));
        assertEquals(42, retrievedProps.get("value"));

        // Verify a new label was added
        nodesByLabel = graph.getNodesByLabel("Manager");
        assertTrue(nodesByLabel.contains("A"));
    }

    @Test
    void testRemoveNodeBasic() {
        // Add nodes
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C", null, "TestLabel");

        // Remove node
        assertTrue(graph.removeNode("B"));

        // Verify node was removed
        assertFalse(graph.getNodes().contains("B"));
        assertEquals(2, graph.getNodes().size());

        // Remove node with label
        assertTrue(graph.removeNode("C"));

        // Verify label index was updated
        assertTrue(graph.getNodesByLabel("TestLabel").isEmpty());

        // Try to remove non-existent node
        assertFalse(graph.removeNode("Z"));
    }

    @Test
    void testNodeLabels() {
        // Add nodes with labels
        graph.addNode("A", null, "Person", "Employee");
        graph.addNode("B", null, "Person");
        graph.addNode("C", null, "Employee");

        // Verify label indexes
        Set<String> people = graph.getNodesByLabel("Person");
        assertEquals(2, people.size());
        assertTrue(people.contains("A"));
        assertTrue(people.contains("B"));

        Set<String> employees = graph.getNodesByLabel("Employee");
        assertEquals(2, employees.size());
        assertTrue(employees.contains("A"));
        assertTrue(employees.contains("C"));

        // Verify empty result for non-existent label
        assertTrue(graph.getNodesByLabel("NonExistentLabel").isEmpty());
    }

    @Test
    void testAddEdge() {
        // Add nodes and a simple edge
        graph.addNode("A");
        graph.addNode("B");
        assertTrue(graph.addEdge("A", "B", "CONNECTS_TO"));

        // Get outgoing edges
        Map<String, Set<String>> outgoingEdges = graph.getOutgoingEdges("A");
        assertEquals(1, outgoingEdges.size());
        assertTrue(outgoingEdges.containsKey("B"));
        assertTrue(outgoingEdges.get("B").contains("CONNECTS_TO"));

        // Get incoming edges
        Map<String, Set<String>> incomingEdges = graph.getIncomingEdges("B");
        assertEquals(1, incomingEdges.size());
        assertTrue(incomingEdges.containsKey("A"));
        assertTrue(incomingEdges.get("A").contains("CONNECTS_TO"));
    }

    @Test
    void testAddEdgeWithProperties() {
        // Add nodes
        graph.addNode("A");
        graph.addNode("B");

        // Add edge with properties
        Map<String, Object> edgeProps = new HashMap<>();
        edgeProps.put("weight", 5);
        edgeProps.put("label", "main connection");

        assertTrue(graph.addEdge("A", "B", "CONNECTS_TO", edgeProps));

        // Verify edge properties
        Map<String, Object> retrievedProps = graph.getEdgeProperties("A", "B", "CONNECTS_TO");
        assertEquals(2, retrievedProps.size());
        assertEquals(5, retrievedProps.get("weight"));
        assertEquals("main connection", retrievedProps.get("label"));
    }

    @Test
    void testAddEdgeAutoCreatesNodes() {
        // Add an edge between non-existent nodes
        assertTrue(graph.addEdge("C", "D", "LINKS_TO"));

        // Verify nodes were created
        Set<String> nodes = graph.getNodes();
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains("C"));
        assertTrue(nodes.contains("D"));

        // Verify edge was created
        Map<String, Set<String>> outgoingEdges = graph.getOutgoingEdges("C");
        assertTrue(outgoingEdges.containsKey("D"));
        assertTrue(outgoingEdges.get("D").contains("LINKS_TO"));
    }

    @Test
    void testAddMultipleEdges() {
        // Add multiple edges between nodes
        graph.addNode("X");
        graph.addNode("Y");

        assertTrue(graph.addEdge("X", "Y", "RELATION_1"));
        assertTrue(graph.addEdge("X", "Y", "RELATION_2"));
        assertTrue(graph.addEdge("Y", "X", "RELATION_3"));

        // Verify all edges
        Map<String, Set<String>> xToY = graph.getOutgoingEdges("X");
        assertEquals(1, xToY.size());
        assertEquals(2, xToY.get("Y").size());
        assertTrue(xToY.get("Y").contains("RELATION_1"));
        assertTrue(xToY.get("Y").contains("RELATION_2"));

        Map<String, Set<String>> yToX = graph.getOutgoingEdges("Y");
        assertEquals(1, yToX.size());
        assertEquals(1, yToX.get("X").size());
        assertTrue(yToX.get("X").contains("RELATION_3"));
    }

    @Test
    void testGetEdgesByRelationshipType() {
        // Setup
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");

        graph.addEdge("A", "B", "FRIEND");
        graph.addEdge("A", "C", "FRIEND");
        graph.addEdge("A", "B", "COLLEAGUE");

        // Test filtering by relationship type
        Map<String, Set<String>> friendEdges = graph.getOutgoingEdges("A", "FRIEND");
        assertEquals(2, friendEdges.size());
        assertTrue(friendEdges.containsKey("B"));
        assertTrue(friendEdges.containsKey("C"));

        Map<String, Set<String>> colleagueEdges = graph.getOutgoingEdges("A", "COLLEAGUE");
        assertEquals(1, colleagueEdges.size());
        assertTrue(colleagueEdges.containsKey("B"));
        assertFalse(colleagueEdges.containsKey("C"));
    }

    @Test
    void testGetNodes() {
        // Empty graph should return an empty set
        Set<String> emptyNodes = graph.getNodes();
        assertTrue(emptyNodes.isEmpty());

        // Add several nodes
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");

        // Verify all nodes are returned
        Set<String> nodes = graph.getNodes();
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains("A"));
        assertTrue(nodes.contains("B"));
        assertTrue(nodes.contains("C"));

        // Remove a node
        graph.removeNode("B");

        // Verify updated set
        nodes = graph.getNodes();
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains("A"));
        assertTrue(nodes.contains("C"));
        assertFalse(nodes.contains("B"));

        // Verify the returned set is a copy (can't modify the graph)
        Set<String> nodesCopy = graph.getNodes();
        nodesCopy.remove("A");
        assertEquals(1, nodesCopy.size());

        // The Original graph should be unchanged
        assertEquals(2, graph.getNodes().size());
        assertTrue(graph.getNodes().contains("A"));
    }

    @Test
    void testGetNodesByLabel() {
        // Add nodes with different label combinations
        graph.addNode("A", null, "Label1", "Label2");
        graph.addNode("B", null, "Label1");
        graph.addNode("C", null, "Label2", "Label3");
        graph.addNode("D", null, "Label3");
        graph.addNode("E");  // No labels

        // Test retrieving by Label1
        Set<String> nodesWithLabel1 = graph.getNodesByLabel("Label1");
        assertEquals(2, nodesWithLabel1.size());
        assertTrue(nodesWithLabel1.contains("A"));
        assertTrue(nodesWithLabel1.contains("B"));

        // Test retrieving by Label2
        Set<String> nodesWithLabel2 = graph.getNodesByLabel("Label2");
        assertEquals(2, nodesWithLabel2.size());
        assertTrue(nodesWithLabel2.contains("A"));
        assertTrue(nodesWithLabel2.contains("C"));

        // Test retrieving by Label3
        Set<String> nodesWithLabel3 = graph.getNodesByLabel("Label3");
        assertEquals(2, nodesWithLabel3.size());
        assertTrue(nodesWithLabel3.contains("C"));
        assertTrue(nodesWithLabel3.contains("D"));

        // Test retrieving by non-existent label
        Set<String> nodesWithNonExistentLabel = graph.getNodesByLabel("NonExistentLabel");
        assertTrue(nodesWithNonExistentLabel.isEmpty());

        // Verify the returned set is a copy (can't modify the graph)
        Set<String> labelCopy = graph.getNodesByLabel("Label1");
        labelCopy.remove("A");
        assertEquals(1, labelCopy.size());

        // The Original index should be unchanged
        assertEquals(2, graph.getNodesByLabel("Label1").size());
    }

    @Test
    void testGetNodeProperties() {
        // Test with a non-existent node
        Map<String, Object> nonExistentProps = graph.getNodeProperties("NonExistent");
        assertTrue(nonExistentProps.isEmpty());

        // Create a node with no properties
        graph.addNode("A");
        Map<String, Object> emptyProps = graph.getNodeProperties("A");
        assertTrue(emptyProps.isEmpty());

        // Create a node with properties
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "Node B");
        properties.put("age", 30);
        properties.put("active", true);
        graph.addNode("B", properties);

        // Retrieve and verify properties
        Map<String, Object> retrievedProps = graph.getNodeProperties("B");
        assertEquals(3, retrievedProps.size());
        assertEquals("Node B", retrievedProps.get("name"));
        assertEquals(30, retrievedProps.get("age"));
        assertEquals(true, retrievedProps.get("active"));

        // Verify a returned map is a copy (can't modify the node)
        retrievedProps.put("modified", "value");
        assertEquals(4, retrievedProps.size());

        // Original node properties should be unchanged
        Map<String, Object> reRetrievedProps = graph.getNodeProperties("B");
        assertEquals(3, reRetrievedProps.size());
        assertNull(reRetrievedProps.get("modified"));

        // Update properties
        Map<String, Object> updatedProps = new HashMap<>();
        updatedProps.put("name", "Updated B");
        updatedProps.put("status", "premium");
        graph.addNode("B", updatedProps);

        // Verify properties were updated/merged
        Map<String, Object> afterUpdateProps = graph.getNodeProperties("B");
        assertEquals(4, afterUpdateProps.size());
        assertEquals("Updated B", afterUpdateProps.get("name"));
        assertEquals(30, afterUpdateProps.get("age"));
        assertEquals(true, afterUpdateProps.get("active"));
        assertEquals("premium", afterUpdateProps.get("status"));
    }

    @Test
    void testGetOutgoingEdges() {
        // Test with non-existent node
        Map<String, Set<String>> nonExistentEdges = graph.getOutgoingEdges("NonExistent");
        assertTrue(nonExistentEdges.isEmpty());

        // Create test graph structure
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");

        graph.addEdge("A", "B", "KNOWS");
        graph.addEdge("A", "B", "WORKS_WITH");
        graph.addEdge("A", "C", "FOLLOWS");
        graph.addEdge("C", "A", "FOLLOWS");

        // Get all outgoing edges from A
        Map<String, Set<String>> aEdges = graph.getOutgoingEdges("A");
        assertEquals(2, aEdges.size());

        // Check B's incoming relationships from A
        Set<String> aToBRelationships = aEdges.get("B");
        assertEquals(2, aToBRelationships.size());
        assertTrue(aToBRelationships.contains("KNOWS"));
        assertTrue(aToBRelationships.contains("WORKS_WITH"));

        // Check C's incoming relationships from A
        Set<String> aToCRelationships = aEdges.get("C");
        assertEquals(1, aToCRelationships.size());
        assertTrue(aToCRelationships.contains("FOLLOWS"));

        // Verify the returned map is a copy
        aEdges.remove("B");
        assertEquals(1, aEdges.size());
        assertEquals(2, graph.getOutgoingEdges("A").size());
    }

    @Test
    void testGetOutgoingEdgesByRelationshipType() {
        // Create a test graph structure
        graph.addNode("X");
        graph.addNode("Y");
        graph.addNode("Z");

        graph.addEdge("X", "Y", "FRIEND");
        graph.addEdge("X", "Z", "FRIEND");
        graph.addEdge("X", "Y", "COLLEAGUE");

        // Test filtering by relationship type (FRIEND)
        Map<String, Set<String>> friendEdges = graph.getOutgoingEdges("X", "FRIEND");
        assertEquals(2, friendEdges.size());
        assertTrue(friendEdges.containsKey("Y"));
        assertTrue(friendEdges.containsKey("Z"));

        // Test filtering by relationship type (COLLEAGUE)
        Map<String, Set<String>> colleagueEdges = graph.getOutgoingEdges("X", "COLLEAGUE");
        assertEquals(1, colleagueEdges.size());
        assertTrue(colleagueEdges.containsKey("Y"));
        assertFalse(colleagueEdges.containsKey("Z"));

        // Test with a non-existent relationship type
        Map<String, Set<String>> nonExistentRelationships = graph.getOutgoingEdges("X", "FAMILY");
        assertTrue(nonExistentRelationships.isEmpty());

        // Test with non-existent source node
        Map<String, Set<String>> nonExistentNodeEdges = graph.getOutgoingEdges("NonExistent", "FRIEND");
        assertTrue(nonExistentNodeEdges.isEmpty());
    }

    @Test
    void testGetIncomingEdges() {
        // Test with non-existent node
        Map<String, Set<String>> nonExistentEdges = graph.getIncomingEdges("NonExistent");
        assertTrue(nonExistentEdges.isEmpty());

        // Create test graph structure
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");

        graph.addEdge("A", "C", "KNOWS");
        graph.addEdge("B", "C", "KNOWS");
        graph.addEdge("B", "C", "WORKS_WITH");
        graph.addEdge("C", "A", "LIKES");

        // Get all incoming edges to C
        Map<String, Set<String>> cEdges = graph.getIncomingEdges("C");
        assertEquals(2, cEdges.size());

        // Check A's outgoing relationships to C
        Set<String> aToCRelationships = cEdges.get("A");
        assertEquals(1, aToCRelationships.size());
        assertTrue(aToCRelationships.contains("KNOWS"));

        // Check B's outgoing relationships to C
        Set<String> bToCRelationships = cEdges.get("B");
        assertEquals(2, bToCRelationships.size());
        assertTrue(bToCRelationships.contains("KNOWS"));
        assertTrue(bToCRelationships.contains("WORKS_WITH"));

        // Verify the returned map is a copy
        cEdges.remove("A");
        assertEquals(1, cEdges.size());
        assertEquals(2, graph.getIncomingEdges("C").size());
    }

    @Test
    void testGetIncomingEdgesByRelationshipType() {
        // Create a test graph structure
        graph.addNode("X");
        graph.addNode("Y");
        graph.addNode("Z");

        graph.addEdge("X", "Z", "FRIEND");
        graph.addEdge("Y", "Z", "FRIEND");
        graph.addEdge("Y", "Z", "COLLEAGUE");

        // Test filtering by relationship type (FRIEND)
        Map<String, Set<String>> friendEdges = graph.getIncomingEdges("Z", "FRIEND");
        assertEquals(2, friendEdges.size());
        assertTrue(friendEdges.containsKey("X"));
        assertTrue(friendEdges.containsKey("Y"));

        // Test filtering by relationship type (COLLEAGUE)
        Map<String, Set<String>> colleagueEdges = graph.getIncomingEdges("Z", "COLLEAGUE");
        assertEquals(1, colleagueEdges.size());
        assertTrue(colleagueEdges.containsKey("Y"));
        assertFalse(colleagueEdges.containsKey("X"));

        // Test with a non-existent relationship type
        Map<String, Set<String>> nonExistentRelationships = graph.getIncomingEdges("Z", "FAMILY");
        assertTrue(nonExistentRelationships.isEmpty());

        // Test with a non-existent target node
        Map<String, Set<String>> nonExistentNodeEdges = graph.getIncomingEdges("NonExistent", "FRIEND");
        assertTrue(nonExistentNodeEdges.isEmpty());
    }

    @Test
    void testGetEdgeProperties() {
        // Test with non-existent source node
        Map<String, Object> nonExistentSourceProps = graph.getEdgeProperties("NonExistent", "B", "CONNECTS");
        assertTrue(nonExistentSourceProps.isEmpty());

        // Create nodes and add edges
        graph.addNode("A");
        graph.addNode("B");

        // Test with existing nodes but no edge
        Map<String, Object> nonExistentEdgeProps = graph.getEdgeProperties("A", "B", "CONNECTS");
        assertTrue(nonExistentEdgeProps.isEmpty());

        // Add edge with no properties
        graph.addEdge("A", "B", "CONNECTS");
        Map<String, Object> emptyProps = graph.getEdgeProperties("A", "B", "CONNECTS");
        assertTrue(emptyProps.isEmpty());

        // Add edge with properties
        Map<String, Object> properties = new HashMap<>();
        properties.put("weight", 10);
        properties.put("distance", 2.5);
        properties.put("active", true);
        graph.addEdge("A", "B", "RELATES", properties);

        // Verify properties for a specific edge type
        Map<String, Object> retrievedProps = graph.getEdgeProperties("A", "B", "RELATES");
        assertEquals(3, retrievedProps.size());
        assertEquals(10, retrievedProps.get("weight"));
        assertEquals(2.5, retrievedProps.get("distance"));
        assertEquals(true, retrievedProps.get("active"));

        // Verify properties are not mixed between edge types
        Map<String, Object> otherEdgeProps = graph.getEdgeProperties("A", "B", "CONNECTS");
        assertTrue(otherEdgeProps.isEmpty());

        // Verify the returned map is a copy
        retrievedProps.put("modified", "value");
        assertEquals(4, retrievedProps.size());

        // Original-edge properties should be unchanged
        Map<String, Object> reRetrievedProps = graph.getEdgeProperties("A", "B", "RELATES");
        assertEquals(3, reRetrievedProps.size());
        assertNull(reRetrievedProps.get("modified"));
    }

    @Test
    void testFindPath() {
        // Create a graph structure
        //    A --- B --- C
        //    |           |
        //    +--- D --- E
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");

        graph.addEdge("A", "B", "CONNECTS");
        graph.addEdge("B", "C", "CONNECTS");
        graph.addEdge("A", "D", "CONNECTS");
        graph.addEdge("D", "E", "CONNECTS");
        graph.addEdge("E", "C", "CONNECTS");

        // Test direct path
        Optional<Iterable<String>> path1 = graph.findPath("A", "B");
        assertTrue(path1.isPresent());
        List<String> path1List = toList(path1.get());
        assertEquals(Arrays.asList("A", "B"), path1List);

        // Test a longer path
        Optional<Iterable<String>> path2 = graph.findPath("A", "C");
        assertTrue(path2.isPresent());
        List<String> path2List = toList(path2.get());
        assertEquals(Arrays.asList("A", "B", "C"), path2List);

        // Test with no path
        graph.addNode("F");  // Isolated node
        Optional<Iterable<String>> path3 = graph.findPath("A", "F");
        assertFalse(path3.isPresent());

        // Test path to self
        Optional<Iterable<String>> path4 = graph.findPath("A", "A");
        assertTrue(path4.isPresent());
        List<String> path4List = toList(path4.get());
        assertEquals(Collections.singletonList("A"), path4List);

        // Test with non-existent nodes
        Optional<Iterable<String>> path5 = graph.findPath("A", "Z");
        assertFalse(path5.isPresent());
        Optional<Iterable<String>> path6 = graph.findPath("Z", "A");
        assertFalse(path6.isPresent());
    }

    @Test
    void testFindPathWithRelationshipTypes() {
        // Create a graph with different relationship types
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");

        graph.addEdge("A", "B", "FRIEND");
        graph.addEdge("B", "C", "COLLEAGUE");
        graph.addEdge("C", "D", "FAMILY");
        graph.addEdge("A", "D", "NEIGHBOR");

        // Test path with a specific relationship type
        Optional<Iterable<String>> path1 = graph.findPath("A", "D", "NEIGHBOR");
        assertTrue(path1.isPresent());
        List<String> path1List = toList(path1.get());
        assertEquals(Arrays.asList("A", "D"), path1List);

        // Test path with multiple allowed relationship types
        Optional<Iterable<String>> path2 = graph.findPath("A", "C", "FRIEND", "COLLEAGUE");
        assertTrue(path2.isPresent());
        List<String> path2List = toList(path2.get());
        assertEquals(Arrays.asList("A", "B", "C"), path2List);

        // Test where relationship type constraint prevents a path
        Optional<Iterable<String>> path3 = graph.findPath("A", "D", "FRIEND", "COLLEAGUE");
        assertFalse(path3.isPresent());

        // Complete path with all relationship types
        Optional<Iterable<String>> path4 = graph.findPath("A", "D", "FRIEND", "COLLEAGUE", "FAMILY");
        assertTrue(path4.isPresent());
        List<String> path4List = toList(path4.get());
        assertEquals(Arrays.asList("A", "B", "C", "D"), path4List);
    }

    // Helper method to convert Iterable to List for easier assertions
    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> result = new ArrayList<>();
        iterable.forEach(result::add);
        return result;
    }

    @Test
    void testTraverseDepthFirst() {
        // Create a graph structure
        //      A
        //     / \
        //    B   C
        //   / \   \
        //  D   E   F
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        graph.addNode("F");

        graph.addEdge("A", "B", "CONNECT");
        graph.addEdge("A", "C", "CONNECT");
        graph.addEdge("B", "D", "CONNECT");
        graph.addEdge("B", "E", "CONNECT");
        graph.addEdge("C", "F", "CONNECT");

        // Collect visited nodes and depths in order
        List<String> visitedNodes = new ArrayList<>();
        List<Integer> visitedDepths = new ArrayList<>();

        graph.traverseDepthFirst("A", (node, depth) -> {
            visitedNodes.add(node);
            visitedDepths.add(depth);
            return true;
        });

        // DFS should explore one branch fully before another
        // Expected order: A -> B -> D -> E -> C -> F
        assertEquals(6, visitedNodes.size());
        assertEquals("A", visitedNodes.getFirst());
        assertEquals(0, visitedDepths.getFirst());

        // Verify B is visited before C (DFS explores the first branch fully)
        assertTrue(visitedNodes.indexOf("B") < visitedNodes.indexOf("C"));

        // Verify D and E come after B but before C
        assertTrue(visitedNodes.indexOf("B") < visitedNodes.indexOf("D"));
        assertTrue(visitedNodes.indexOf("B") < visitedNodes.indexOf("E"));
        assertTrue(visitedNodes.indexOf("D") < visitedNodes.indexOf("C"));
        assertTrue(visitedNodes.indexOf("E") < visitedNodes.indexOf("C"));

        // Verify F comes after C
        assertTrue(visitedNodes.indexOf("C") < visitedNodes.indexOf("F"));

        // Verify depths are correct
        assertEquals(0, visitedDepths.get(visitedNodes.indexOf("A")));
        assertEquals(1, visitedDepths.get(visitedNodes.indexOf("B")));
        assertEquals(1, visitedDepths.get(visitedNodes.indexOf("C")));
        assertEquals(2, visitedDepths.get(visitedNodes.indexOf("D")));
        assertEquals(2, visitedDepths.get(visitedNodes.indexOf("E")));
        assertEquals(2, visitedDepths.get(visitedNodes.indexOf("F")));

        // Test early termination
        List<String> earlyTerminationNodes = new ArrayList<>();
        graph.traverseDepthFirst("A", (node, depth) -> {
            earlyTerminationNodes.add(node);
            return !node.equals("B"); // Stop once we find B
        });

        assertEquals(2, earlyTerminationNodes.size());
        assertEquals("A", earlyTerminationNodes.get(0));
        assertEquals("B", earlyTerminationNodes.get(1));

        // Test with a non-existent start node
        List<String> nonExistentStart = new ArrayList<>();
        graph.traverseDepthFirst("Z", (node, depth) -> {
            nonExistentStart.add(node);
            return true;
        });
        assertTrue(nonExistentStart.isEmpty());
    }

    @Test
    void testRemoveNode() {
        // Create a graph with some interconnected nodes
        graph.addNode("A", null, "Person");
        graph.addNode("B", null, "Person", "Employee");
        graph.addNode("C", null, "Company");

        // Add properties to a node
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "Node B");
        properties.put("age", 30);
        graph.addNode("B", properties);

        // Create edges between nodes
        graph.addEdge("A", "B", "KNOWS");
        graph.addEdge("B", "A", "WORKS_WITH");
        graph.addEdge("B", "C", "WORKS_AT");
        graph.addEdge("C", "B", "EMPLOYS");

        // Now remove node B, which has both incoming and outgoing edges
        assertTrue(graph.removeNode("B"));

        // Verify node B is removed
        assertFalse(graph.getNodes().contains("B"));
        assertEquals(2, graph.getNodes().size());

        // Verify B is removed from label indexes
        assertTrue(graph.getNodesByLabel("Employee").isEmpty());
        assertEquals(1, graph.getNodesByLabel("Person").size());
        assertTrue(graph.getNodesByLabel("Person").contains("A"));

        // Verify edges to/from B are removed
        assertTrue(graph.getOutgoingEdges("A").isEmpty());
        assertTrue(graph.getIncomingEdges("A").isEmpty());
        assertTrue(graph.getOutgoingEdges("C").isEmpty());
        assertTrue(graph.getIncomingEdges("C").isEmpty());

        // Try to remove a non-existent node
        assertFalse(graph.removeNode("Z"));

        // Remove node with no connections
        assertTrue(graph.removeNode("A"));
        assertEquals(1, graph.getNodes().size());
        assertTrue(graph.getNodesByLabel("Person").isEmpty());

        // Remove last node
        assertTrue(graph.removeNode("C"));
        assertTrue(graph.getNodes().isEmpty());
        assertTrue(graph.getNodesByLabel("Company").isEmpty());
    }

    @Test
    void testRemoveEdge() {
        // Create test graph structure
        graph.addNode("A");
        graph.addNode("B");

        // Add multiple edges of different types
        graph.addEdge("A", "B", "KNOWS");
        graph.addEdge("A", "B", "WORKS_WITH");

        // Remove one specific edge
        assertTrue(graph.removeEdge("A", "B", "KNOWS"));

        // Verify only the specific edge was removed
        Map<String, Set<String>> outgoingEdges = graph.getOutgoingEdges("A");
        assertTrue(outgoingEdges.containsKey("B"));
        assertEquals(1, outgoingEdges.get("B").size());
        assertTrue(outgoingEdges.get("B").contains("WORKS_WITH"));
        assertFalse(outgoingEdges.get("B").contains("KNOWS"));

        // Verify removing an edge that doesn't exist returns false
        assertFalse(graph.removeEdge("A", "B", "FAMILY"));

        // Verify removing from a non-existent node returns false
        assertFalse(graph.removeEdge("Z", "B", "WORKS_WITH"));

        // Verify removing to a non-existent node returns false
        assertFalse(graph.removeEdge("A", "Z", "WORKS_WITH"));

        // Remove the last edge
        assertTrue(graph.removeEdge("A", "B", "WORKS_WITH"));

        // Verify all edges are gone
        assertTrue(graph.getOutgoingEdges("A").isEmpty());
        assertTrue(graph.getIncomingEdges("B").isEmpty());
    }

    @Test
    void testRemoveEdges() {
        // Create test graph structure
        graph.addNode("A");
        graph.addNode("B");

        // Add multiple edges of the same type
        graph.addEdge("A", "B", "KNOWS");
        graph.addEdge("A", "B", "KNOWS");  // This won't add a duplicate in our implementation

        // Add edges of a different type
        graph.addEdge("A", "B", "FRIEND");
        graph.addEdge("A", "B", "COLLEAGUE");

        // Remove all edges of type KNOWS
        int removed = graph.removeEdges("A", "B", "KNOWS");
        assertEquals(1, removed);

        // Verify only KNOWS edges were removed
        Map<String, Set<String>> outgoingEdges = graph.getOutgoingEdges("A");
        assertEquals(1, outgoingEdges.size());
        assertEquals(2, outgoingEdges.get("B").size());
        assertFalse(outgoingEdges.get("B").contains("KNOWS"));
        assertTrue(outgoingEdges.get("B").contains("FRIEND"));
        assertTrue(outgoingEdges.get("B").contains("COLLEAGUE"));

        // Remove with non-existent relationship type
        removed = graph.removeEdges("A", "B", "NON_EXISTENT");
        assertEquals(0, removed);

        // Remove with non-existent source node
        removed = graph.removeEdges("X", "B", "FRIEND");
        assertEquals(0, removed);

        // Remove with non-existent target node
        removed = graph.removeEdges("A", "X", "FRIEND");
        assertEquals(0, removed);

        // Remove remaining edges
        removed = graph.removeEdges("A", "B", "FRIEND");
        assertEquals(1, removed);

        removed = graph.removeEdges("A", "B", "COLLEAGUE");
        assertEquals(1, removed);

        // Verify all edges are gone
        assertTrue(graph.getOutgoingEdges("A").isEmpty());
        assertTrue(graph.getIncomingEdges("B").isEmpty());
    }

    @Test
    void testRemoveNodeV2() {
        // Create a graph with interconnected nodes
        graph.addNode("A", null, "Person");
        graph.addNode("B", null, "Person", "Employee");
        graph.addNode("C", null, "Company");

        // Add properties to nodes
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "Node B");
        properties.put("age", 30);
        graph.addNode("B", properties);

        // Create edges between nodes
        graph.addEdge("A", "B", "KNOWS");
        graph.addEdge("B", "A", "WORKS_WITH");
        graph.addEdge("B", "C", "WORKS_AT");
        graph.addEdge("C", "B", "EMPLOYS");

        // Remove node B which has both incoming and outgoing edges
        assertTrue(graph.removeNode("B"));

        // Verify node B is removed
        assertFalse(graph.getNodes().contains("B"));
        assertEquals(2, graph.getNodes().size());

        // Verify B is removed from label indexes
        assertTrue(graph.getNodesByLabel("Employee").isEmpty());
        assertEquals(1, graph.getNodesByLabel("Person").size());
        assertTrue(graph.getNodesByLabel("Person").contains("A"));

        // Verify edges connected to B are removed
        assertTrue(graph.getOutgoingEdges("A").isEmpty());
        assertTrue(graph.getIncomingEdges("A").isEmpty());
        assertTrue(graph.getOutgoingEdges("C").isEmpty());
        assertTrue(graph.getIncomingEdges("C").isEmpty());

        // Try to remove non-existent node
        assertFalse(graph.removeNode("Z"));

        // Remove a node with no connections
        assertTrue(graph.removeNode("A"));
        assertEquals(1, graph.getNodes().size());
        assertTrue(graph.getNodesByLabel("Person").isEmpty());

        // Remove last node
        assertTrue(graph.removeNode("C"));
        assertTrue(graph.getNodes().isEmpty());
        assertTrue(graph.getNodesByLabel("Company").isEmpty());
    }

    @Test
    void testClear() {
        // Create a non-empty graph with nodes, labels, edges and properties
        graph.addNode("A", Map.of("key1", "value1"), "Person");
        graph.addNode("B", Map.of("key2", "value2"), "Company");
        graph.addNode("C", Map.of("key3", "value3"), "Person", "Employee");

        graph.addEdge("A", "B", "KNOWS", Map.of("since", 2020));
        graph.addEdge("B", "C", "EMPLOYS", Map.of("role", "Developer"));
        graph.addEdge("C", "A", "WORKS_WITH");

        // Verify graph is populated
        assertEquals(3, graph.getNodes().size());
        assertEquals(2, graph.getNodesByLabel("Person").size());
        assertEquals(1, graph.getNodesByLabel("Company").size());
        assertEquals(1, graph.getNodesByLabel("Employee").size());
        assertFalse(graph.getOutgoingEdges("A").isEmpty());
        assertFalse(graph.getOutgoingEdges("B").isEmpty());
        assertFalse(graph.getOutgoingEdges("C").isEmpty());

        // Clear the graph
        graph.clear();

        // Verify the graph is empty
        assertTrue(graph.getNodes().isEmpty());
        assertTrue(graph.getNodesByLabel("Person").isEmpty());
        assertTrue(graph.getNodesByLabel("Company").isEmpty());
        assertTrue(graph.getNodesByLabel("Employee").isEmpty());

        // Verify outgoing edges are empty
        assertTrue(graph.getOutgoingEdges("A").isEmpty());
        assertTrue(graph.getOutgoingEdges("B").isEmpty());
        assertTrue(graph.getOutgoingEdges("C").isEmpty());

        // Verify incoming edges are empty
        assertTrue(graph.getIncomingEdges("A").isEmpty());
        assertTrue(graph.getIncomingEdges("B").isEmpty());
        assertTrue(graph.getIncomingEdges("C").isEmpty());

        // Verify we can repopulate the graph after clearing
        graph.addNode("X", null, "NewLabel");
        assertEquals(1, graph.getNodes().size());
        assertEquals(1, graph.getNodesByLabel("NewLabel").size());
        assertTrue(graph.getNodes().contains("X"));
    }

    @Test
    void testTraverseBreadthFirst() {
        // Create a graph structure
        //      A
        //     / \
        //    B   C
        //   / \   \
        //  D   E   F
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        graph.addNode("F");

        graph.addEdge("A", "B", "CONNECT");
        graph.addEdge("A", "C", "CONNECT");
        graph.addEdge("B", "D", "CONNECT");
        graph.addEdge("B", "E", "CONNECT");
        graph.addEdge("C", "F", "CONNECT");

        // Collect visited nodes and depths in order
        List<String> visitedNodes = new ArrayList<>();
        List<Integer> visitedDepths = new ArrayList<>();

        graph.traverseBreadthFirst("A", (node, depth) -> {
            visitedNodes.add(node);
            visitedDepths.add(depth);
            return true;
        });

        // BFS should explore level by level
        // Expected order: A -> B -> C -> D -> E -> F
        assertEquals(6, visitedNodes.size());
        assertEquals("A", visitedNodes.getFirst());
        assertEquals(0, visitedDepths.getFirst());

        // Verify A is visited first (depth 0)
        assertEquals("A", visitedNodes.getFirst());
        assertEquals(0, visitedDepths.getFirst());

        // Verify B and C are visited next (both depth 1)
        assertTrue(visitedNodes.indexOf("B") < visitedNodes.indexOf("D"));
        assertTrue(visitedNodes.indexOf("B") < visitedNodes.indexOf("E"));
        assertTrue(visitedNodes.indexOf("C") < visitedNodes.indexOf("F"));

        // B and C should be at depth 1 (direct neighbors of A)
        assertEquals(1, visitedDepths.get(visitedNodes.indexOf("B")));
        assertEquals(1, visitedDepths.get(visitedNodes.indexOf("C")));

        // D, E, and F should be at depth 2
        assertEquals(2, visitedDepths.get(visitedNodes.indexOf("D")));
        assertEquals(2, visitedDepths.get(visitedNodes.indexOf("E")));
        assertEquals(2, visitedDepths.get(visitedNodes.indexOf("F")));

        // Test early termination
        List<String> earlyTerminationNodes = new ArrayList<>();
        graph.traverseBreadthFirst("A", (node, depth) -> {
            earlyTerminationNodes.add(node);
            return !node.equals("C"); // Stop once we find C
        });

        // Should include A, B, and C (in some order, but A must-be first)
        assertEquals(3, earlyTerminationNodes.size());
        assertEquals("A", earlyTerminationNodes.getFirst());
        assertTrue(earlyTerminationNodes.contains("B"));
        assertTrue(earlyTerminationNodes.contains("C"));

        // Test with a non-existent start node
        List<String> nonExistentStart = new ArrayList<>();
        graph.traverseBreadthFirst("Z", (node, depth) -> {
            nonExistentStart.add(node);
            return true;
        });
        assertTrue(nonExistentStart.isEmpty());
    }

    @Test
    void testTraverseBreadthFirstWithCyclicGraph() {
        // Create a graph with cycles
        //      A ---→ B
        //      ↑      ↓
        //      └── C ←┘
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");

        graph.addEdge("A", "B", "CONNECT");
        graph.addEdge("B", "C", "CONNECT");
        graph.addEdge("C", "A", "CONNECT");

        // Collect visited nodes
        List<String> visitedNodes = new ArrayList<>();

        graph.traverseBreadthFirst("A", (node, depth) -> {
            visitedNodes.add(node);
            return true;
        });

        // Verify all nodes are visited exactly once
        assertEquals(3, visitedNodes.size());
        assertEquals("A", visitedNodes.getFirst());
        assertTrue(visitedNodes.contains("B"));
        assertTrue(visitedNodes.contains("C"));

        // Verify depth increases correctly even with cycles
        List<Integer> depths = new ArrayList<>();
        graph.traverseBreadthFirst("A", (node, depth) -> {
            depths.add(depth);
            return true;
        });

        assertEquals(0, depths.get(0)); // A at depth 0
        assertEquals(1, depths.get(1)); // B at depth 1
        assertEquals(2, depths.get(2)); // C at depth 2
    }

    @Test
    void testAddNodeWithNullLabels() {
        // Add a node with properties but null labels
        Map<String, Object> properties = Map.of("key1", "value1", "key2", 42);

        // Cast null to String[] to avoid the varargs ambiguity warning
        boolean result = graph.addNode("A", properties, (String[]) null);

        // Node should be added successfully
        assertTrue(result);
        assertTrue(graph.getNodes().contains("A"));

        // Properties should be set correctly
        Map<String, Object> nodeProperties = graph.getNodeProperties("A");
        assertEquals(2, nodeProperties.size());
        assertEquals("value1", nodeProperties.get("key1"));
        assertEquals(42, nodeProperties.get("key2"));

        // Verify no labels were added
        Set<String> nodeLabels = graph.getNodesByLabel("SomeLabel");
        assertTrue(nodeLabels.isEmpty());

        // Add another node with the same ID but different properties and still null labels
        Map<String, Object> newProperties = Map.of("key3", "value3");
        boolean secondResult = graph.addNode("A", newProperties, (String[]) null);

        // Node should not be added again (already exists)
        assertFalse(secondResult);

        // Properties should be updated
        nodeProperties = graph.getNodeProperties("A");
        assertEquals(3, nodeProperties.size());
        assertEquals("value1", nodeProperties.get("key1"));
        assertEquals(42, nodeProperties.get("key2"));
        assertEquals("value3", nodeProperties.get("key3"));
    }

    @Test
    void testGetEdgePropertiesWithNonMatchingTarget() {
        // Set up a simple graph
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");

        // Add an edge from A to B with properties
        Map<String, Object> edgeProps = Map.of("weight", 10, "type", "direct");
        graph.addEdge("A", "B", "CONNECTS", edgeProps);

        // Verify properties can be retrieved with the correct target
        Map<String, Object> retrievedProps = graph.getEdgeProperties("A", "B", "CONNECTS");
        assertEquals(2, retrievedProps.size());
        assertEquals(10, retrievedProps.get("weight"));
        assertEquals("direct", retrievedProps.get("type"));

        // Test with a non-matching target (A->C instead of A->B)
        Map<String, Object> nonMatchingTarget = graph.getEdgeProperties("A", "C", "CONNECTS");
        assertTrue(nonMatchingTarget.isEmpty());

        // Test with the correct target but wrong-edge type
        Map<String, Object> wrongType = graph.getEdgeProperties("A", "B", "KNOWS");
        assertTrue(wrongType.isEmpty());

        // Test with non-existent source node
        Map<String, Object> nonExistentSource = graph.getEdgeProperties("X", "B", "CONNECTS");
        assertTrue(nonExistentSource.isEmpty());

        // Add edge from A to C with different properties
        Map<String, Object> otherEdgeProps = Map.of("strength", 5);
        graph.addEdge("A", "C", "CONNECTS", otherEdgeProps);

        // Verify we get the right properties for A->C
        Map<String, Object> aToC = graph.getEdgeProperties("A", "C", "CONNECTS");
        assertEquals(1, aToC.size());
        assertEquals(5, aToC.get("strength"));

        // Verify we still get the right properties for A->B
        Map<String, Object> aToB = graph.getEdgeProperties("A", "B", "CONNECTS");
        assertEquals(2, aToB.size());
        assertEquals(10, aToB.get("weight"));
    }

    @Test
    void testFindPathWithRelationshipTypesAdvanced() {
        // Create a more complex graph with diverse relationship patterns
        //     A --friend-> B --friend-> C <-coworker- H
        //     |            |            |            |
        //     v            v            v            v
        //     D --likes--> E <-owns---- F <-knows--- G
        //     |            ^
        //     v            |
        //     I --trusts--->

        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        graph.addNode("F");
        graph.addNode("G");
        graph.addNode("H");
        graph.addNode("I");

        graph.addEdge("A", "B", "FRIEND");
        graph.addEdge("B", "C", "FRIEND");
        graph.addEdge("A", "D", "FAMILY");
        graph.addEdge("D", "E", "LIKES");
        graph.addEdge("D", "I", "SUPERVISES");
        graph.addEdge("I", "E", "TRUSTS");
        graph.addEdge("F", "E", "OWNS");
        graph.addEdge("C", "F", "DEPENDS_ON");
        graph.addEdge("G", "F", "KNOWS");
        graph.addEdge("H", "C", "COWORKER");
        graph.addEdge("H", "G", "MENTORS");

        // Add bidirectional relationship
        graph.addEdge("E", "B", "REPORTS_TO");
        graph.addEdge("B", "E", "MANAGES");

        // Test 1: Direct path with one relationship type
        Optional<Iterable<String>> path1 = graph.findPath("A", "B", "FRIEND");
        assertTrue(path1.isPresent());
        assertEquals(Arrays.asList("A", "B"), toList(path1.get()));

        // Test 2: Longer path with alternating relationship types
        Optional<Iterable<String>> path2 = graph.findPath("A", "F", "FRIEND", "FRIEND", "DEPENDS_ON");
        assertTrue(path2.isPresent());
        assertEquals(Arrays.asList("A", "B", "C", "F"), toList(path2.get()));

        // Test 3: Path with multiple relationship type options
        Optional<Iterable<String>> path3 = graph.findPath("D", "E", "LIKES", "TRUSTS");
        assertTrue(path3.isPresent());
        List<String> path3List = toList(path3.get());
        assertEquals("D", path3List.getFirst());
        assertEquals("E", path3List.getLast());

        // Test 4: Multi-hop path with the same relationship type
        Optional<Iterable<String>> path4 = graph.findPath("A", "C", "FRIEND");
        assertTrue(path4.isPresent());
        assertEquals(Arrays.asList("A", "B", "C"), toList(path4.get()));

        // Test 5: Relationship type combo that exists but doesn't create a valid path
        Optional<Iterable<String>> path5 = graph.findPath("A", "G", "FRIEND", "COWORKER");
        assertFalse(path5.isPresent());

        // Test 6: Impossible path due to directed graph constraints
        Optional<Iterable<String>> path6 = graph.findPath("I", "D", "SUPERVISES");
        assertFalse(path6.isPresent());

        // Test 7: Cyclic path using bidirectional relationships
        Optional<Iterable<String>> path7 = graph.findPath("B", "B", "MANAGES", "REPORTS_TO");
        assertTrue(path7.isPresent());
        List<String> path7List = toList(path7.get());
        assertTrue(path7List.size() > 1); // Should find B->E->B, not just B
        assertEquals("B", path7List.getFirst());
        assertEquals("B", path7List.getLast());

        // Test 8: No path exists with given types
        Optional<Iterable<String>> path8 = graph.findPath("A", "H", "FAMILY", "LIKES", "TRUSTS");
        assertFalse(path8.isPresent());

        // Test 9: Case of a non-existent relationship type
        Optional<Iterable<String>> path9 = graph.findPath("A", "B", "NONEXISTENT");
        assertFalse(path9.isPresent());

        // Test 10: Mix of existing and non-existing relationship types
        Optional<Iterable<String>> path10 = graph.findPath("A", "C", "FRIEND", "NONEXISTENT");
        assertFalse(path10.isPresent());

        // Test 11: Empty relationship types
        Optional<Iterable<String>> path11 = graph.findPath("A", "B", new String[0]);
        assertFalse(path11.isPresent());

// Test 12: Null relationship types (checks for unrestricted path)
        Optional<Iterable<String>> path12 = graph.findPath("A", "G", (String[]) null);
        assertFalse(path12.isPresent()); // No directed path exists from A to G
    }

}