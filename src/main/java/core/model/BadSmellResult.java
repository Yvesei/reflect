package core.model;

import java.util.ArrayList;
import java.util.List;

public class BadSmellResult {

    private String className;
    private List<String> smells = new ArrayList<>();

    public BadSmellResult(String clsName) {
        this.className = clsName;
    }

    public List<String> getSmells() { return smells; }
    public String getClassName() { return className; }

    public void addSmell(String smell) {
        smells.add(smell);
    }
}
