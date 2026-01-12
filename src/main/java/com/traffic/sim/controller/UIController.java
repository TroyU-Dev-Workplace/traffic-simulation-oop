package com.traffic.sim.controller;

import com.traffic.sim.rendering.Renderer;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import javafx.scene.control.TextField;

public class UIController {

    @FXML
    private Pane simulationCanvas;

    private MainController mainController;

    @FXML
    public void initialize() {
        // Initialize Renderer with the Pane from FXML
        Renderer renderer = new Renderer(simulationCanvas);

        // Initialize MainController with Renderer and UIController
        mainController = new MainController(renderer, this);
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
        // Reset timer label if needed
        timerLabel.setText("02:00");
        mainController.setTimerLabel(timerLabel);
    }

    @FXML
    private javafx.scene.control.Button autoSimButton;
    @FXML
    private javafx.scene.control.Label timerLabel;
    @FXML
    private javafx.scene.control.Button toggleGridButton;

    @FXML
    private void onAutoSimClicked() {
        mainController.startAutoSimulation(timerLabel);
    }

    @FXML
    private void onToggleGridClicked() {
        mainController.toggleGrid();
    }

    @FXML
    private void onSpawnVehicleClicked() {
        mainController.spawnVehicle();
    }

    @FXML
    private void onSpawnPedestrianClicked() {
        mainController.spawnPedestrian();
    }

    @FXML
    private TextField greenInput;
    @FXML
    private TextField yellowInput;
    @FXML
    private TextField redInput;

    @FXML
    private void onApplySettingsClicked() {
        try {
            int g = Integer.parseInt(greenInput.getText());
            int y = Integer.parseInt(yellowInput.getText());
            int r = Integer.parseInt(redInput.getText());
            mainController.updateTrafficLightSettings(g, y, r);
        } catch (NumberFormatException e) {
            System.err.println("Invalid traffic light timings input");
        }
    }

    // --- METRICS DASHBOARD BINDINGS ---
    @FXML
    private javafx.scene.control.Label lblThroughput;
    @FXML
    private javafx.scene.control.Label lblOccupancy;
    @FXML
    private javafx.scene.control.Label lblCongestion;
    @FXML
    private javafx.scene.control.Label lblDeadlockRate;
    @FXML
    private javafx.scene.control.Label lblBlockageCount;

    @FXML
    private javafx.scene.control.Label lblAvgWait;
    @FXML
    private javafx.scene.control.Label lblAvgTravel;
    @FXML
    private javafx.scene.control.Label lblCO2;
    @FXML
    private javafx.scene.control.Label lblFrustration;

    public void updateMetrics(double throughput, double occupancy, boolean congested,
            double deadlockRate, int blockageCount,
            double avgWait, double avgTravel, double co2, double frustration,
            boolean isSummaryAvailable) {

        javafx.application.Platform.runLater(() -> {
            // Real-time Metrics
            lblThroughput.setText(String.format("%.1f v/m", throughput));
            lblOccupancy.setText(String.format("%.1f%%", occupancy));

            lblCongestion.setText(congested ? "HIGH" : "Normal");
            lblCongestion.setTextFill(
                    congested ? javafx.scene.paint.Color.rgb(243, 139, 168) : javafx.scene.paint.Color.WHITE);

            lblDeadlockRate.setText(String.format("%.1f%%", deadlockRate));
            lblBlockageCount.setText(String.format("%d", blockageCount));

            // Summary Metrics (Only show values if simulation finished/summary available)
            if (isSummaryAvailable) {
                lblAvgWait.setText(String.format("%.1fs", avgWait));
                lblAvgTravel.setText(String.format("%.1fs", avgTravel));
                lblCO2.setText(String.format("%.1f CO2 units", co2));
                lblFrustration.setText(String.format("%.1f%%", frustration));
            } else {
                lblAvgWait.setText("--");
                lblAvgTravel.setText("--");
                lblCO2.setText("--");
                lblFrustration.setText("--");
            }
        });
    }
}
