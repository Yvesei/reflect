package core.neo4j;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jService {

    private final Driver driver;

    public Neo4jService(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public Driver getDriver() {
        return driver;
    }

    public void close() {
        driver.close();
    }
}
