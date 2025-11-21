package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import core.model.ClassInfo;
import java.io.FileWriter;
import java.util.List;

public class JsonExporter {

    public static void export(List<ClassInfo> classes, String path) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fw = new FileWriter(path);
        gson.toJson(classes, fw);
        fw.close();
    }
}
