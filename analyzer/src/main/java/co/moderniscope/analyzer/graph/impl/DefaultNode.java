package co.moderniscope.analyzer.graph.impl;

import co.moderniscope.analyzer.graph.Edge;
import co.moderniscope.analyzer.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultNode<I, E extends Edge<I, ?>> implements Node<I, E> {
    private final I id;
    private final Map<String, Object> properties = new HashMap<>();
    private final Set<String> labels = new HashSet<>();
    private final Set<E> outgoingEdges = new HashSet<>();
    private final Set<E> incomingEdges = new HashSet<>();

    public DefaultNode(I id) {
        this.id = Objects.requireNonNull(id, "Node ID cannot be null");
    }

    @Override
    public I getId() {
        return id;
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
    public Node<I, E> setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public boolean removeProperty(String key) {
        return properties.remove(key) != null;
    }

    @Override
    public Set<E> getOutgoingEdges() {
        return new HashSet<>(outgoingEdges);
    }

    @Override
    public Set<E> getOutgoingEdges(String relationshipType) {
        return outgoingEdges.stream()
                .filter(e -> relationshipType.equals(e.getType().toString()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<E> getIncomingEdges() {
        return new HashSet<>(incomingEdges);
    }

    @Override
    public Set<E> getIncomingEdges(String relationshipType) {
        return incomingEdges.stream()
                .filter(e -> relationshipType.equals(e.getType().toString()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getLabels() {
        return new HashSet<>(labels);
    }

    @Override
    public boolean addLabel(String label) {
        return labels.add(label);
    }

    @Override
    public boolean removeLabel(String label) {
        return labels.remove(label);
    }

    @Override
    public boolean hasLabel(String label) {
        return labels.contains(label);
    }

    // These methods are for internal use by the graph implementation
    void addOutgoingEdge(E edge) {
        outgoingEdges.add(edge);
    }

    void addIncomingEdge(E edge) {
        incomingEdges.add(edge);
    }

    void removeOutgoingEdge(E edge) {
        outgoingEdges.remove(edge);
    }

    void removeIncomingEdge(E edge) {
        incomingEdges.remove(edge);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNode<?, ?> that = (DefaultNode<?, ?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node(" + id + ")" +
                (labels.isEmpty() ? "" : ":" + String.join(":", labels));
    }
}