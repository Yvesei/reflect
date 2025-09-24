import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectUtil {

    private final Class<?> targetClass;

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


    public static void main(String[] args) {
        try {
            ReflectUtil util = new ReflectUtil(Class.forName("user"));

            System.out.println("Field names: " + util.getFieldNames());
            System.out.println("Method names: " + util.getMethodNames());
            System.out.println("Constructors: " + util.getConstructorSignatures());


        } catch (ClassNotFoundException e) {

        }
    }
}
