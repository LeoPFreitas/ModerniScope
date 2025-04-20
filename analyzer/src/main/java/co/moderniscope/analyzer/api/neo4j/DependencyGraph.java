package co.moderniscope.analyzer.api;

import co.moderniscope.analyzer.api.neo4j.Direction;
import co.moderniscope.analyzer.api.neo4j.Node;
import co.moderniscope.analyzer.api.neo4j.Relationship;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a dependency graph that can be built, queried, and exported.
 */
public interface DependencyGraph {
    /**
     * Adds a node to the graph.
     *
     * @param node The node to add
     * @return The added node
     */
    Node addNode(Node node);

    /**
     * Adds a relationship to the graph.
     *
     * @param relationship The relationship to add
     * @return The added relationship
     */
    Relationship addRelationship(Relationship relationship);

    /**
     * Gets the raw graph data structure.
     *
     * @return Map representing the graph
     */
    Map<String, Map<String, Set<String>>> getRawGraph();

    /**
     * Gets the optimized graph structure for Neo4j-like traversal.
     *
     * @return Map of nodes to their relationships by type and direction
     */
    Map<Node, Map<String, Map<Direction, Set<Relationship>>>> getOptimizedGraph();

    /**
     * Finds a node by its ID.
     *
     * @param id The node ID
     * @return Optional containing the node if found, empty otherwise
     */
    default Optional<Node> findNodeById(String id) {
        return getAllNodes().stream()
                .filter(node -> node.getId().equals(id))
                .findFirst();
    }

    /**
     * Finds nodes by type and name pattern.
     *
     * @param type        The node type
     * @param namePattern The name pattern to match
     * @return Collection of matching nodes
     */
    default Collection<Node> findNodesByTypeAndName(String type, String namePattern) {
        return getAllNodes().stream()
                .filter(node -> node.getType().equals(type) && node.getName().matches(namePattern))
                .toList();
    }

    /**
     * Finds nodes by label.
     *
     * @param label The label to match
     * @return Collection of nodes with the specified label
     */
    default Collection<Node> findNodesByLabel(String label) {
        return getAllNodes().stream()
                .filter(node -> node.hasLabel(label))
                .toList();
    }

    /**
     * Finds nodes by property value.
     *
     * @param propertyKey   The property key
     * @param propertyValue The property value to match
     * @return Collection of nodes with the matching property
     */
    default Collection<Node> findNodesByProperty(String propertyKey, Object propertyValue) {
        return getAllNodes().stream()
                .filter(node -> node.hasProperty(propertyKey) &&
                        propertyValue.equals(node.getProperty(propertyKey)))
                .toList();
    }

    /**
     * Gets all nodes in the graph.
     *
     * @return Collection of all nodes
     */
    default Collection<Node> getAllNodes() {
        return getRawGraph().values().stream()
                .flatMap(map -> map.values().stream())
                .flatMap(Collection::stream)
                .map(this::findNodeById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * Finds a relationship by its ID.
     *
     * @param id The relationship ID
     * @return Optional containing the relationship if found, empty otherwise
     */
    default Optional<Relationship> findRelationshipById(String id) {
        return getAllRelationships().stream()
                .filter(rel -> rel.getId().equals(id))
                .findFirst();
    }

    /**
     * Finds relationships by type.
     *
     * @param type The relationship type
     * @return Collection of relationships of the specified type
     */
    default Collection<Relationship> findRelationshipsByType(String type) {
        return getAllRelationships().stream()
                .filter(rel -> rel.getType().equals(type))
                .toList();
    }

    /**
     * Finds relationships by property value.
     *
     * @param propertyKey   The property key
     * @param propertyValue The property value to match
     * @return Collection of relationships with the matching property
     */
    default Collection<Relationship> findRelationshipsByProperty(String propertyKey, Object propertyValue) {
        return getAllRelationships().stream()
                .filter(rel -> rel.hasProperty(propertyKey) &&
                        propertyValue.equals(rel.getProperty(propertyKey)))
                .toList();
    }

    /**
     * Gets all relationships in the graph.
     *
     * @return Collection of all relationships
     */
    default Collection<Relationship> getAllRelationships() {
        return getRawGraph().values().stream()
                .flatMap(map -> map.values().stream())
                .flatMap(Collection::stream)
                .map(this::findRelationshipById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * Gets all outgoing relationships from a node.
     *
     * @param node The source node
     * @return Collection of outgoing relationships
     */
    default Collection<Relationship> getOutgoingRelationships(Node node) {
        return getAllRelationships().stream()
                .filter(rel -> rel.getSource().equals(node))
                .toList();
    }

    /**
     * Gets all incoming relationships to a node.
     *
     * @param node The target node
     * @return Collection of incoming relationships
     */
    default Collection<Relationship> getIncomingRelationships(Node node) {
        return getAllRelationships().stream()
                .filter(rel -> rel.getTarget().equals(node))
                .toList();
    }

    /**
     * Gets relationships between two nodes.
     *
     * @param node1 The first node
     * @param node2 The second node
     * @return Collection of relationships between the nodes
     */
    default Collection<Relationship> getRelationshipsBetween(Node node1, Node node2) {
        return getAllRelationships().stream()
                .filter(rel -> rel.isBetween(node1, node2))
                .toList();
    }

    /**
     * Executes a Cypher-like query on the graph.
     *
     * @param query The query string in a simplified Cypher format
     * @return Map of result variables to their values
     */
    default Map<String, Collection<Object>> executeQuery(String query) {
        // This would be implemented by a concrete class
        throw new UnsupportedOperationException("Query execution not implemented in this graph");
    }

    /**
     * Calculates the impact of a change to a node in the graph.
     *
     * @param node The node to assess
     * @return A map of impacted nodes to impact scores
     */
    default Map<Node, Double> calculateImpact(Node node) {
        return Map.of();
    }

    /**
     * Calculates the risk score for a potential change.
     *
     * @param node The node to assess
     * @return A risk score between 0.0 (no risk) and 1.0 (high risk)
     */
    default double calculateRiskScore(Node node) {
        return 0.0;
    }

    /**
     * Identifies teams that would be impacted by a change.
     *
     * @param node The node to assess
     * @return Set of team nodes that would be impacted
     */
    default Set<Node> identifyImpactedTeams(Node node) {
        return Set.of();
    }

    /**
     * Exports the graph to DOT format.
     *
     * @param outputPath Path to write the DOT file
     * @throws IOException If an error occurs during writing
     */
    default void exportToDot(Path outputPath) throws IOException {
    }

    /**
     * Exports the graph to JSON format.
     *
     * @param outputPath Path to write the JSON file
     * @throws IOException If an error occurs during writing
     */
    default void exportToJson(Path outputPath) throws IOException {
    }

    /**
     * Exports the graph to Neo4j Cypher statements.
     *
     * @param outputPath Path to write the Cypher file
     * @throws IOException If an error occurs during writing
     */
    default void exportToCypher(Path outputPath) throws IOException {
    }

    /**
     * Creates an index on a node property.
     *
     * @param label       The node label
     * @param propertyKey The property key to index
     */
    default void createIndex(String label, String propertyKey) {
    }

    /**
     * Creates a uniqueness constraint on a node property.
     *
     * @param label       The node label
     * @param propertyKey The property key that must be unique
     */
    default void createUniqueConstraint(String label, String propertyKey) {
    }
}