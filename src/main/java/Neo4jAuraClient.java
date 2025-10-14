import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

public class Neo4jAuraClient {
    private final Driver driver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Neo4jAuraClient(String uri, String username, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public void close() {
        driver.close();
    }

    public void createClassNodesFromJson(String jsonData) {
        try (Session session = driver.session()) {
            JsonNode classes = objectMapper.readTree(jsonData);

            // Clear existing data (optional)
            session.run("MATCH (n) DETACH DELETE n");

            for (JsonNode classNode : classes) {
                String className = classNode.get("className").asText();

                // Create Class node
                session.run(
                        "CREATE (c:Class {name: $className, fullName: $className})",
                        Map.of("className", className)
                );

                // Create Field nodes and relationships
                JsonNode fields = classNode.get("fields");
                for (JsonNode field : fields) {
                    String fieldName = field.asText();
                    session.run(
                            "MATCH (c:Class {name: $className}) " +
                                    "CREATE (f:Field {name: $fieldName}) " +
                                    "CREATE (c)-[:HAS_FIELD]->(f)",
                            Map.of("className", className, "fieldName", fieldName)
                    );
                }

                // Create Method nodes and relationships
                JsonNode methods = classNode.get("methods");
                for (JsonNode method : methods) {
                    String methodName = method.asText();
                    session.run(
                            "MATCH (c:Class {name: $className}) " +
                                    "CREATE (m:Method {name: $methodName}) " +
                                    "CREATE (c)-[:HAS_METHOD]->(m)",
                            Map.of("className", className, "methodName", methodName)
                    );
                }

                // Create Constructor nodes and relationships
                JsonNode constructors = classNode.get("constructors");
                for (JsonNode constructor : constructors) {
                    String constructorSig = constructor.asText();
                    session.run(
                            "MATCH (c:Class {name: $className}) " +
                                    "CREATE (con:Constructor {signature: $signature}) " +
                                    "CREATE (c)-[:HAS_CONSTRUCTOR]->(con)",
                            Map.of("className", className, "signature", constructorSig)
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Enhanced method with additional information from EnhancedReflectUtil
    public void createEnhancedClassStructure(String jarPath) {
        try (Session session = driver.session()) {
            List<Class<?>> classes = ReflectUtil.loadClassesFromJar(jarPath);

            // Clear existing data
            session.run("MATCH (n) DETACH DELETE n");

            for (Class<?> clazz : classes) {
                EnhancedReflectUtil util = new EnhancedReflectUtil(clazz);
                String className = clazz.getName();

                // Create Class node
                session.run(
                        "CREATE (c:Class {name: $name, fullName: $fullName})",
                        Map.of("name", clazz.getSimpleName(), "fullName", className)
                );

                // Create Field nodes with visibility
                Map<String, String> fieldVisibilities = util.getFieldVisibilities();
                for (Map.Entry<String, String> field : fieldVisibilities.entrySet()) {
                    session.run(
                            "MATCH (c:Class {fullName: $className}) " +
                                    "CREATE (f:Field {name: $fieldName, visibility: $visibility}) " +
                                    "CREATE (c)-[:HAS_FIELD]->(f)",
                            Map.of("className", className,
                                    "fieldName", field.getKey(),
                                    "visibility", field.getValue())
                    );
                }

                // Create Method nodes with line numbers
                Map<String, Integer> methodLineNumbers = util.getMethodLineNumbers();
                Map<String, List<String>> methodCalls = util.getMethodCalls();

                for (String methodName : methodLineNumbers.keySet()) {
                    Integer lineNumber = methodLineNumbers.get(methodName);
                    List<String> calls = methodCalls.get(methodName);

                    session.run(
                            "MATCH (c:Class {fullName: $className}) " +
                                    "CREATE (m:Method {name: $methodName, lineNumber: $lineNumber}) " +
                                    "CREATE (c)-[:HAS_METHOD]->(m)",
                            Map.of("className", className,
                                    "methodName", methodName,
                                    "lineNumber", lineNumber != null ? lineNumber : -1)
                    );

                    // Create CALLS relationships between methods
                    if (calls != null) {
                        for (String calledMethod : calls) {
                            session.run(
                                    "MATCH (m1:Method {name: $callerMethod}), (c:Class {fullName: $className}) " +
                                            "WHERE (c)-[:HAS_METHOD]->(m1) " +
                                            "MERGE (m2:Method {name: $calledMethod}) " +
                                            "CREATE (m1)-[:CALLS]->(m2)",
                                    Map.of("callerMethod", methodName,
                                            "calledMethod", calledMethod,
                                            "className", className)
                            );
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}