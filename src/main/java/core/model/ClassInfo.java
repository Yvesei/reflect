package core.model;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo {

    private String name;
    private List<String> attributes = new ArrayList<>();
    private List<MethodInfo> methods = new ArrayList<>();
    private String superClass;
    private List<String> interfaces = new ArrayList<>();
    private MetricInfo metrics = new MetricInfo();

    public String getName() { return name; }
    public void setName(String n) { this.name = n; }

    public List<String> getAttributes() { return attributes; }
    public List<MethodInfo> getMethods() { return methods; }

    public String getSuperClass() { return superClass; }
    public void setSuperClass(String superClass) { this.superClass = superClass; }

    public List<String> getInterfaces() { return interfaces; }

    public MetricInfo getMetrics() { return metrics; }
}
