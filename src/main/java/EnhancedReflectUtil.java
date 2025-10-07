import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class EnhancedReflectUtil extends ReflectUtil {
    public EnhancedReflectUtil(Class<?> targetClass) {
        super(targetClass);
    }

    // Get method line numbers (requires ASM)
    public Map<String, Integer> getMethodLineNumbers() {
        Map<String, Integer> lineNumbers = new HashMap<>();
        try {
            ClassReader reader = new ClassReader(targetClass.getName());
            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitLineNumber(int line, Label start) {
                            lineNumbers.put(name, line);
                        }
                    };
                }
            }, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lineNumbers;
    }

    // Get methods called by a method (requires ASM)
    public Map<String, List<String>> getMethodCalls() {
        Map<String, List<String>> methodCalls = new HashMap<>();
        try {
            ClassReader reader = new ClassReader(targetClass.getName());
            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    List<String> calls = new ArrayList<>();
                    methodCalls.put(name, calls);
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String methodName, String desc, boolean itf) {
                            calls.add(methodName);
                        }
                    };
                }
            }, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return methodCalls;
    }

    // Get field visibility
    public Map<String, String> getFieldVisibilities() {
        Map<String, String> visibilities = new HashMap<>();
        for (Field f : targetClass.getDeclaredFields()) {
            visibilities.put(f.getName(), Modifier.toString(f.getModifiers()));
        }
        return visibilities;
    }

    // Get fields used in methods (requires ASM)
    public Map<String, List<String>> getFieldsUsedInMethods() {
        Map<String, List<String>> fieldsUsed = new HashMap<>();
        try {
            ClassReader reader = new ClassReader(targetClass.getName());
            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    List<String> fields = new ArrayList<>();
                    fieldsUsed.put(name, fields);
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitFieldInsn(int opcode, String owner, String fieldName, String desc) {
                            fields.add(fieldName);
                        }
                    };
                }
            }, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fieldsUsed;
    }
}
