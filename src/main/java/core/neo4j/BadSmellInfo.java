package core.neo4j;

import java.util.Map;

public class BadSmellInfo {

    private String name;
    private String rule;
    private String reason;
    private Map<String, Object> metrics;
    private String query;

    public String getName() { return name; }
    public String getRule() { return rule; }
    public String getReason() { return reason; }
    public Map<String, Object> getMetrics() { return metrics; }
    public String getQuery() { return query; }

    public void setName(String name) { this.name = name; }
    public void setRule(String rule) { this.rule = rule; }
    public void setReason(String reason) { this.reason = reason; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
    public void setQuery(String query) { this.query = query; }

    @Override
    public String toString() {
        return name + " [" + rule + "]";
    }
}
