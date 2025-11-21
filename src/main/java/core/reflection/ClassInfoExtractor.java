package core.reflection;

import core.model.ClassInfo;
import core.model.MethodInfo;

import java.lang.reflect.*;

public class ClassInfoExtractor {

    public static ClassInfo extract(Class<?> cls) {

        ClassInfo info = new ClassInfo();
        info.setName(cls.getName());

        // Fields
        for (Field f : cls.getDeclaredFields()) {
            info.getAttributes().add(f.getName());
        }

        // Methods
        for (Method m : cls.getDeclaredMethods()) {
            MethodInfo mi = new MethodInfo(m.getName());

            for (Parameter p : m.getParameters()) {
                mi.addParameter(p.getType().getSimpleName());
            }

            info.getMethods().add(mi);
        }

        // Superclass
        if (cls.getSuperclass() != null)
            info.setSuperClass(cls.getSuperclass().getName());

        // Interfaces
        for (Class<?> i : cls.getInterfaces()) {
            info.getInterfaces().add(i.getName());
        }

        return info;
    }
}
