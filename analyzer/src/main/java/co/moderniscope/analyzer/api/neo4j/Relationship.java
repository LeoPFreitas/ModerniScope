
package co.moderniscope.analyzer.api.neo4j;

import java.util.Map;

/**
 * Interface representing a relationship between nodes in the dependency graph.
 */
public interface Relationship {
    /**
     * Gets the unique identifier of this relationship.
     *
     * @return The relationship ID
     */
    String getId();

    /**
     * Gets the type of this relationship.
     *
     * @return The relationship type
     */
    String getType();

    /**
     * Gets the source node of this relationship.
     *
     * @return The source node
     */
    Node getSource();

    /**
     * Gets the target node of this relationship.
     *
     * @return The target node
     */
    Node getTarget();

    /**
     * Gets all properties of this relationship.
     *
     * @return Map of property keys to values
     */
    Map<String, Object> getProperties();

    /**
     * Gets a specific property value.
     *
     * @param key The property key
     * @return The property value, or null if not found
     */
    Object getProperty(String key);

    /**
     * Sets a property value.
     *
     * @param key The property key
     * @param value The property value
     */
    void setProperty(String key, Object value);

    /**
     * Checks if this relationship has a specific property.
     *
     * @param key The property key
     * @return true if the property exists, false otherwise
     */
    boolean hasProperty(String key);

    /**
     * Gets the node at the other end of the relationship.
     *
     * @param node The known node (either source or target)
     * @return The other node, or null if the provided node is not part of this relationship
     */
    default Node getOtherNode(Node node) {
        if (getSource().equals(node)) {
            return getTarget();
        } else if (getTarget().equals(node)) {
            return getSource();
        }
        return null;
    }

    /**
     * Checks if this relationship is between the specified nodes.
     *
     * @param node1 The first node
     * @param node2 The second node
     * @return true if the relationship connects these nodes, false otherwise
     */
    default boolean isBetween(Node node1, Node node2) {
        return (getSource().equals(node1) && getTarget().equals(node2)) ||
                (getSource().equals(node2) && getTarget().equals(node1));
    }

    /**
     * Gets the direction of this relationship relative to the specified node.
     *
     * @param node The reference node
     * @return Direction.OUTGOING if node is the source, Direction.INCOMING if node is the target,
     *         null if the node is not part of this relationship
     */
    default Direction getDirection(Node node) {
        if (getSource().equals(node)) {
            return Direction.OUTGOING;
        } else if (getTarget().equals(node)) {
            return Direction.INCOMING;
        }
        return null;
    }
}