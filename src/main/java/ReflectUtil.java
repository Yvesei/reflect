import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Map;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.concurrent.TimeUnit;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.QueryConfig;

public class ReflectUtil {
    protected final Class<?> targetClass;

    public ReflectUtil(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public List<String> getFieldNames() {
        Field[] fields = targetClass.getDeclaredFields();
        List<String> names = new ArrayList<>();
        for (Field f : fields) names.add(f.getName());
        return names;
    }

    public List<String> getMethodNames() {
        Method[] methods = targetClass.getDeclaredMethods();
        List<String> names = new ArrayList<>();
        for (Method m : methods) names.add(m.getName());
        return names;
    }

    public List<String> getConstructorSignatures() {
        Constructor<?>[] ctors = targetClass.getDeclaredConstructors();
        List<String> sigs = new ArrayList<>();
        for (Constructor<?> c : ctors) {
            StringBuilder sb = new StringBuilder();
            sb.append(Modifier.toString(c.getModifiers())).append(" ");
            sb.append(targetClass.getSimpleName()).append("(");
            Class<?>[] params = c.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(params[i].getSimpleName());
            }
            sb.append(")");
            sigs.add(sb.toString().trim());
        }
        return sigs;
    }

    public Object newInstance() {
        try {
            return targetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    // New method to get info from a .jar file
    public static String getJarInfoAsJson(String jarPath) throws Exception {
        List<Class<?>> classes = loadClassesFromJar(jarPath);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < classes.size(); i++) {
            if (i > 0) sb.append(",");
            ReflectUtil util = new ReflectUtil(classes.get(i));
            sb.append(util.toJSON());
        }
        sb.append("]");
        return sb.toString();
    }

    // Helper method to load classes from a .jar file
    static List<Class<?>> loadClassesFromJar(String jarPath) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        URL jarUrl = new File(jarPath).toURI().toURL();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] { jarUrl }, ClassLoader.getSystemClassLoader())) {
            try (JarFile jarFile = new JarFile(new File(jarPath))) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String className = entry.getName()
                                .replace("/", ".")
                                .replace(".class", "");
                        Class<?> clazz = classLoader.loadClass(className);
                        classes.add(clazz);
                    }
                }
            }
        }
        return classes;
    }

    // Convert class info to JSON
    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"className\":\"").append(targetClass.getName()).append("\",");
        sb.append("\"fields\":[");
        List<String> fields = getFieldNames();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(fields.get(i)).append("\"");
        }
        sb.append("],");
        sb.append("\"methods\":[");
        List<String> methods = getMethodNames();
        for (int i = 0; i < methods.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(methods.get(i)).append("\"");
        }
        sb.append("],");
        sb.append("\"constructors\":[");
        List<String> constructors = getConstructorSignatures();
        for (int i = 0; i < constructors.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(constructors.get(i)).append("\"");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    public static void main(String[] args) {
        try {
            String jarPath = "target/reflection-1.0-SNAPSHOT.jar";

            //Get JSON output
            String jsonOutput = ReflectUtil.getJarInfoAsJson(jarPath);
            try (java.io.FileWriter file = new java.io.FileWriter("output.json")) {
                file.write(jsonOutput);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("JSON Output:");
            System.out.println(jsonOutput);

            // Connect to Neo4j Aura
            var dotenv = Dotenv.configure()
                    .filename(".env")
                    .load();

            final String dbUri = dotenv.get("NEO4J_URI");
            final String dbUser = dotenv.get("NEO4J_USERNAME");
            final String dbPassword = dotenv.get("NEO4J_PASSWORD");

            try (var driver = GraphDatabase.driver(dbUri, AuthTokens.basic(dbUser, dbPassword))) {
                driver.verifyConnectivity();
                System.out.println("Connection established.");
            }


//            var result = driver.executableQuery("""
//            CREATE (a:Person {name: $name})
//            CREATE (b:Person {name: $friendName})
//            CREATE (a)-[:KNOWS]->(b)
//            """)
//                    .withParameters(Map.of("name", "Alice", "friendName", "David"))
//                    .withConfig(QueryConfig.builder().withDatabase("<database-name>").build())
//                    .execute();
//            var summary = result.summary();
//            System.out.printf("Created %d records in %d ms.%n",
//                    summary.counters().nodesCreated(),
//                    summary.resultAvailableAfter(TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
}
