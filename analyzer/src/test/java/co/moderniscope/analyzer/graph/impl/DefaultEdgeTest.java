package co.moderniscope.analyzer.graph.impl;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultEdgeTest {

    @Test
    void testConstructorAndGetters() {
        // Create a basic edge
        DefaultEdge<String, String> edge = new DefaultEdge<>("A", "B", "KNOWS");

        // Verify getters return correct values
        assertEquals("A", edge.getSource());
        assertEquals("B", edge.getTarget());
        assertEquals("KNOWS", edge.getType());
        assertTrue(edge.getProperties().isEmpty());
    }

    @Test
    void testConstructorWithNullValues() {
        // Test null source
        assertThrows(NullPointerException.class, () -> new DefaultEdge<>(null, "B", "KNOWS"));

        // Test null target
        assertThrows(NullPointerException.class, () -> new DefaultEdge<>("A", null, "KNOWS"));

        // Test null type
        assertThrows(NullPointerException.class, () -> new DefaultEdge<>("A", "B", null));
    }

    @Test
    void testProperties() {
        DefaultEdge<String, String> edge = new DefaultEdge<>("A", "B", "KNOWS");

        // Add properties
        edge.setProperty("since", 2020);
        edge.setProperty("active", true);

        // Verify properties are stored
        assertEquals(2020, edge.getProperty("since"));
        assertEquals(true, edge.getProperty("active"));
        assertNull(edge.getProperty("nonExistent"));

        // Test hasProperty
        assertTrue(edge.hasProperty("since"));
        assertTrue(edge.hasProperty("active"));
        assertFalse(edge.hasProperty("nonExistent"));

        // Test getProperties returns a copy
        Map<String, Object> props = edge.getProperties();
        assertEquals(2, props.size());
        assertEquals(2020, props.get("since"));
        assertEquals(true, props.get("active"));

        // Modify the returned map
        props.put("newProp", "value");

        // Verify original properties are unchanged
        assertFalse(edge.hasProperty("newProp"));
        assertEquals(2, edge.getProperties().size());

        // Test removeProperty
        assertTrue(edge.removeProperty("since"));
        assertFalse(edge.hasProperty("since"));
        assertEquals(1, edge.getProperties().size());

        // Test removing non-existent property
        assertFalse(edge.removeProperty("nonExistent"));
    }

    @Test
    void testSetPropertyReturnsSelf() {
        DefaultEdge<String, String> edge = new DefaultEdge<>("A", "B", "KNOWS");

        // Test method chaining
        DefaultEdge<String, String> result = (DefaultEdge<String, String>) edge
                .setProperty("prop1", "value1")
                .setProperty("prop2", "value2");

        assertSame(edge, result);
        assertEquals("value1", edge.getProperty("prop1"));
        assertEquals("value2", edge.getProperty("prop2"));
    }

    @Test
    void testEqualsAndHashCode() {
        DefaultEdge<String, String> edge1 = new DefaultEdge<>("A", "B", "KNOWS");
        DefaultEdge<String, String> edge2 = new DefaultEdge<>("A", "B", "KNOWS");
        DefaultEdge<String, String> edge3 = new DefaultEdge<>("A", "B", "FRIENDS");
        DefaultEdge<String, String> edge4 = new DefaultEdge<>("B", "A", "KNOWS");

        // Same source, target, and type should be equal
        assertEquals(edge1, edge2);
        assertEquals(edge1.hashCode(), edge2.hashCode());

        // Different type should not be equal
        assertNotEquals(edge1, edge3);

        // Different source/target should not be equal
        assertNotEquals(edge1, edge4);

        // Properties should not affect equality
        edge1.setProperty("property", "value");
        edge2.setProperty("different", "other");
        assertEquals(edge1, edge2);
        assertEquals(edge1.hashCode(), edge2.hashCode());
    }

    @Test
    void testToString() {
        DefaultEdge<String, String> edge = new DefaultEdge<>("A", "B", "KNOWS");

        // Verify toString format
        assertEquals("(A)-[KNOWS]->(B)", edge.toString());

        // Add properties (should not affect toString)
        edge.setProperty("since", 2020);
        assertEquals("(A)-[KNOWS]->(B)", edge.toString());
    }

    @Test
    void testWithIntegerNodes() {
        // Test with different generic types
        DefaultEdge<Integer, String> edge = new DefaultEdge<>(1, 2, "CONNECTS");

        assertEquals(Integer.valueOf(1), edge.getSource());
        assertEquals(Integer.valueOf(2), edge.getTarget());
        assertEquals("CONNECTS", edge.getType());

        // Add property and verify
        edge.setProperty("weight", 10);
        assertEquals(10, edge.getProperty("weight"));
    }
}