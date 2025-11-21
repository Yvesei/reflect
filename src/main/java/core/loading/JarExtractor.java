package core.loading;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class JarExtractor {

    public static File extractJarFromExe(File exe) throws Exception {

        byte[] bytes = Files.readAllBytes(exe.toPath());

        for (int i = 0; i < bytes.length - 3; i++) {
            if (bytes[i] == 'P' && bytes[i+1] == 'K') {
                File out = new File("extracted.jar");
                FileOutputStream fos = new FileOutputStream(out);
                fos.write(bytes, i, bytes.length - i);
                fos.close();
                return out;
            }
        }

        throw new RuntimeException("No JAR found inside the EXE.");
    }
}
