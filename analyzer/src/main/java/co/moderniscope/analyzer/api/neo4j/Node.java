package co.moderniscope.analyzer.api.neo4j;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Interface representing a node in the dependency graph.
 */
public interface Node {
    /**
     * Gets the unique identifier of this node.
     *
     * @return The node ID
     */
    String getId();

    /**
     * Gets the type of this node.
     *
     * @return The node type
     */
    String getType();

    /**
     * Gets the name of this node.
     *
     * @return The node name
     */
    String getName();

    /**
     * Gets all properties of this node.
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
     * @param key   The property key
     * @param value The property value
     */
    void setProperty(String key, Object value);

    /**
     * Checks if this node has a specific property.
     *
     * @param key The property key
     * @return true if the property exists, false otherwise
     */
    boolean hasProperty(String key);

    /**
     * Gets all labels assigned to this node.
     *
     * @return Set of labels
     */
    Set<String> getLabels();

    /**
     * Adds a label to this node.
     *
     * @param label The label to add
     */
    void addLabel(String label);

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
     * @return true if the node has the label, false otherwise
     */
    boolean hasLabel(String label);

    /**
     * Gets relationships connected to this node in the specified direction.
     *
     * @param direction The direction of relationships to get
     * @return Collection of relationships
     */
    Collection<Relationship> getRelationships(Direction direction);

    /**
     * Gets relationships of specific types connected to this node in the specified direction.
     *
     * @param direction The direction of relationships to get
     * @param types     The relationship types to filter by
     * @return Collection of matching relationships
     */
    Collection<Relationship> getRelationships(Direction direction, String... types);

    /**
     * Gets all nodes connected to this node through relationships in the specified direction.
     *
     * @param direction The direction of relationships to traverse
     * @return Collection of connected nodes
     */
    Collection<Node> getConnectedNodes(Direction direction);

    /**
     * Gets nodes connected to this node through relationships of specific types in the specified direction.
     *
     * @param direction         The direction of relationships to traverse
     * @param relationshipTypes The relationship types to filter by
     * @return Collection of connected nodes
     */
    Collection<Node> getConnectedNodes(Direction direction, String... relationshipTypes);

    /**
     * Checks if this node has any relationship of the specified direction.
     *
     * @param direction The direction to check
     * @return true if the node has at least one relationship in that direction, false otherwise
     */
    boolean hasRelationship(Direction direction);

    /**
     * Checks if this node has a relationship of the specified direction and type.
     *
     * @param direction The direction to check
     * @param types     The relationship types to check
     * @return true if the node has at least one matching relationship, false otherwise
     */
    boolean hasRelationship(Direction direction, String... types);
}