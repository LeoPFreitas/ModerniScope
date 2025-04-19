package co.moderniscope.analyzer;

import co.moderniscope.analyzer.api.DependencyGraph;
import org.neo4j.driver.*;

import java.util.Map;
import java.util.Set;

/**
 * Exports a dependency graph to Neo4j.
 */
public class Neo4jDependencyExporter implements AutoCloseable {
    // Cypher query constants
    private static final String CLASS_INDEX = "CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.fqcn)";
    private static final String PACKAGE_INDEX = "CREATE INDEX IF NOT EXISTS FOR (p:Package) ON (p.name)";
    private static final String MERGE_PACKAGE = "MERGE (p:Package {name: $name})";
    private static final String MERGE_CLASS =
            "MERGE (c:Class {fqcn: $fqcn}) " +
                    "SET c.name = $name " +
                    "WITH c " +
                    "MATCH (p:Package {name: $package}) " +
                    "MERGE (c)-[:BELONGS_TO]->(p)";
    private static final String CREATE_RELATIONSHIP =
            "MATCH (from:Class {fqcn: $from}), (to:Class {fqcn: $to}) " +
                    "MERGE (from)-[r:%s]->(to)";

    private final Driver driver;

    public Neo4jDependencyExporter(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void exportGraph(DependencyGraph dependencyGraph) {
        Map<String, Map<String, Set<String>>> graph = dependencyGraph.getRawGraph();

        try (Session session = driver.session()) {
            setupSchema(session);
            exportGraphData(session, graph);
        }
    }

    private void setupSchema(Session session) {
        try (var tx = session.beginTransaction()) {
            tx.run(CLASS_INDEX);
            tx.run(PACKAGE_INDEX);
            tx.commit();
        }
    }

    private void exportGraphData(Session session, Map<String, Map<String, Set<String>>> graph) {
        try (var tx = session.beginTransaction()) {
            for (var fromEntry : graph.entrySet()) {
                String from = fromEntry.getKey();
                exportSourceNode(tx, from);
                exportRelationships(tx, from, fromEntry.getValue());
            }
            tx.commit();
        }
    }

    private void exportSourceNode(Transaction tx, String from) {
        String fromPackage = getPackage(from);
        String fromSimpleName = getSimpleName(from);

        // Create or retrieve the Package node
        tx.run(MERGE_PACKAGE, Values.parameters("name", fromPackage));

        // Create the Class node linked to its package
        tx.run(MERGE_CLASS, Values.parameters(
                "fqcn", from,
                "name", fromSimpleName,
                "package", fromPackage
        ));
    }

    private void exportRelationships(Transaction tx, String from, Map<String, Set<String>> relationships) {
        for (var toEntry : relationships.entrySet()) {
            String to = toEntry.getKey();
            exportTargetNode(tx, to);
            createRelationships(tx, from, to, toEntry.getValue());
        }
    }

    private void exportTargetNode(Transaction tx, String to) {
        String toPackage = getPackage(to);
        String toSimpleName = getSimpleName(to);

        // Create the target Package and Class nodes
        tx.run(MERGE_PACKAGE, Values.parameters("name", toPackage));
        tx.run(MERGE_CLASS, Values.parameters(
                "fqcn", to,
                "name", toSimpleName,
                "package", toPackage
        ));
    }

    private void createRelationships(Transaction tx, String from, String to, Set<String> relationshipTypes) {
        for (String relType : relationshipTypes) {
            String sanitizedRelType = sanitizeRelationshipType(relType);
            String query = String.format(CREATE_RELATIONSHIP, sanitizedRelType);
            tx.run(query, Values.parameters("from", from, "to", to));
        }
    }

    private String sanitizeRelationshipType(String relType) {
        // Neo4j relationship types must not contain spaces and certain special characters
        return relType.replaceAll("[^A-Za-z0-9_]", "_").toUpperCase();
    }

    private String getPackage(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? fullyQualifiedName.substring(0, lastDot) : "";
    }

    private String getSimpleName(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
    }

    @Override
    public void close() {
        driver.close();
    }
}