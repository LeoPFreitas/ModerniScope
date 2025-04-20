package co.moderniscope.analyzer.graph;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A generic property graph data structure that can represent various relationships between nodes.
 * This interface follows the property graph model where:
 * - Nodes represent entities
 * - Edges represent relationships between nodes
 * - Both nodes and edges can have properties (key-value pairs)
 * - Nodes can have labels representing types or categories
 * - Edges have specific relationship types
 *
 * @param <N> Node type
 * @param <E> Edge type
 */
public interface Graph<N, E> {
    /**
     * Adds a node to the graph.
     *
     * @param node The node to add
     * @return true if the node was added, false if it already existed
     */
    boolean addNode(N node);

    /**
     * Adds a node with properties and labels.
     *
     * @param node       The node to add
     * @param properties Properties associated with the node
     * @param labels     Categories or types for this node
     * @return true if the node was added or updated
     */
    boolean addNode(N node, Map<String, Object> properties, String... labels);

    /**
     * Adds an edge between nodes with a specific relationship type.
     *
     * @param source Source node
     * @param target Target node
     * @param edge   Edge with relationship type information
     * @return true if the edge was added
     */
    boolean addEdge(N source, N target, E edge);

    /**
     * Adds an edge with properties.
     *
     * @param source     Source node
     * @param target     Target node
     * @param edge       Edge with relationship type information
     * @param properties Properties associated with the edge
     * @return true if the edge was added
     */
    boolean addEdge(N source, N target, E edge, Map<String, Object> properties);

    /**
     * Gets all nodes in the graph.
     *
     * @return Set of all nodes
     */
    Set<N> getNodes();

    /**
     * Gets nodes with a specific label.
     *
     * @param label The label to filter nodes by
     * @return Set of nodes with the given label
     */
    Set<N> getNodesByLabel(String label);

    /**
     * Gets node properties.
     *
     * @param node The node
     * @return Map of property name to value
     */
    Map<String, Object> getNodeProperties(N node);

    /**
     * Gets outgoing edges from a node.
     *
     * @param source Source node
     * @return Map of target nodes to edges
     */
    Map<N, Set<E>> getOutgoingEdges(N source);

    /**
     * Gets outgoing edges of a specific relationship type from a node.
     *
     * @param source           Source node
     * @param relationshipType Type of relationship to filter by
     * @return Map of target nodes to edges
     */
    Map<N, Set<E>> getOutgoingEdges(N source, String relationshipType);

    /**
     * Gets incoming edges to a node.
     *
     * @param target Target node
     * @return Map of source nodes to edges
     */
    Map<N, Set<E>> getIncomingEdges(N target);

    /**
     * Gets incoming edges of a specific relationship type to a node.
     *
     * @param target           Target node
     * @param relationshipType Type of relationship to filter by
     * @return Map of source nodes to edges
     */
    Map<N, Set<E>> getIncomingEdges(N target, String relationshipType);

    /**
     * Gets properties for an edge.
     *
     * @param source Source node
     * @param target Target node
     * @param edge   Edge information
     * @return Map of property name to value
     */
    Map<String, Object> getEdgeProperties(N source, N target, E edge);

    /**
     * Finds a path between nodes.
     *
     * @param start Starting node
     * @param end   Ending node
     * @return Optional containing an iterable of nodes forming the path, empty if no path exists
     */
    Optional<Iterable<N>> findPath(N start, N end);

    /**
     * Finds a path between nodes using specific relationship types.
     *
     * @param start             Starting node
     * @param end               Ending node
     * @param relationshipTypes Types of relationships to traverse
     * @return Optional containing an iterable of nodes forming the path, empty if no path exists
     */
    Optional<Iterable<N>> findPath(N start, N end, String... relationshipTypes);

    /**
     * Performs depth-first traversal from a start node.
     *
     * @param start   Starting node
     * @param visitor Visitor to apply to each node
     */
    void traverseDepthFirst(N start, NodeVisitor<N> visitor);

    /**
     * Performs breadth-first traversal from a start node.
     *
     * @param start   Starting node
     * @param visitor Visitor to apply to each node
     */
    void traverseBreadthFirst(N start, NodeVisitor<N> visitor);

    /**
     * Functional interface for graph traversal.
     */
    interface NodeVisitor<N> {
        /**
         * Visits a node during traversal.
         *
         * @param node  The current node
         * @param depth Depth in the traversal
         * @return true to continue traversal, false to stop
         */
        boolean visit(N node, int depth);
    }
}