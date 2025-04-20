package co.moderniscope.analyzer.api.impl;

import co.moderniscope.analyzer.api.PropertyKeys;
import co.moderniscope.analyzer.api.neo4j.Direction;
import co.moderniscope.analyzer.api.neo4j.Node;
import co.moderniscope.analyzer.api.neo4j.Relationship;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of a node in the dependency graph.
 */
public class NodeImpl implements Node {
    private final String id;
    private final String type;
    private final Map<String, Object> properties;
    private final Set<String> labels;

    // This will be populated by the graph when relationships are added
    // NOTE: The dependency graph should manage this data, not the node itself
    private transient Map<String, Map<Direction, Set<Relationship>>> relationships;

    /**
     * Creates a new node with a generated ID.
     *
     * @param type The node type
     * @param name The node name
     */
    public NodeImpl(String type, String name) {
        this(UUID.randomUUID().toString(), type, name);
    }

    /**
     * Creates a new node with a specified ID.
     *
     * @param id   The node ID
     * @param type The node type
     * @param name The node name
     */
    public NodeImpl(String id, String type, String name) {
        this.id = id;
        this.type = type;
        this.properties = new HashMap<>();
        this.labels = new HashSet<>();
        this.relationships = new ConcurrentHashMap<>();

        // Add type as the default label (Neo4j convention)
        this.labels.add(type);

        this.properties.put(PropertyKeys.NAME, name);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return (String) properties.get(PropertyKeys.NAME);
    }

    @Override
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    @Override
    public Set<String> getLabels() {
        return new HashSet<>(labels);
    }

    @Override
    public void addLabel(String label) {
        if (label != null && !label.isEmpty()) {
            labels.add(label);
        }
    }

    @Override
    public boolean removeLabel(String label) {
        return labels.remove(label);
    }

    @Override
    public boolean hasLabel(String label) {
        return labels.contains(label);
    }

    /**
     * Sets the relationships map for this node.
     * This should only be called by the graph implementation.
     *
     * @param relationships The relationship map
     */
    public void setRelationships(Map<String, Map<Direction, Set<Relationship>>> relationships) {
        this.relationships = relationships;
    }

    @Override
    public Collection<Relationship> getRelationships(Direction direction) {
        if (relationships == null) {
            return Collections.emptyList();
        }

        Set<Relationship> result = new HashSet<>();
        for (Map<Direction, Set<Relationship>> directionMap : relationships.values()) {
            Set<Relationship> directionalRels = directionMap.getOrDefault(direction, Collections.emptySet());
            result.addAll(directionalRels);

            // If BOTH direction is requested, also include the specific directions
            if (direction == Direction.BOTH) {
                result.addAll(directionMap.getOrDefault(Direction.INCOMING, Collections.emptySet()));
                result.addAll(directionMap.getOrDefault(Direction.OUTGOING, Collections.emptySet()));
            }
        }

        return result;
    }

    @Override
    public Collection<Relationship> getRelationships(Direction direction, String... types) {
        if (relationships == null || types.length == 0) {
            return getRelationships(direction);
        }

        Set<Relationship> result = new HashSet<>();
        for (String type : types) {
            Map<Direction, Set<Relationship>> directionMap = relationships.getOrDefault(type, Collections.emptyMap());

            // Add relationships for the specified direction
            result.addAll(directionMap.getOrDefault(direction, Collections.emptySet()));

            // If BOTH direction is requested, also include the specific directions
            if (direction == Direction.BOTH) {
                result.addAll(directionMap.getOrDefault(Direction.INCOMING, Collections.emptySet()));
                result.addAll(directionMap.getOrDefault(Direction.OUTGOING, Collections.emptySet()));
            }
        }

        return result;
    }

    @Override
    public Collection<Node> getConnectedNodes(Direction direction) {
        return getRelationships(direction).stream()
                .map(rel -> {
                    if (direction == Direction.OUTGOING) {
                        return rel.getTarget();
                    } else if (direction == Direction.INCOMING) {
                        return rel.getSource();
                    } else {
                        return rel.getOtherNode(this);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Node> getConnectedNodes(Direction direction, String... relationshipTypes) {
        return getRelationships(direction, relationshipTypes).stream()
                .map(rel -> {
                    if (direction == Direction.OUTGOING) {
                        return rel.getTarget();
                    } else if (direction == Direction.INCOMING) {
                        return rel.getSource();
                    } else {
                        return rel.getOtherNode(this);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasRelationship(Direction direction) {
        return !getRelationships(direction).isEmpty();
    }

    @Override
    public boolean hasRelationship(Direction direction, String... types) {
        return !getRelationships(direction, types).isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeImpl node = (NodeImpl) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + getName() + '\'' +
                ", labels=" + labels +
                '}';
    }
}