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
import java.util.List;

public class MainController {

    @FXML private Button openButton;
    @FXML private Label statusLabel;

    private final ReflectiveAnalyzer analyzer = new ReflectiveAnalyzer();
    private final Neo4jService neo = new Neo4jService("bolt://localhost:7687", "neo4j", "password");
    private final GraphImporter importer = new GraphImporter(neo);

    // ---------------------------------------------
    // UI button: choose file OR folder
    // ---------------------------------------------
    @FXML
    public void openFileChooser() {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select .java / .class file (or choose a folder instead)");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Java Files", "*.java", "*.class")
        );

        // Let user pick a file FIRST
        File file = chooser.showOpenDialog(openButton.getScene().getWindow());

        if (file != null) {
            statusLabel.setText("Analyzing: " + file.getName());
            analyzeJavaProject(file);
            return;
        }

        // Otherwise: let them pick a folder
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Or choose a folder to analyze");
        File folder = dc.showDialog(openButton.getScene().getWindow());

        if (folder == null) {
            statusLabel.setText("Nothing selected.");
            return;
        }

        statusLabel.setText("Analyzing folder: " + folder.getName());
        analyzeJavaProject(folder);
    }

    // ---------------------------------------------
    // Run reflection → export JSON → load Neo4j → detect smells
    // ---------------------------------------------
    private void analyzeJavaProject(File fileOrFolder) {
        try {

            SystemModel model;

            // If folder → analyze all .java + .class
            if (fileOrFolder.isDirectory()) {
                model = analyzer.buildProjectModel(fileOrFolder);
            }
            // If single file → normal behavior
            else {
                InputType type = fileOrFolder.getName().endsWith(".java")
                        ? InputType.JAVA_SOURCE
                        : InputType.CLASS_FILE;

                model = analyzer.buildModel(fileOrFolder, type);
            }

            // 2. Export JSON
            File jsonOut = new File("/tmp/model.json");
            try (FileWriter writer = new FileWriter(jsonOut)) {
                writer.write(model.toJson());
            }

            // 3. Import into Neo4j
            importer.importJson(jsonOut.getAbsolutePath());

            // 4. Run detectors
            BadSmellDetector smells = new BadSmellDetector(neo);

            List<String> god = smells.detectGodClass();
            List<String> envy = smells.detectFeatureEnvy();
            List<String> data = smells.detectDataClass();
            List<String> longM = smells.detectLongMethod();
            List<String> large = smells.detectLargeClass();
            List<String> chain = smells.detectMessageChains();
            List<String> middle = smells.detectMiddleMan();
            List<String> shotgun = smells.detectShotgunSurgery();
            List<String> divergent = smells.detectDivergentChange();

            // 5. Show results UI
            loadResultsWindow(
                    god, envy, data, longM, large,
                    chain, middle, shotgun, divergent
            );

            statusLabel.setText("Analysis completed.");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    // ---------------------------------------------
    // Load Results Window (10 lists)
    // ---------------------------------------------
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
