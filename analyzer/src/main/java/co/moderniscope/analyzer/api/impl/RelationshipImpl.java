package co.moderniscope.analyzer.api.impl;

import co.moderniscope.analyzer.api.neo4j.Direction;
import co.moderniscope.analyzer.api.neo4j.Node;
import co.moderniscope.analyzer.api.neo4j.Relationship;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of a relationship in the dependency graph.
 */
public class RelationshipImpl implements Relationship {
    private final String id;
    private final String type;
    private final Node source;
    private final Node target;
    private final Map<String, Object> properties;

    /**
     * Creates a new relationship with a generated ID.
     *
     * @param type   The relationship type
     * @param source The source node
     * @param target The target node
     */
    public RelationshipImpl(String type, Node source, Node target) {
        this(UUID.randomUUID().toString(), type, source, target);
    }

    /**
     * Creates a new relationship with a specified ID.
     *
     * @param id     The relationship ID
     * @param type   The relationship type
     * @param source The source node
     * @param target The target node
     */
    public RelationshipImpl(String id, String type, Node source, Node target) {
        this.id = id;
        this.type = type;
        this.source = source;
        this.target = target;
        this.properties = new HashMap<>();
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
    public Node getSource() {
        return source;
    }

    @Override
    public Node getTarget() {
        return target;
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
    public Node getOtherNode(Node node) {
        if (source.equals(node)) {
            return target;
        } else if (target.equals(node)) {
            return source;
        }
        return null;
    }

    @Override
    public boolean isBetween(Node node1, Node node2) {
        return (source.equals(node1) && target.equals(node2)) ||
                (source.equals(node2) && target.equals(node1));
    }

    @Override
    public Direction getDirection(Node node) {
        if (source.equals(node)) {
            return Direction.OUTGOING;
        } else if (target.equals(node)) {
            return Direction.INCOMING;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipImpl that = (RelationshipImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", source=" + source.getName() +
                ", target=" + target.getName() +
                '}';
    }
}