package core.reflection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class SystemModel {

    // List of classes analyzed
    private final List<ClassInfo> classes = new ArrayList<>();

    public void addClass(ClassInfo info) {
        classes.add(info);
    }

    public List<ClassInfo> getClasses() {
        return classes;
    }

    // Save JSON to file output.json
    public void saveToJson(String path) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(classes, writer);
        }
    }

    // -------------------------
    // Inner class describing a class
    // -------------------------
    public static class ClassInfo {
        public String name;
        public List<String> attributes = new ArrayList<>();
        public List<MethodInfo> methods = new ArrayList<>();
        public String superClass;
        public List<String> interfaces = new ArrayList<>();

        public Metrics metrics = new Metrics();
    }

    // ------------------------
    // Inner class describing a method
    // ------------------------
    public static class MethodInfo {
        public String name;
        public List<String> parameters = new ArrayList<>();
    }

    // ------------------------
    // Inner class describing metrics
    // ------------------------
    public static class Metrics {
        public int wmc;
        public int atfd;
        public double tcc;
    }

    public String toJson() {
        return new Gson().toJson(classes);
    }
}
