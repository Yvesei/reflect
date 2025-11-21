package core.neo4j;

import org.neo4j.driver.Session;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

import java.util.ArrayList;
import java.util.List;

public class BadSmellDetector {

    private final Neo4jService neo;

    public BadSmellDetector(Neo4jService neo) {
        this.neo = neo;
    }

    // Generic helper
    private List<String> runQuery(String cypher) {
        List<String> out = new ArrayList<>();

        try (Session session = neo.getDriver().session()) {
            Result r = session.run(cypher);
            for (Record rec : r.list()) {
                String name = rec.get("name").asString();
                String detail = rec.containsKey("detail") ? rec.get("detail").asString() : "";
                out.add(name + "   [" + detail + "]");
            }
        }
        return out;
    }

    // 1. GOD CLASS
    public List<String> detectGodClass() {
        return runQuery("""
            MATCH (c:Class)
            WHERE c.wmc > 20 AND c.atfd > 5 AND c.tcc < 0.33
            RETURN 
                c.name AS name,
                "WMC=" + c.wmc +
                ", ATFD=" + c.atfd +
                ", TCC=" + c.tcc AS detail
        """);
    }

    // 2. LARGE CLASS
    public List<String> detectLargeClass() {
        return runQuery("""
            MATCH (c:Class)
            WHERE c.nbMethods > 12 OR c.nbAttributes > 10
            RETURN 
                c.name AS name,
                "Methods=" + c.nbMethods +
                ", Attributes=" + c.nbAttributes AS detail
        """);
    }

    // 3. LONG METHOD
    public List<String> detectLongMethod() {
        return runQuery("""
            MATCH (c:Class)-[:OWNS_METHOD]->(m:Method)
            WHERE m.loc > 50 OR m.cc > 10
            RETURN 
                c.name + "." + m.name AS name,
                "LOC=" + m.loc +
                ", CC=" + m.cc AS detail
        """);
    }

    // 4. LONG PARAMETER LIST
    public List<String> detectLongParameterList() {
        return runQuery("""
            MATCH (c:Class)-[:OWNS_METHOD]->(m:Method)
            WHERE m.params > 5
            RETURN 
                c.name + "." + m.name AS name,
                "Params=" + m.params AS detail
        """);
    }

    // 5. FEATURE ENVY
    public List<String> detectFeatureEnvy() {
        return runQuery("""
            MATCH (c:Class)-[:OWNS_METHOD]->(m:Method)
            OPTIONAL MATCH (m)-[:ACCESSES]->(a1)<-[:OWNS_FIELD]-(c)
            WITH c, m, COUNT(a1) AS ownAccess
            OPTIONAL MATCH (m)-[:ACCESSES]->(a2)<-[:OWNS_FIELD]-(other:Class)
            WHERE other <> c
            WITH c, m, ownAccess, other, COUNT(a2) AS foreignAccess
            WHERE foreignAccess > ownAccess AND foreignAccess > 0
            RETURN 
                c.name + "." + m.name + " → " + other.name AS name,
                "ForeignAccess=" + foreignAccess +
                ", OwnAccess=" + ownAccess +
                ", EnviedClass=" + other.name AS detail
        """);
    }

    // 6. DATA CLASS
    public List<String> detectDataClass() {
        return runQuery("""
            MATCH (c:Class)
            MATCH (c)-[:OWNS_METHOD]->(m:Method)
            WITH c, COLLECT(m) AS methods
            WHERE ALL(m IN methods WHERE m.cc < 2 AND m.loc < 10)
            RETURN 
                c.name AS name,
                "Methods=" + size(methods) + ", Behavior=weak" AS detail
        """);
    }

    // 7. MIDDLE MAN
    public List<String> detectMiddleMan() {
        return runQuery("""
            MATCH (c:Class)-[:OWNS_METHOD]->(m:Method)
            OPTIONAL MATCH (m)-[:CALLS]->(x)
            WITH c, m, COUNT(x) AS delegateCount
            WITH c, SUM(CASE WHEN delegateCount > 0 THEN 1 ELSE 0 END) AS delegated,
                 COUNT(m) AS total
            WHERE delegated > total * 0.6
            RETURN 
                c.name AS name,
                "Delegating=" + delegated +
                "/" + total AS detail
        """);
    }

    // 8. MESSAGE CHAINS
    public List<String> detectMessageChains() {
        return runQuery("""
            MATCH (m1:Method)-[:CALLS]->(m2:Method)-[:CALLS]->(m3:Method)
            RETURN 
                m1.name AS name,
                "Chain=m1 → " + m2.name + " → " + m3.name AS detail
        """);
    }

    // 9. SHOTGUN SURGERY
    public List<String> detectShotgunSurgery() {
        return runQuery("""
            MATCH (caller:Method)-[:CALLS]->(target:Method)
            MATCH (c:Class)-[:OWNS_METHOD]->(target)
            WITH c, COUNT(DISTINCT caller) AS impact
            WHERE impact > 8
            RETURN 
                c.name AS name,
                "Callers=" + impact AS detail
        """);
    }

    // 10. DIVERGENT CHANGE
    public List<String> detectDivergentChange() {
        return runQuery("""
            MATCH (c:Class)
            WHERE c.tcc < 0.2 AND c.nbMethods > 8
            RETURN 
                c.name AS name,
                "TCC=" + c.tcc +
                ", Methods=" + c.nbMethods AS detail
        """);
    }
}
