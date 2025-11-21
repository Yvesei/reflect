package core.detection;

import java.io.File;
import java.nio.file.Files;

public class FileTypeDetector {

    public static InputType detect(File file) {

        if (file.isDirectory())
            return InputType.DIRECTORY;

        String name = file.getName().toLowerCase();

        if (name.endsWith(".java")) return InputType.JAVA_SOURCE;
        if (name.endsWith(".class")) return InputType.CLASS_FILE;
        if (name.endsWith(".jar")) return InputType.JAR_FILE;

        if (name.endsWith(".exe") && containsJar(file))
            return InputType.JAVA_EXE;

        throw new RuntimeException("Unsupported Java artifact: " + file.getName());
    }

    private static boolean containsJar(File exe) {
        try {
            byte[] bytes = Files.readAllBytes(exe.toPath());
            // "PK" = signature ZIP/JAR
            for (int i = 0; i < bytes.length - 1; i++) {
                if (bytes[i] == 'P' && bytes[i+1] == 'K')
                    return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
