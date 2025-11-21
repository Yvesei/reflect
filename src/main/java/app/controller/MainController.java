package app.controller;

import app.Main;
import core.detection.InputType;
import core.neo4j.BadSmellDetector;
import core.neo4j.GraphImporter;
import core.neo4j.Neo4jService;
import core.reflection.ReflectiveAnalyzer;
import core.reflection.SystemModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class MainController {

    @FXML private Button openButton;
    @FXML private Label statusLabel;

    private final ReflectiveAnalyzer analyzer = new ReflectiveAnalyzer();
    private final Neo4jService neo = new Neo4jService("bolt://localhost:7687", "neo4j", "password");
    private final GraphImporter importer = new GraphImporter(neo);

    // -----------------------------------------------------
    // Choose MULTIPLE FILES or a FOLDER
    // -----------------------------------------------------
    @FXML
    public void openFileChooser() {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select .java / .class files (or choose a folder)");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Java / Class / Jar Files", "*.java", "*.class", "*.jar")
        );

        // MULTIPLE FILES
        List<File> files = chooser.showOpenMultipleDialog(openButton.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            statusLabel.setText("Analyzing " + files.size() + " files...");
            analyzeMultipleFiles(files);
            return;
        }

        // SINGLE FOLDER
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose a folder to analyze");
        File folder = dc.showDialog(openButton.getScene().getWindow());

        if (folder == null) {
            statusLabel.setText("Nothing selected.");
            return;
        }

        statusLabel.setText("Analyzing folder: " + folder.getName());
        analyzeJavaProject(folder);
    }

    // -----------------------------------------------------
    // Analyze MULTIPLE FILES by building a temporary project
    // -----------------------------------------------------
    private void analyzeMultipleFiles(List<File> files) {
        try {
            // Clean & recreate temp directory
            File tempDir = new File("/tmp/javafx_multi/");
            if (tempDir.exists()) {
                for (File f : tempDir.listFiles()) f.delete();
            }
            tempDir.mkdirs();

            // Copy all selected files into temp project
            for (File f : files) {
                File out = new File(tempDir, f.getName());
                Files.copy(f.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Analyze as a project
            analyzeJavaProject(tempDir);

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------
    // Analyze project (single file OR folder)
    // -----------------------------------------------------
    private void analyzeJavaProject(File fileOrFolder) {
        try {

            SystemModel model;

            if (fileOrFolder.isDirectory()) {
                cleanClassFiles(fileOrFolder);

                model = analyzer.buildProjectModel(fileOrFolder);
            } else {
                InputType type = fileOrFolder.getName().endsWith(".java")
                        ? InputType.JAVA_SOURCE
                        : InputType.CLASS_FILE;
                model = analyzer.buildModel(fileOrFolder, type);
            }

            File jsonOut = new File("/tmp/model.json");
            try (FileWriter writer = new FileWriter(jsonOut)) {
                writer.write(model.toJson());
            }

            importer.importJson(jsonOut.getAbsolutePath());

            BadSmellDetector smells = new BadSmellDetector(neo);

            loadResultsWindow(
                    smells.detectGodClass(),
                    smells.detectFeatureEnvy(),
                    smells.detectDataClass(),
                    smells.detectLongMethod(),
                    smells.detectLargeClass(),
                    smells.detectMessageChains(),
                    smells.detectMiddleMan(),
                    smells.detectShotgunSurgery(),
                    smells.detectDivergentChange()
            );

            statusLabel.setText("Analysis completed.");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------
    // Remove old .class files (prevents mismatched bytecode)
    // -----------------------------------------------------
    private void cleanClassFiles(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                cleanClassFiles(f);
            } else if (f.getName().endsWith(".class")) {
                f.delete();
            }
        }
    }

    // -----------------------------------------------------
    // Results UI
    // -----------------------------------------------------
    private void loadResultsWindow(
            List<String> god,
            List<String> envy,
            List<String> data,
            List<String> longM,
            List<String> large,
            List<String> chain,
            List<String> middle,
            List<String> shotgun,
            List<String> divergent
    ) throws Exception {

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/results.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Bad Smell Analysis");

        ResultsController rc = loader.getController();
        rc.setGodClasses(god);
        rc.setFeatureEnvy(envy);
        rc.setDataClasses(data);
        rc.setLongMethods(longM);
        rc.setLargeClasses(large);
        rc.setMessageChains(chain);
        rc.setMiddleMen(middle);
        rc.setShotgunSurgery(shotgun);
        rc.setDivergentChange(divergent);

        stage.show();
    }
}
