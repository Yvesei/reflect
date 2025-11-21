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
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ReflectiveAnalyzer {

    // ----------------------------------------------------
    // MAIN ENTRY: Build model from file type
    // ----------------------------------------------------
    public SystemModel buildModel(File file, InputType type) throws Exception {

        SystemModel model = new SystemModel();

        switch (type) {

            case JAVA_SOURCE -> {
                File compiled = compileJavaFile(file);
                loadClassAndAnalyze(compiled, model);
            }

            case CLASS_FILE ->
                    loadClassAndAnalyze(file, model);

            case JAR_FILE ->
                    model = analyzeJarFile(file);

            default -> throw new RuntimeException("Unsupported input type: " + type);
        }

        return model;
    }

    // ----------------------------------------------------
    // FULL PROJECT MODE — recursively parse folder
    // ----------------------------------------------------
    public SystemModel buildProjectModel(File root) throws Exception {

        SystemModel model = new SystemModel();

        List<File> files = getAllFiles(root);

        for (File f : files) {

            InputType type =
                    f.getName().endsWith(".java") ? InputType.JAVA_SOURCE :
                            f.getName().endsWith(".class") ? InputType.CLASS_FILE :
                                    f.getName().endsWith(".jar")   ? InputType.JAR_FILE   :
                                            null;

            if (type == null)
                continue;

            SystemModel partial = buildModel(f, type);
            model.getClasses().addAll(partial.getClasses());
        }

        return model;
    }

    // ----------------------------------------------------
    // ANALYZE .JAR FILE
    // ----------------------------------------------------
    private SystemModel analyzeJarFile(File jarFile) throws Exception {

        SystemModel model = new SystemModel();

        JarFile jar = new JarFile(jarFile);
        URLClassLoader loader =
                new URLClassLoader(new URL[]{ jarFile.toURI().toURL() });

        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if (!entry.getName().endsWith(".class"))
                continue;

            String className = entry.getName()
                    .replace("/", ".")
                    .replace(".class", "");

            try {
                Class<?> cls = loader.loadClass(className);
                analyzeClassWithReflection(cls, model);

            } catch (Throwable ignored) {}
        }

        jar.close();
        return model;
    }

    // ----------------------------------------------------
    // LOAD .class WITH REFLECTION
    // ----------------------------------------------------
    private void loadClassAndAnalyze(File classFile, SystemModel model) throws Exception {

        URL url = classFile.getParentFile().toURI().toURL();
        URLClassLoader loader = new URLClassLoader(new URL[]{url});

        String className = classFile.getName().replace(".class", "");

        Class<?> cls = loader.loadClass(className);
        analyzeClassWithReflection(cls, model);
    }

    // ----------------------------------------------------
    // REFLECTION PARSER (for class or jar)
    // ----------------------------------------------------
    private void analyzeClassWithReflection(Class<?> cls, SystemModel model) {

        SystemModel.ClassInfo info = new SystemModel.ClassInfo();
        info.name = cls.getName();

        for (var f : cls.getDeclaredFields())
            info.attributes.add(f.getName());

        for (var m : cls.getDeclaredMethods()) {
            SystemModel.MethodInfo mi = new SystemModel.MethodInfo();
            mi.name = m.getName();
            for (var p : m.getParameters())
                mi.parameters.add(p.getType().getSimpleName());
            info.methods.add(mi);
        }

        info.superClass = (cls.getSuperclass() != null)
                ? cls.getSuperclass().getName()
                : "java.lang.Object";

        for (var i : cls.getInterfaces())
            info.interfaces.add(i.getName());

        // Very rough metrics
        info.metrics.wmc = info.methods.size();
        info.metrics.atfd = info.attributes.size();
        info.metrics.tcc = info.methods.isEmpty() ? 0 : 1.0 / info.methods.size();

        model.addClass(info);
    }

    // ----------------------------------------------------
    // RECURSIVE FILE SEARCH
    // ----------------------------------------------------
    private List<File> getAllFiles(File root) {
        List<File> list = new ArrayList<>();

        File[] files = root.listFiles();
        if (files == null) return list;

        for (File f : files) {
            if (f.isDirectory()) {
                list.addAll(getAllFiles(f));
            } else if (
                    f.getName().endsWith(".java") ||
                            f.getName().endsWith(".class") ||
                            f.getName().endsWith(".jar")
            ) {
                list.add(f);
            }
        }
        return list;
    }

    // ----------------------------------------------------
    // JAVAC COMPILATION FOR .java FILES
    // ----------------------------------------------------
    private File compileJavaFile(File javaFile) throws Exception {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null)
            throw new RuntimeException("No JDK compiler available!");

        int result = compiler.run(null, null, null, javaFile.getPath());

        if (result != 0)
            throw new RuntimeException("javac failed for: " + javaFile.getName());

        return new File(javaFile.getParent(), javaFile.getName().replace(".java", ".class"));
    }

    // ----------------------------------------------------
    // JSON EXPORT
    // ----------------------------------------------------
    public void exportJson(SystemModel model, String outPath) throws Exception {
        Gson gson = new Gson();
        try (FileWriter fw = new FileWriter(outPath)) {
            gson.toJson(model.getClasses(), fw);
        }
    }
}
