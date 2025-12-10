package com.traffic.sim.controller;

import com.traffic.sim.rendering.Renderer;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class UIController {

    @FXML
    private Pane simulationCanvas;

    private MainController mainController;

    @FXML
    public void initialize() {
        // Initialize Renderer with the Pane from FXML
        Renderer renderer = new Renderer(simulationCanvas);

        // Initialize MainController with Renderer
        mainController = new MainController(renderer);
    }

    @FXML
    private void onStartClicked() {
        mainController.start();
    }

    @FXML
    private void onStopClicked() {
        mainController.stop();
    }

    @FXML
    private void onResetClicked() {
        mainController.reset();
    }

    @FXML
    private void onSpawnVehicleClicked() {
        mainController.spawnVehicle();
    }

    @FXML
    private void onSpawnPedestrianClicked() {
        mainController.spawnPedestrian();
    }
}
