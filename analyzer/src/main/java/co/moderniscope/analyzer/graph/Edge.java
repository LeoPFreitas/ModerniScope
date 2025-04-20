package co.moderniscope.analyzer.graph;

import java.util.Map;

/**
 * Represents an edge in a property graph.
 * Edges represent relationships between nodes and can have:
 * - A source node
 * - A target node
 * - A relationship type
 * - Multiple properties (key-value pairs)
 *
 * @param <I> Type of the node identifiers
 * @param <T> Type of the edge (relationship type)
 */
public interface Edge<I, T> {
    /**
     * Gets the source node of this edge.
     *
     * @return Source node identifier
     */
    I getSource();

    /**
     * Gets the target node of this edge.
     *
     * @return Target node identifier
     */
    I getTarget();

    /**
     * Gets the type of this edge.
     * In property graphs, each edge has one and only one relationship type.
     *
     * @return Edge type (relationship type)
     */
    T getType();

    /**
     * Gets all properties of this edge.
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
     * Sets a property on this edge.
     *
     * @param key   Property name
     * @param value Property value
     * @return This edge for method chaining
     */
    Edge<I, T> setProperty(String key, Object value);

    /**
     * Removes a property from this edge.
     *
     * @param key Property name to remove
     * @return true if the property was removed, false if it didn't exist
     */
    boolean removeProperty(String key);

    /**
     * Checks if this edge has a specific property.
     *
     * @param key Property name
     * @return true if the edge has the property
     */
    boolean hasProperty(String key);

    /**
     * Gets a unique identifier for this edge.
     * By default, combines source, relationship type, and target.
     *
     * @return A unique identifier for this edge
     */
    default String getUniqueId() {
        return getSource() + "-" + getType() + "-" + getTarget();
    }
}