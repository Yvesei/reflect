import org.neo4j.driver.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Neo4jAuraService {
    private final Driver driver;

    public Neo4jAuraService(String uri, String username, String password) {
        this.driver = GraphDatabase.driver(uri,
                AuthTokens.basic(username, password),
                Config.builder()
                        .withMaxConnectionLifetime(30, TimeUnit.MINUTES)
                        .withMaxConnectionPoolSize(50)
                        .withConnectionAcquisitionTimeout(2, TimeUnit.MINUTES)
                        .build());
    }

    public void close() {
        driver.close();
    }

    public void createClassNode(String className, String packageName,
                                List<String> methods, List<String> fields,
                                List<String> interfaces, String superClass) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                String query = """
                    MERGE (c:Class {name: $className})
                    SET c.package = $packageName,
                        c.methods = $methods,
                        c.fields = $fields,
                        c.interfaces = $interfaces,
                        c.superClass = $superClass
                    WITH c
                    MERGE (p:Package {name: $packageName})
                    MERGE (c)-[:BELONGS_TO]->(p)
                    """;

                Map<String, Object> params = Map.of(
                        "className", className,
                        "packageName", packageName,
                        "methods", methods != null ? methods : List.of(),
                        "fields", fields != null ? fields : List.of(),
                        "interfaces", interfaces != null ? interfaces : List.of(),
                        "superClass", superClass != null ? superClass : ""
                );

                tx.run(query, params);
                return null;
            });
        }
    }

    public void createInheritanceRelationship(String subclass, String superclass) {
        if (superclass == null || superclass.isEmpty()) return;

        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                String query = """
                    MATCH (sub:Class {name: $subclass})
                    MATCH (super:Class {name: $superclass})
                    MERGE (sub)-[:EXTENDS]->(super)
                    """;

                tx.run(query, Map.of("subclass", subclass, "superclass", superclass));
                return null;
            });
        }
    }

    public void createImplementsRelationship(String className, String interfaceName) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                String query = """
                    MATCH (c:Class {name: $className})
                    MATCH (i:Interface {name: $interfaceName})
                    MERGE (c)-[:IMPLEMENTS]->(i)
                    """;

                tx.run(query, Map.of("className", className, "interfaceName", interfaceName));
                return null;
            });
        }
    }

    public void createInterfaceNode(String interfaceName, String packageName) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                String query = """
                    MERGE (i:Interface {name: $interfaceName})
                    SET i.package = $packageName
                    WITH i
                    MERGE (p:Package {name: $packageName})
                    MERGE (i)-[:BELONGS_TO]->(p)
                    """;

                tx.run(query, Map.of("interfaceName", interfaceName, "packageName", packageName));
                return null;
            });
        }
    }

    // Additional utility methods
    public void clearDatabase() {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
        }
    }

    public void createMethodNode(String className, String methodName, List<String> parameters,
                                 String returnType, String visibility) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                String query = """
                    MATCH (c:Class {name: $className})
                    MERGE (m:Method {name: $methodName, signature: $signature})
                    SET m.returnType = $returnType,
                        m.visibility = $visibility
                    MERGE (c)-[:HAS_METHOD]->(m)
                    """;

                String signature = methodName + "(" + String.join(",", parameters) + ")";

                Map<String, Object> params = Map.of(
                        "className", className,
                        "methodName", methodName,
                        "signature", signature,
                        "returnType", returnType != null ? returnType : "void",
                        "visibility", visibility != null ? visibility : "public"
                );

                tx.run(query, params);
                return null;
            });
        }
    }

    public void createFieldNode(String className, String fieldName, String fieldType, String visibility) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                String query = """
                    MATCH (c:Class {name: $className})
                    MERGE (f:Field {name: $fieldName})
                    SET f.type = $fieldType,
                        f.visibility = $visibility
                    MERGE (c)-[:HAS_FIELD]->(f)
                    """;

                Map<String, Object> params = Map.of(
                        "className", className,
                        "fieldName", fieldName,
                        "fieldType", fieldType != null ? fieldType : "Object",
                        "visibility", visibility != null ? visibility : "private"
                );

                tx.run(query, params);
                return null;
            });
        }
    }
}