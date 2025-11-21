package core.reflection;

import com.google.gson.Gson;
import core.detection.InputType;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ReflectiveAnalyzer {

    public SystemModel buildModel(File file, InputType type) throws Exception {

        SystemModel model = new SystemModel();

        switch (type) {

            case JAVA_SOURCE -> {
                File compiled = compileJavaFile(file);
                loadClassAndAnalyze(compiled, model);
            }

            case CLASS_FILE -> loadClassAndAnalyze(file, model);

            case JAR_FILE -> throw new RuntimeException("JAR support not implemented yet");

            default -> throw new RuntimeException("Unsupported input: " + type);
        }

        return model;
    }

    public void exportJson(SystemModel model, String outPath) throws Exception {
        Gson gson = new Gson();
        try (FileWriter fw = new FileWriter(outPath)) {
            gson.toJson(model.getClasses(), fw);
        }
    }
    public SystemModel buildProjectModel(File directory) throws Exception {
        SystemModel model = new SystemModel();

        // Recursively collect all .java and .class files
        List<File> files = getAllFiles(directory);

        for (File f : files) {
            InputType type = f.getName().endsWith(".java")
                    ? InputType.JAVA_SOURCE
                    : InputType.CLASS_FILE;

            buildSingleFileModel(f, type, model);
        }

        return model;
    }
    private List<File> getAllFiles(File root) {
        List<File> result = new ArrayList<>();

        File[] files = root.listFiles();
        if (files == null) return result;

        for (File f : files) {
            if (f.isDirectory()) {
                result.addAll(getAllFiles(f));
            } else if (f.getName().endsWith(".java") || f.getName().endsWith(".class")) {
                result.add(f);
            }
        }
        return result;
    }
    private void buildSingleFileModel(File file, InputType type, SystemModel model) throws Exception {

        switch (type) {
            case JAVA_SOURCE -> {
                File compiled = compileJavaFile(file);
                loadClassAndAnalyze(compiled, model);
            }
            case CLASS_FILE -> loadClassAndAnalyze(file, model);

            default -> throw new RuntimeException("Unsupported input: " + type);
        }
    }

    private File compileJavaFile(File javaFile) throws Exception {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null)
            throw new RuntimeException("No compiler available. Use JDK, not JRE.");

        int result = compiler.run(null, null, null, javaFile.getPath());

        if (result != 0)
            throw new RuntimeException("javac compilation failed");

        String className = javaFile.getName().replace(".java", ".class");
        return new File(javaFile.getParentFile(), className);
    }

    private void loadClassAndAnalyze(File classFile, SystemModel model) throws Exception {

        URL url = classFile.getParentFile().toURI().toURL();
        URLClassLoader loader = new URLClassLoader(new URL[]{url});

        String className = classFile.getName().replace(".class", "");
        Class<?> cls = loader.loadClass(className);

        SystemModel.ClassInfo info = new SystemModel.ClassInfo();
        info.name = cls.getSimpleName();

        for (var f : cls.getDeclaredFields())
            info.attributes.add(f.getName());

        for (var m : cls.getDeclaredMethods()) {
            SystemModel.MethodInfo mi = new SystemModel.MethodInfo();
            mi.name = m.getName();
            for (var p : m.getParameters())
                mi.parameters.add(p.getType().getSimpleName());
            info.methods.add(mi);
        }

        info.superClass = cls.getSuperclass() != null
                ? cls.getSuperclass().getName()
                : "java.lang.Object";

        for (var i : cls.getInterfaces())
            info.interfaces.add(i.getName());

        info.metrics.wmc = cls.getDeclaredMethods().length;
        info.metrics.atfd = info.attributes.size();
        info.metrics.tcc = info.methods.isEmpty() ? 0.0 : 1.0 / info.methods.size();

        model.addClass(info);
    }

    // ------------------------------------------------------------
    // NEW: Export in-memory model → JSON file for Neo4j
    // ------------------------------------------------------------
    public void export(SystemModel model, String path) throws Exception {
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                .setPrettyPrinting()
                .create();

        java.io.FileWriter writer = new java.io.FileWriter(path);
        gson.toJson(model.getClasses(), writer);
        writer.close();
    }
}
