
package co.moderniscope.analyzer.api.neo4j;

/**
 * Enum representing the direction of relationships.
 */
public enum Direction {
    /**
     * Incoming relationships (pointing to the node)
     */
    INCOMING,

    /**
     * Outgoing relationships (pointing from the node)
     */
    OUTGOING,

    /**
     * Both incoming and outgoing relationships
     */
    BOTH
}