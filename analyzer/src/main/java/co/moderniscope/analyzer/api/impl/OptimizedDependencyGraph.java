package co.moderniscope.analyzer.api.impl;

import co.moderniscope.analyzer.api.DependencyGraph;
import co.moderniscope.analyzer.api.neo4j.Direction;
import co.moderniscope.analyzer.api.neo4j.Node;
import co.moderniscope.analyzer.api.neo4j.Relationship;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An optimized implementation of a dependency graph for representing Neo4j-like graphs.
 */
public class OptimizedDependencyGraph implements DependencyGraph {

    // Maps node IDs to Node objects
    private final Map<String, Node> nodeMap = new ConcurrentHashMap<>();

    // Maps relationship IDs to Relationship objects
    private final Map<String, Relationship> relationshipMap = new ConcurrentHashMap<>();

    // Optimized structure for node traversal
    // Node -> RelationshipType -> Direction -> Set<Relationship>
    private final Map<Node, Map<String, Map<Direction, Set<Relationship>>>> nodeRelationshipMap = new ConcurrentHashMap<>();

    // Index: Label -> Set<Node>
    private final Map<String, Set<Node>> labelIndex = new ConcurrentHashMap<>();

    // Index: NodeType -> Set<Node>
    private final Map<String, Set<Node>> typeIndex = new ConcurrentHashMap<>();

    // Index: PropertyKey -> PropertyValue -> Set<Node>
    private final Map<String, Map<Object, Set<Node>>> nodePropertyIndex = new ConcurrentHashMap<>();

    // Index: PropertyKey -> PropertyValue -> Set<Relationship>
    private final Map<String, Map<Object, Set<Relationship>>> relationshipPropertyIndex = new ConcurrentHashMap<>();

    @Override
    public Node addNode(Node node) {
        nodeMap.put(node.getId(), node);

        // Add to type index
        typeIndex.computeIfAbsent(node.getType(), k -> ConcurrentHashMap.newKeySet()).add(node);

        // Add to label indices
        for (String label : node.getLabels()) {
            labelIndex.computeIfAbsent(label, k -> ConcurrentHashMap.newKeySet()).add(node);
        }

        // Add to property indices
        for (Map.Entry<String, Object> entry : node.getProperties().entrySet()) {
            Map<Object, Set<Node>> valueMap = nodePropertyIndex.computeIfAbsent(
                    entry.getKey(), k -> new ConcurrentHashMap<>());
            valueMap.computeIfAbsent(entry.getValue(), k -> ConcurrentHashMap.newKeySet()).add(node);
        }

        // Initialize node in relationship map
        nodeRelationshipMap.putIfAbsent(node, new ConcurrentHashMap<>());

        return node;
    }

    @Override
    public Relationship addRelationship(Relationship relationship) {
        relationshipMap.put(relationship.getId(), relationship);

        // Add to source node's outgoing relationships
        addToNodeRelationships(relationship.getSource(), relationship, relationship.getType(), Direction.OUTGOING);

        // Add to target node's incoming relationships
        addToNodeRelationships(relationship.getTarget(), relationship, relationship.getType(), Direction.INCOMING);

        // Add to property indices
        for (Map.Entry<String, Object> entry : relationship.getProperties().entrySet()) {
            Map<Object, Set<Relationship>> valueMap = relationshipPropertyIndex.computeIfAbsent(
                    entry.getKey(), k -> new ConcurrentHashMap<>());
            valueMap.computeIfAbsent(entry.getValue(), k -> ConcurrentHashMap.newKeySet()).add(relationship);
        }

        return relationship;
    }

    private void addToNodeRelationships(Node node, Relationship relationship, String type, Direction direction) {
        Map<String, Map<Direction, Set<Relationship>>> typeMap =
                nodeRelationshipMap.computeIfAbsent(node, k -> new ConcurrentHashMap<>());

        Map<Direction, Set<Relationship>> directionMap =
                typeMap.computeIfAbsent(type, k -> new ConcurrentHashMap<>());

        directionMap.computeIfAbsent(direction, k -> ConcurrentHashMap.newKeySet()).add(relationship);

        // Also add to BOTH direction
        directionMap.computeIfAbsent(Direction.BOTH, k -> ConcurrentHashMap.newKeySet()).add(relationship);
    }

    @Override
    public Map<String, Map<String, Set<String>>> getRawGraph() {
        // Convert the optimized structure to the required format for backward compatibility
        Map<String, Map<String, Set<String>>> rawGraph = new HashMap<>();

        for (Node node : nodeMap.values()) {
            Map<String, Set<String>> typeMap = rawGraph.computeIfAbsent(node.getId(), k -> new HashMap<>());

            for (Relationship relationship : getOutgoingRelationships(node)) {
                typeMap.computeIfAbsent(relationship.getType(), k -> new HashSet<>())
                        .add(relationship.getTarget().getId());
            }
        }

        return rawGraph;
    }

    @Override
    public Map<Node, Map<String, Map<Direction, Set<Relationship>>>> getOptimizedGraph() {
        return Collections.unmodifiableMap(nodeRelationshipMap);
    }

    @Override
    public Optional<Node> findNodeById(String id) {
        return Optional.ofNullable(nodeMap.get(id));
    }

    @Override
    public Collection<Node> findNodesByTypeAndName(String type, String namePattern) {
        Set<Node> nodes = typeIndex.getOrDefault(type, Collections.emptySet());
        return nodes.stream()
                .filter(node -> node.getName().matches(namePattern))
                .toList();
    }

    @Override
    public Collection<Node> findNodesByLabel(String label) {
        return new ArrayList<>(labelIndex.getOrDefault(label, Collections.emptySet()));
    }

    @Override
    public Collection<Node> findNodesByProperty(String propertyKey, Object propertyValue) {
        Map<Object, Set<Node>> valueMap = nodePropertyIndex.getOrDefault(propertyKey, Collections.emptyMap());
        return new ArrayList<>(valueMap.getOrDefault(propertyValue, Collections.emptySet()));
    }

    @Override
    public Collection<Node> getAllNodes() {
        return new ArrayList<>(nodeMap.values());
    }

    @Override
    public Optional<Relationship> findRelationshipById(String id) {
        return Optional.ofNullable(relationshipMap.get(id));
    }

    @Override
    public Collection<Relationship> findRelationshipsByType(String type) {
        return getAllRelationships().stream()
                .filter(rel -> rel.getType().equals(type))
                .toList();
    }

    @Override
    public Collection<Relationship> findRelationshipsByProperty(String propertyKey, Object propertyValue) {
        Map<Object, Set<Relationship>> valueMap = relationshipPropertyIndex.getOrDefault(propertyKey, Collections.emptyMap());
        return new ArrayList<>(valueMap.getOrDefault(propertyValue, Collections.emptySet()));
    }

    @Override
    public Collection<Relationship> getAllRelationships() {
        return new ArrayList<>(relationshipMap.values());
    }

    @Override
    public Collection<Relationship> getOutgoingRelationships(Node node) {
        Map<String, Map<Direction, Set<Relationship>>> typeMap = nodeRelationshipMap.getOrDefault(node, Collections.emptyMap());

        Set<Relationship> result = new HashSet<>();
        for (Map<Direction, Set<Relationship>> directionMap : typeMap.values()) {
            Set<Relationship> outgoing = directionMap.getOrDefault(Direction.OUTGOING, Collections.emptySet());
            result.addAll(outgoing);
        }

        return result;
    }

    @Override
    public Collection<Relationship> getIncomingRelationships(Node node) {
        Map<String, Map<Direction, Set<Relationship>>> typeMap = nodeRelationshipMap.getOrDefault(node, Collections.emptyMap());

        Set<Relationship> result = new HashSet<>();
        for (Map<Direction, Set<Relationship>> directionMap : typeMap.values()) {
            Set<Relationship> incoming = directionMap.getOrDefault(Direction.INCOMING, Collections.emptySet());
            result.addAll(incoming);
        }

        return result;
    }

    @Override
    public Collection<Relationship> getRelationshipsBetween(Node node1, Node node2) {
        Collection<Relationship> outgoing = getOutgoingRelationships(node1);
        return outgoing.stream()
                .filter(rel -> rel.getTarget().equals(node2))
                .toList();
    }

    @Override
    public void exportToCypher(Path outputPath) throws IOException {
        StringBuilder builder = new StringBuilder();

        // Create nodes
        for (Node node : getAllNodes()) {
            builder.append("CREATE (")
                    .append(sanitizeId(node.getId()))
                    .append(":");

            // Add labels
            String labels = String.join(":", new ArrayList<>(node.getLabels()));
            if (labels.isEmpty()) {
                labels = node.getType(); // Use type as default label
            }
            builder.append(labels)
                    .append(" {");

            // Add properties
            List<String> props = new ArrayList<>();
            props.add("id: '" + escapeString(node.getId()) + "'");
            props.add("name: '" + escapeString(node.getName()) + "'");
            props.add("type: '" + escapeString(node.getType()) + "'");

            for (Map.Entry<String, Object> entry : node.getProperties().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String) {
                    props.add(key + ": '" + escapeString((String) value) + "'");
                } else {
                    props.add(key + ": " + value);
                }
            }

            builder.append(String.join(", ", props))
                    .append("})\n");
        }

        // Create relationships
        for (Relationship rel : getAllRelationships()) {
            builder.append("CREATE (")
                    .append(sanitizeId(rel.getSource().getId()))
                    .append(")-[:")
                    .append(rel.getType());

            // Add properties
            if (!rel.getProperties().isEmpty()) {
                builder.append(" {");
                List<String> props = new ArrayList<>();
                props.add("id: '" + escapeString(rel.getId()) + "'");

                for (Map.Entry<String, Object> entry : rel.getProperties().entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        props.add(key + ": '" + escapeString((String) value) + "'");
                    } else {
                        props.add(key + ": " + value);
                    }
                }

                builder.append(String.join(", ", props))
                        .append("}");
            }

            builder.append("]->(")
                    .append(sanitizeId(rel.getTarget().getId()))
                    .append(")\n");
        }

        // Create indices and constraints
        Set<String> labels = new HashSet<>(labelIndex.keySet());

        // Add type-based labels if not already included
        for (String type : typeIndex.keySet()) {
            labels.add(type);
        }

        for (String label : labels) {
            builder.append("CREATE INDEX ON :")
                    .append(label)
                    .append("(id)\n");

            builder.append("CREATE CONSTRAINT ON (n:")
                    .append(label)
                    .append(") ASSERT n.id IS UNIQUE\n");
        }

        Files.writeString(outputPath, builder.toString());
    }

    private String sanitizeId(String id) {
        return "n_" + id.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private String escapeString(String s) {
        return s.replace("'", "\\'");
    }

    @Override
    public void createIndex(String label, String propertyKey) {
        // In the in-memory implementation, we create the index structures
        // This would be translated to Neo4j commands when exported to Cypher

        // For now, we'll just ensure the label index exists
        labelIndex.putIfAbsent(label, ConcurrentHashMap.newKeySet());
    }

    @Override
    public void createUniqueConstraint(String label, String propertyKey) {
        // Similar to createIndex, this would be enforced when exporting to Neo4j
        // For now, we'll just ensure the label index exists
        labelIndex.putIfAbsent(label, ConcurrentHashMap.newKeySet());
    }
}