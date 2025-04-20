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
}