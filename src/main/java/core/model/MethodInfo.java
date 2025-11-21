package core.model;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {

    private String name;
    private List<String> parameters = new ArrayList<>();
    private List<String> accessedAttributes = new ArrayList<>();
    private List<String> calledMethods = new ArrayList<>();
    private int loc = 0;
    private int cc = 1;

    public MethodInfo() {}

    public MethodInfo(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getParameters() { return parameters; }
    public void addParameter(String p) { parameters.add(p); }

    public List<String> getAccessedAttributes() { return accessedAttributes; }
    public List<String> getCalledMethods() { return calledMethods; }

    public int getLoc() { return loc; }
    public void setLoc(int loc) { this.loc = loc; }

    public int getCc() { return cc; }
    public void setCc(int cc) { this.cc = cc; }
}
