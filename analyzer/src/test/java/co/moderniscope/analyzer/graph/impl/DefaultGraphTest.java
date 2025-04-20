package co.moderniscope.analyzer.graph.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    void testRemoveNode() {
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

}