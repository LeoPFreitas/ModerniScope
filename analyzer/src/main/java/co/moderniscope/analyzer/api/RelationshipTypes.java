package co.moderniscope.analyzer.api;

/**
 * Standardized relationship types for dependency graphs across languages.
 */
public final class RelationshipTypes {
    // General relationships
    public static final String DEPENDS_ON = "DEPENDS_ON";
    public static final String CONTAINS = "CONTAINS";
    public static final String IMPORTS = "IMPORTS";

    // Object-oriented relationships
    public static final String EXTENDS = "EXTENDS";
    public static final String IMPLEMENTS = "IMPLEMENTS";
    public static final String HAS_FIELD = "HAS_FIELD";
    public static final String INSTANTIATES = "INSTANTIATES";
    public static final String CALLS_METHOD = "CALLS_METHOD";
    public static final String REFERENCES = "REFERENCES";

    // Module relationships
    public static final String REQUIRES = "REQUIRES";
    public static final String EXPORTS = "EXPORTS";

    // Prevent instantiation
    private RelationshipTypes() {
    }
}