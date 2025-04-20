package co.moderniscope.analyzer.graph;

import java.util.Map;
import java.util.Set;

/**
 * Represents a node in a property graph.
 * Nodes are entities that can have:
 * - A unique identifier
 * - Multiple properties (key-value pairs)
 * - Multiple labels (categories/types)
 * - Connections to other nodes via relationships (edges)
 *
 * @param <I> Type of the node identifier
 * @param <E> Type of edges connected to this node
 */
public interface Node<I, E> {
    /**
     * Gets the unique identifier for this node.
     *
     * @return The node identifier
     */
    I getId();

    /**
     * Gets all properties of this node.
     *
     * @return Map of property name to property value
     */
    Map<String, Object> getProperties();

    /**
     * Gets a specific property value.
     *
     * @param key Property name
     * @return Property value or null if not found
     */
    Object getProperty(String key);

    /**
     * Sets a property on this node.
     *
     * @param key   Property name
     * @param value Property value
     * @return This node for method chaining
     */
    Node<I, E> setProperty(String key, Object value);

    /**
     * Removes a property from this node.
     *
     * @param key Property name to remove
     * @return true if the property was removed, false if it didn't exist
     */
    boolean removeProperty(String key);

    /**
     * Gets all outgoing edges from this node.
     *
     * @return Set of outgoing edges
     */
    Set<E> getOutgoingEdges();

    /**
     * Gets outgoing edges of a specific relationship type.
     *
     * @param relationshipType The type of relationship to filter by
     * @return Set of outgoing edges with the specified relationship type
     */
    Set<E> getOutgoingEdges(String relationshipType);

    /**
     * Gets all incoming edges to this node.
     *
     * @return Set of incoming edges
     */
    Set<E> getIncomingEdges();

    /**
     * Gets incoming edges of a specific relationship type.
     *
     * @param relationshipType The type of relationship to filter by
     * @return Set of incoming edges with the specified relationship type
     */
    Set<E> getIncomingEdges(String relationshipType);

    /**
     * Gets the label(s) of this node.
     *
     * @return Set of labels for this node
     */
    Set<String> getLabels();

    /**
     * Adds a label to this node.
     *
     * @param label The label to add
     * @return true if the label was added
     */
    boolean addLabel(String label);

    /**
     * Removes a label from this node.
     *
     * @param label The label to remove
     * @return true if the label was removed, false if it didn't exist
     */
    boolean removeLabel(String label);

    /**
     * Checks if this node has a specific label.
     *
     * @param label The label to check
     * @return true if the node has the label
     */
    boolean hasLabel(String label);
}