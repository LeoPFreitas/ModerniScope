package co.moderniscope.analyzer;

import co.moderniscope.analyzer.api.DependencyGraph;
import org.neo4j.driver.*;

import java.util.Map;
import java.util.Set;

/**
 * Exports a dependency graph to Neo4j.
 */
public class Neo4jDependencyExporter implements AutoCloseable {
    private final Driver driver;

    public Neo4jDependencyExporter(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void exportGraph(DependencyGraph dependencyGraph) {
        Map<String, Map<String, Set<String>>> graph = dependencyGraph.getRawGraph();

        try (Session session = driver.session()) {
            // Create necessary indexes and constraints
            setupSchema(session);

            // Write the graph data
            session.writeTransaction(tx -> {
                for (var fromEntry : graph.entrySet()) {
                    String from = fromEntry.getKey();
                    String fromPackage = getPackage(from);
                    String fromSimpleName = getSimpleName(from);

                    // Create or retrieve the Package node
                    tx.run(
                            "MERGE (p:Package {name: $name})",
                            Values.parameters("name", fromPackage)
                    );

                    // Create the Class node linked to its package
                    tx.run(
                            "MERGE (c:Class {fqcn: $fqcn}) " +
                                    "SET c.name = $name " +
                                    "WITH c " +
                                    "MATCH (p:Package {name: $package}) " +
                                    "MERGE (c)-[:BELONGS_TO]->(p)",
                            Values.parameters(
                                    "fqcn", from,
                                    "name", fromSimpleName,
                                    "package", fromPackage
                            )
                    );

                    // Create relationships to other classes
                    for (var toEntry : fromEntry.getValue().entrySet()) {
                        String to = toEntry.getKey();
                        String toPackage = getPackage(to);
                        String toSimpleName = getSimpleName(to);

                        // Create the target Package and Class nodes
                        tx.run(
                                "MERGE (p:Package {name: $name})",
                                Values.parameters("name", toPackage)
                        );

                        tx.run(
                                "MERGE (c:Class {fqcn: $fqcn}) " +
                                        "SET c.name = $name " +
                                        "WITH c " +
                                        "MATCH (p:Package {name: $package}) " +
                                        "MERGE (c)-[:BELONGS_TO]->(p)",
                                Values.parameters(
                                        "fqcn", to,
                                        "name", toSimpleName,
                                        "package", toPackage
                                )
                        );

                        // Create the relationships between the classes
                        for (String relType : toEntry.getValue()) {
                            // Sanitize relationship type for Neo4j
                            String sanitizedRelType = sanitizeRelationshipType(relType);

                            tx.run(
                                    "MATCH (from:Class {fqcn: $from}), (to:Class {fqcn: $to}) " +
                                            "MERGE (from)-[r:" + sanitizedRelType + "]->(to)",
                                    Values.parameters("from", from, "to", to)
                            );
                        }
                    }
                }
                return null;
            });
        }
    }

    private static final String CLASS_INDEX = "CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.fqcn)";
    private static final String PACKAGE_INDEX = "CREATE INDEX IF NOT EXISTS FOR (p:Package) ON (p.name)";

    private void setupSchema(Session session) {
        try (var tx = session.beginTransaction()) {
            // Create indexes for faster lookups
            tx.run(CLASS_INDEX);
            tx.run(PACKAGE_INDEX);
            tx.commit();
        }
    }

    private String sanitizeRelationshipType(String relType) {
        // Neo4j relationship types must not contain spaces and certain special characters
        // Replace spaces with underscores and remove any invalid characters
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