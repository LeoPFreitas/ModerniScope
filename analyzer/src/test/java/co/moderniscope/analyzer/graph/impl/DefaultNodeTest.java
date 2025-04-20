package co.moderniscope.analyzer.graph.impl;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultNodeTest {

    @Test
    void testConstructorAndGetId() {
        DefaultNode<String, DefaultEdge<String, String>> node = new DefaultNode<>("A");
        assertEquals("A", node.getId());

        // Test null id
        assertThrows(NullPointerException.class, () -> new DefaultNode<>(null));
    }

    @Test
    void testProperties() {
        DefaultNode<String, DefaultEdge<String, String>> node = new DefaultNode<>("A");

        // Add properties
        node.setProperty("name", "Node A");
        node.setProperty("age", 30);

        // Get properties
        assertEquals("Node A", node.getProperty("name"));
        assertEquals(30, node.getProperty("age"));
        assertNull(node.getProperty("nonExistent"));

        // Get all properties
        Map<String, Object> props = node.getProperties();
        assertEquals(2, props.size());
        assertEquals("Node A", props.get("name"));
        assertEquals(30, props.get("age"));

        // Verify getProperties returns a copy
        props.put("newProp", "value");
        assertNull(node.getProperty("newProp"));

        // Remove property
        assertTrue(node.removeProperty("name"));
        assertNull(node.getProperty("name"));
        assertEquals(1, node.getProperties().size());

        // Remove non-existent property
        assertFalse(node.removeProperty("nonExistent"));
    }

    @Test
    void testLabels() {
        DefaultNode<String, DefaultEdge<String, String>> node = new DefaultNode<>("A");

        // Initially no labels
        assertTrue(node.getLabels().isEmpty());
        assertFalse(node.hasLabel("Person"));

        // Add labels
        assertTrue(node.addLabel("Person"));
        assertTrue(node.addLabel("Employee"));

        // Verify labels were added
        assertTrue(node.hasLabel("Person"));
        assertTrue(node.hasLabel("Employee"));
        assertEquals(2, node.getLabels().size());

        // Adding duplicate label
        assertFalse(node.addLabel("Person"));
        assertEquals(2, node.getLabels().size());

        // Remove label
        assertTrue(node.removeLabel("Person"));
        assertFalse(node.hasLabel("Person"));
        assertEquals(1, node.getLabels().size());

        // Remove non-existent label
        assertFalse(node.removeLabel("NonExistent"));
    }

    @Test
    void testEdges() {
        DefaultNode<String, DefaultEdge<String, String>> nodeA = new DefaultNode<>("A");
        DefaultNode<String, DefaultEdge<String, String>> nodeB = new DefaultNode<>("B");

        // Create edges
        DefaultEdge<String, String> edge1 = new DefaultEdge<>("A", "B", "KNOWS");
        DefaultEdge<String, String> edge2 = new DefaultEdge<>("A", "B", "WORKS_WITH");
        DefaultEdge<String, String> edge3 = new DefaultEdge<>("B", "A", "MANAGES");

        // Add edges
        nodeA.addOutgoingEdge(edge1);
        nodeA.addOutgoingEdge(edge2);
        nodeA.addIncomingEdge(edge3);

        nodeB.addIncomingEdge(edge1);
        nodeB.addIncomingEdge(edge2);
        nodeB.addOutgoingEdge(edge3);

        // Test outgoing edges
        Set<DefaultEdge<String, String>> outEdges = nodeA.getOutgoingEdges();
        assertEquals(2, outEdges.size());
        assertTrue(outEdges.contains(edge1));
        assertTrue(outEdges.contains(edge2));

        // Test filtered outgoing edges
        Set<DefaultEdge<String, String>> knowsEdges = nodeA.getOutgoingEdges("KNOWS");
        assertEquals(1, knowsEdges.size());
        assertTrue(knowsEdges.contains(edge1));

        // Test incoming edges
        Set<DefaultEdge<String, String>> inEdges = nodeA.getIncomingEdges();
        assertEquals(1, inEdges.size());
        assertTrue(inEdges.contains(edge3));

        // Test filtered incoming edges
        Set<DefaultEdge<String, String>> managesEdges = nodeA.getIncomingEdges("MANAGES");
        assertEquals(1, managesEdges.size());
        assertTrue(managesEdges.contains(edge3));

        // Remove edges
        nodeA.removeOutgoingEdge(edge1);
        assertEquals(1, nodeA.getOutgoingEdges().size());
        assertFalse(nodeA.getOutgoingEdges().contains(edge1));

        nodeA.removeIncomingEdge(edge3);
        assertTrue(nodeA.getIncomingEdges().isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        DefaultNode<String, DefaultEdge<String, String>> node1 = new DefaultNode<>("A");
        DefaultNode<String, DefaultEdge<String, String>> node2 = new DefaultNode<>("A");
        DefaultNode<String, DefaultEdge<String, String>> node3 = new DefaultNode<>("B");

        // Same ID should be equal
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        // Different ID should not be equal
        assertNotEquals(node1, node3);

        // Properties and labels should not affect equality
        node1.setProperty("prop", "value");
        node1.addLabel("Label");
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        // Edges should not affect equality
        DefaultEdge<String, String> edge = new DefaultEdge<>("A", "B", "KNOWS");
        node1.addOutgoingEdge(edge);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());
    }

    @Test
    void testToString() {
        DefaultNode<String, DefaultEdge<String, String>> node = new DefaultNode<>("A");
        assertEquals("Node(A)", node.toString());

        // Add labels and check toString format
        node.addLabel("Person");
        assertEquals("Node(A):Person", node.toString());

        node.addLabel("Employee");
        // The order of labels in toString is not guaranteed, so check for both possibilities
        String toString = node.toString();
        assertTrue(toString.equals("Node(A):Person:Employee") || toString.equals("Node(A):Employee:Person"));
    }

    @Test
    void testSetPropertyReturnsSelf() {
        DefaultNode<String, DefaultEdge<String, String>> node = new DefaultNode<>("A");

        // Test method chaining
        DefaultNode<String, DefaultEdge<String, String>> result = (DefaultNode<String, DefaultEdge<String, String>>) node
                .setProperty("prop1", "value1")
                .setProperty("prop2", "value2");

        assertSame(node, result);
        assertEquals("value1", node.getProperty("prop1"));
        assertEquals("value2", node.getProperty("prop2"));
    }

    @Test
    void testWithIntegerIds() {
        // Test with different generic types
        DefaultNode<Integer, DefaultEdge<Integer, String>> node = new DefaultNode<>(123);

        assertEquals(Integer.valueOf(123), node.getId());

        // Add property and verify
        node.setProperty("key", "value");
        assertEquals("value", node.getProperty("key"));

        // Test with edges
        DefaultEdge<Integer, String> edge = new DefaultEdge<>(123, 456, "CONNECTS");
        node.addOutgoingEdge(edge);

        assertEquals(1, node.getOutgoingEdges().size());
        assertTrue(node.getOutgoingEdges().contains(edge));
    }
}