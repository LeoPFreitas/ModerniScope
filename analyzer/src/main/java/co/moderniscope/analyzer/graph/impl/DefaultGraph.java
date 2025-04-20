package co.moderniscope.analyzer.graph.impl;

import co.moderniscope.analyzer.graph.Graph;

import java.util.*;

public class DefaultGraph<N, E> implements Graph<N, E> {
    private final Map<N, DefaultNode<N, DefaultEdge<N, E>>> nodes = new HashMap<>();
    private final Map<String, Set<N>> nodesByLabel = new HashMap<>();

    @Override
    public boolean addNode(N node) {
        if (nodes.containsKey(node)) {
            return false;
        }
        nodes.put(node, new DefaultNode<>(node));
        return true;
    }

    @Override
    public boolean addNode(N node, Map<String, Object> properties, String... labels) {
        boolean added = false;
        if (!nodes.containsKey(node)) {
            nodes.put(node, new DefaultNode<>(node));
            added = true;
        }

        DefaultNode<N, DefaultEdge<N, E>> nodeObj = nodes.get(node);

        // Add properties
        if (properties != null) {
            properties.forEach(nodeObj::setProperty);
        }

        // Add labels
        if (labels != null) {
            for (String label : labels) {
                nodeObj.addLabel(label);
                nodesByLabel.computeIfAbsent(label, k -> new HashSet<>()).add(node);
            }
        }

        return added;
    }

    @Override
    public boolean addEdge(N source, N target, E edge) {
        return addEdge(source, target, edge, null);
    }

    @Override
    public boolean addEdge(N source, N target, E edge, Map<String, Object> properties) {
        // Ensure nodes exist
        if (!nodes.containsKey(source)) {
            addNode(source);
        }
        if (!nodes.containsKey(target)) {
            addNode(target);
        }

        DefaultNode<N, DefaultEdge<N, E>> sourceNode = nodes.get(source);
        DefaultNode<N, DefaultEdge<N, E>> targetNode = nodes.get(target);

        // Create and register the edge
        DefaultEdge<N, E> edgeObj = new DefaultEdge<>(source, target, edge);

        // Add properties to edge
        if (properties != null) {
            properties.forEach(edgeObj::setProperty);
        }

        // Connect nodes with edge
        sourceNode.addOutgoingEdge(edgeObj);
        targetNode.addIncomingEdge(edgeObj);

        return true;
    }

    @Override
    public Set<N> getNodes() {
        return new HashSet<>(nodes.keySet());
    }

    @Override
    public Set<N> getNodesByLabel(String label) {
        return new HashSet<>(nodesByLabel.getOrDefault(label, Collections.emptySet()));
    }

    @Override
    public Map<String, Object> getNodeProperties(N node) {
        DefaultNode<N, DefaultEdge<N, E>> nodeObj = nodes.get(node);
        return nodeObj != null ? nodeObj.getProperties() : Collections.emptyMap();
    }

    @Override
    public Map<N, Set<E>> getOutgoingEdges(N source) {
        DefaultNode<N, DefaultEdge<N, E>> sourceNode = nodes.get(source);
        if (sourceNode == null) {
            return Collections.emptyMap();
        }

        Map<N, Set<E>> result = new HashMap<>();
        for (DefaultEdge<N, E> edge : sourceNode.getOutgoingEdges()) {
            N target = edge.getTarget();
            result.computeIfAbsent(target, k -> new HashSet<>()).add(edge.getType());
        }

        return result;
    }

    @Override
    public Map<N, Set<E>> getOutgoingEdges(N source, String relationshipType) {
        DefaultNode<N, DefaultEdge<N, E>> sourceNode = nodes.get(source);
        if (sourceNode == null) {
            return Collections.emptyMap();
        }

        Map<N, Set<E>> result = new HashMap<>();
        for (DefaultEdge<N, E> edge : sourceNode.getOutgoingEdges(relationshipType)) {
            N target = edge.getTarget();
            result.computeIfAbsent(target, k -> new HashSet<>()).add(edge.getType());
        }

        return result;
    }

    @Override
    public Map<N, Set<E>> getIncomingEdges(N target) {
        DefaultNode<N, DefaultEdge<N, E>> targetNode = nodes.get(target);
        if (targetNode == null) {
            return Collections.emptyMap();
        }

        Map<N, Set<E>> result = new HashMap<>();
        for (DefaultEdge<N, E> edge : targetNode.getIncomingEdges()) {
            N source = edge.getSource();
            result.computeIfAbsent(source, k -> new HashSet<>()).add(edge.getType());
        }

        return result;
    }

    @Override
    public Map<N, Set<E>> getIncomingEdges(N target, String relationshipType) {
        DefaultNode<N, DefaultEdge<N, E>> targetNode = nodes.get(target);
        if (targetNode == null) {
            return Collections.emptyMap();
        }

        Map<N, Set<E>> result = new HashMap<>();
        for (DefaultEdge<N, E> edge : targetNode.getIncomingEdges(relationshipType)) {
            N source = edge.getSource();
            result.computeIfAbsent(source, k -> new HashSet<>()).add(edge.getType());
        }

        return result;
    }

    @Override
    public Map<String, Object> getEdgeProperties(N source, N target, E edgeType) {
        DefaultNode<N, DefaultEdge<N, E>> sourceNode = nodes.get(source);
        if (sourceNode == null) {
            return Collections.emptyMap();
        }

        for (DefaultEdge<N, E> edge : sourceNode.getOutgoingEdges()) {
            if (edge.getTarget().equals(target) && edge.getType().equals(edgeType)) {
                return edge.getProperties();
            }
        }

        return Collections.emptyMap();
    }

    @Override
    public Optional<Iterable<N>> findPath(N start, N end) {
        // Simple BFS implementation for path finding
        if (!nodes.containsKey(start) || !nodes.containsKey(end)) {
            return Optional.empty();
        }

        if (start.equals(end)) {
            return Optional.of(Collections.singletonList(start));
        }

        Map<N, N> predecessors = new HashMap<>();
        Queue<N> queue = new LinkedList<>();
        Set<N> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            N current = queue.poll();

            for (Map.Entry<N, Set<E>> entry : getOutgoingEdges(current).entrySet()) {
                N neighbor = entry.getKey();

                if (!visited.contains(neighbor)) {
                    predecessors.put(neighbor, current);
                    visited.add(neighbor);
                    queue.add(neighbor);

                    if (neighbor.equals(end)) {
                        // Reconstruct path
                        List<N> path = new ArrayList<>();
                        N step = end;

                        while (step != null) {
                            path.add(0, step);
                            step = predecessors.get(step);
                        }

                        return Optional.of(path);
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Iterable<N>> findPath(N start, N end, String... relationshipTypes) {
        // Similar to findPath but limiting to specific relationship types
        if (!nodes.containsKey(start) || !nodes.containsKey(end)) {
            return Optional.empty();
        }

        if (start.equals(end)) {
            return Optional.of(Collections.singletonList(start));
        }

        Set<String> allowedTypes = new HashSet<>(Arrays.asList(relationshipTypes));
        Map<N, N> predecessors = new HashMap<>();
        Queue<N> queue = new LinkedList<>();
        Set<N> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            N current = queue.poll();
            DefaultNode<N, DefaultEdge<N, E>> currentNode = nodes.get(current);

            for (DefaultEdge<N, E> edge : currentNode.getOutgoingEdges()) {
                // Skip if not an allowed relationship type
                if (!allowedTypes.contains(edge.getType().toString())) {
                    continue;
                }

                N neighbor = edge.getTarget();

                if (!visited.contains(neighbor)) {
                    predecessors.put(neighbor, current);
                    visited.add(neighbor);
                    queue.add(neighbor);

                    if (neighbor.equals(end)) {
                        // Reconstruct path
                        List<N> path = new ArrayList<>();
                        N step = end;

                        while (step != null) {
                            path.add(0, step);
                            step = predecessors.get(step);
                        }

                        return Optional.of(path);
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void traverseDepthFirst(N start, NodeVisitor<N> visitor) {
        if (!nodes.containsKey(start)) {
            return;
        }

        Set<N> visited = new HashSet<>();
        dfsVisit(start, visitor, visited, 0);
    }

    private boolean dfsVisit(N node, NodeVisitor<N> visitor, Set<N> visited, int depth) {
        if (!visitor.visit(node, depth)) {
            return false;
        }

        visited.add(node);

        for (N neighbor : getOutgoingEdges(node).keySet()) {
            if (!visited.contains(neighbor)) {
                if (!dfsVisit(neighbor, visitor, visited, depth + 1)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean removeNode(N node) {
        DefaultNode<N, DefaultEdge<N, E>> nodeObj = nodes.remove(node);
        if (nodeObj == null) {
            return false;
        }

        // Remove node from label indexes
        for (String label : nodeObj.getLabels()) {
            Set<N> labeledNodes = nodesByLabel.get(label);
            if (labeledNodes != null) {
                labeledNodes.remove(node);
                if (labeledNodes.isEmpty()) {
                    nodesByLabel.remove(label);
                }
            }
        }

        // Remove all edges connected to this node
        // First, handle incoming edges from other nodes
        for (DefaultEdge<N, E> edge : nodeObj.getIncomingEdges()) {
            DefaultNode<N, DefaultEdge<N, E>> sourceNode = nodes.get(edge.getSource());
            if (sourceNode != null) {
                sourceNode.removeOutgoingEdge(edge);
            }
        }

        // Then, handle outgoing edges to other nodes
        for (DefaultEdge<N, E> edge : nodeObj.getOutgoingEdges()) {
            DefaultNode<N, DefaultEdge<N, E>> targetNode = nodes.get(edge.getTarget());
            if (targetNode != null) {
                targetNode.removeIncomingEdge(edge);
            }
        }

        return true;
    }

    @Override
    public boolean removeEdge(N source, N target, E edgeType) {
        DefaultNode<N, DefaultEdge<N, E>> sourceNode = nodes.get(source);
        DefaultNode<N, DefaultEdge<N, E>> targetNode = nodes.get(target);

        if (sourceNode == null || targetNode == null) {
            return false;
        }

        // Find the edge to remove
        DefaultEdge<N, E> edgeToRemove = null;
        for (DefaultEdge<N, E> edge : sourceNode.getOutgoingEdges()) {
            if (edge.getTarget().equals(target) && edge.getType().equals(edgeType)) {
                edgeToRemove = edge;
                break;
            }
        }

        if (edgeToRemove == null) {
            return false;
        }

        // Remove the edge from both nodes
        sourceNode.removeOutgoingEdge(edgeToRemove);
        targetNode.removeIncomingEdge(edgeToRemove);

        return true;
    }

    @Override
    public int removeEdges(N source, N target, String relationshipType) {
        DefaultNode<N, DefaultEdge<N, E>> sourceNode = nodes.get(source);
        DefaultNode<N, DefaultEdge<N, E>> targetNode = nodes.get(target);

        if (sourceNode == null || targetNode == null) {
            return 0;
        }

        // Find all edges matching the relationship type
        Set<DefaultEdge<N, E>> edgesToRemove = new HashSet<>();
        for (DefaultEdge<N, E> edge : sourceNode.getOutgoingEdges()) {
            if (edge.getTarget().equals(target) &&
                    edge.getType().toString().equals(relationshipType)) {
                edgesToRemove.add(edge);
            }
        }

        // Remove all matching edges
        for (DefaultEdge<N, E> edge : edgesToRemove) {
            sourceNode.removeOutgoingEdge(edge);
            targetNode.removeIncomingEdge(edge);
        }

        return edgesToRemove.size();
    }

    @Override
    public void clear() {
        nodes.clear();
        nodesByLabel.clear();
    }

    @Override
    public void traverseBreadthFirst(N start, NodeVisitor<N> visitor) {
        if (!nodes.containsKey(start)) {
            return;
        }

        Set<N> visited = new HashSet<>();
        Queue<Map.Entry<N, Integer>> queue = new LinkedList<>();

        queue.add(new AbstractMap.SimpleEntry<>(start, 0));
        visited.add(start);

        while (!queue.isEmpty()) {
            Map.Entry<N, Integer> current = queue.poll();
            N node = current.getKey();
            int depth = current.getValue();

            if (!visitor.visit(node, depth)) {
                return;
            }

            for (N neighbor : getOutgoingEdges(node).keySet()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(new AbstractMap.SimpleEntry<>(neighbor, depth + 1));
                }
            }
        }
    }
}