package app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.List;

public class ResultsController {

    @FXML public ListView<String> godList;
    @FXML public ListView<String> envyList;
    @FXML public ListView<String> dataList;
    @FXML public ListView<String> longMethodList;
    @FXML public ListView<String> largeClassList;
    @FXML public ListView<String> messageChainList;
    @FXML public ListView<String> middleManList;
    @FXML public ListView<String> shotgunList;
    @FXML public ListView<String> divergentList;
    @FXML public ListView<String> deadCodeList;

    public void setGodClasses(List<String> items) {
        godList.getItems().setAll(items);
    }

    public void setFeatureEnvy(List<String> items) {
        envyList.getItems().setAll(items);
    }

    public void setDataClasses(List<String> items) {
        dataList.getItems().setAll(items);
    }

    public void setLongMethods(List<String> items) {
        longMethodList.getItems().setAll(items);
    }

    public void setLargeClasses(List<String> items) {
        largeClassList.getItems().setAll(items);
    }

    public void setMessageChains(List<String> items) {
        messageChainList.getItems().setAll(items);
    }

    public void setMiddleMen(List<String> items) {
        middleManList.getItems().setAll(items);
    }

    public void setShotgunSurgery(List<String> items) {
        shotgunList.getItems().setAll(items);
    }

    public void setDivergentChange(List<String> items) {
        divergentList.getItems().setAll(items);
    }

    public void setDeadCode(List<String> items) {
        deadCodeList.getItems().setAll(items);
    }
}
