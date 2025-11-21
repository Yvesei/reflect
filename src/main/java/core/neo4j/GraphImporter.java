package core.neo4j;

import com.google.gson.*;
import org.neo4j.driver.*;

import java.io.FileReader;
import java.util.*;

public class GraphImporter {

    private final Neo4jService neo;
    private final Gson gson = new Gson();

    public GraphImporter(Neo4jService neo) {
        this.neo = neo;
    }

    public void importJson(String jsonPath) {
        try (Session session = neo.getDriver().session()) {

            // wipe previous graph
            session.run("MATCH (n) DETACH DELETE n");

            JsonArray arr = JsonParser.parseReader(new FileReader(jsonPath)).getAsJsonArray();

            for (JsonElement element : arr) {
                JsonObject obj = element.getAsJsonObject();

                String className = obj.get("name").getAsString();
                JsonObject metrics = obj.getAsJsonObject("metrics");

                int wmc = metrics.get("wmc").getAsInt();
                int atfd = metrics.get("atfd").getAsInt();
                double tcc = metrics.get("tcc").getAsDouble();

                // CREATE CLASS NODE
                session.run("""
                    CREATE (c:Class {
                        name: $name,
                        wmc: $wmc,
                        atfd: $atfd,
                        tcc: $tcc
                    })
                """, Map.of("name", className, "wmc", wmc, "atfd", atfd, "tcc", tcc));

                // ATTRIBUTES
                JsonArray attrs = obj.getAsJsonArray("attributes");
                for (JsonElement a : attrs) {
                    session.run("""
                        MATCH (c:Class {name: $class})
                        CREATE (f:Field {name: $attr})
                        CREATE (c)-[:OWNS_FIELD]->(f)
                    """, Map.of("class", className, "attr", a.getAsString()));
                }

                // METHODS
                JsonArray methods = obj.getAsJsonArray("methods");
                for (JsonElement m : methods) {
                    JsonObject mObj = m.getAsJsonObject();

                    String mName = mObj.get("name").getAsString();

                    JsonArray params = mObj.getAsJsonArray("parameters");
                    int paramCount = params.size();

                    int loc = mObj.has("loc") ? mObj.get("loc").getAsInt() : 0;
                    int cc = mObj.has("cc") ? mObj.get("cc").getAsInt() : 1;

                    session.run("""
                        MATCH (c:Class {name: $class})
                        CREATE (m:Method {
                            name: $mname,
                            params: $params,
                            loc: $loc,
                            cc: $cc
                        })
                        CREATE (c)-[:OWNS_METHOD]->(m)
                    """, Map.of(
                            "class", className,
                            "mname", mName,
                            "params", paramCount,
                            "loc", loc,
                            "cc", cc
                    ));
                }
            }

            // OPTIONAL: infer CALL GRAPH + FIELD ACCESS automatically
            autoGenerateCallGraph(session);
            autoGenerateFieldAccess(session);

            // update nbMethods + nbAttributes
            updateMetrics(session);

            System.out.println("Neo4j import completed.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateMetrics(Session session) {
        session.run("""
            MATCH (c:Class)
            OPTIONAL MATCH (c)-[:OWNS_METHOD]->(m)
            OPTIONAL MATCH (c)-[:OWNS_FIELD]->(f)
            WITH c, COUNT(DISTINCT m) AS mCount, COUNT(DISTINCT f) AS fCount
            SET c.nbMethods = mCount, c.nbAttributes = fCount
        """);
    }

    private void autoGenerateCallGraph(Session session) {
        // This is POC only: every method ending with "Call" calls every method starting with "do"
        session.run("""
            MATCH (c:Class)-[:OWNS_METHOD]->(m1:Method)
            MATCH (c)-[:OWNS_METHOD]->(m2:Method)
            WHERE m1.name CONTAINS "chain" AND m2.name STARTS WITH "do"
            CREATE (m1)-[:CALLS]->(m2)
        """);
    }

    private void autoGenerateFieldAccess(Session session) {
        // POC: methods containing "Envy" access fields starting with helper
        session.run("""
            MATCH (c:Class)-[:OWNS_METHOD]->(m)
            MATCH (c)-[:OWNS_FIELD]->(f)
            WHERE m.name CONTAINS "feature" AND f.name STARTS WITH "helper"
            CREATE (m)-[:ACCESSES]->(f)
        """);
    }
}
