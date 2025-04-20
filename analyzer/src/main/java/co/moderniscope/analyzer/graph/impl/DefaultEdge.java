package co.moderniscope.analyzer.graph.impl;

import co.moderniscope.analyzer.graph.Edge;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultEdge<I, T> implements Edge<I, T> {
    private final I source;
    private final I target;
    private final T type;
    private final Map<String, Object> properties = new HashMap<>();

    public DefaultEdge(I source, I target, T type) {
        this.source = Objects.requireNonNull(source, "Source node cannot be null");
        this.target = Objects.requireNonNull(target, "Target node cannot be null");
        this.type = Objects.requireNonNull(type, "Relationship type cannot be null");
    }

    @Override
    public I getSource() {
        return source;
    }

    @Override
    public I getTarget() {
        return target;
    }

    @Override
    public T getType() {
        return type;
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
    public Edge<I, T> setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public boolean removeProperty(String key) {
        return properties.remove(key) != null;
    }

    @Override
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultEdge<?, ?> that = (DefaultEdge<?, ?>) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(target, that.target) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, type);
    }

    @Override
    public String toString() {
        return "(" + source + ")-[" + type + "]->(" + target + ")";
    }
}