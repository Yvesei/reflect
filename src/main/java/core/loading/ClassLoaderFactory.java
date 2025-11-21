package core.loading;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderFactory {

    public static ClassLoader fromDirectory(File dir) throws Exception {
        return new URLClassLoader(new URL[]{dir.toURI().toURL()});
    }

    public static ClassLoader fromJar(File jar) throws Exception {
        return new URLClassLoader(new URL[]{jar.toURI().toURL()});
    }
}
