package com.traffic.sim.controller;

import com.traffic.sim.rendering.Renderer;
import com.traffic.sim.simulation.SimulationManager;

import javafx.animation.AnimationTimer;

/**
 * Coordinator between Backend (Simulation) and Frontend (Renderer).
 */
public class MainController {
    private SimulationManager simulationManager;
    private Renderer renderer;
    private UIController uiController; // Reference to UI
    private AnimationTimer gameLoop;
    private boolean isRunning;

    public MainController(Renderer renderer, UIController uiController) {
        this.renderer = renderer;
        this.uiController = uiController;
        this.simulationManager = new SimulationManager();
        this.isRunning = false;

        initializeGameLoop();
    }

    private javafx.scene.control.Label timerLabel;
    private boolean isAutoSimRunning = false;
    private double autoSimTimeRemaining = 120.0; // 2 minutes

    // Counters for auto spawning
    private int vehicleSpawnTimer = 0;
    private int pedestrianSpawnTimer = 0;
    private int metricsUpdateTimer = 0; // Throttle UI updates

    public void setTimerLabel(javafx.scene.control.Label label) {
        this.timerLabel = label;
    }

    public void startAutoSimulation(javafx.scene.control.Label label) {
        this.timerLabel = label;
        if (!isRunning) {
            start();
        }
        isAutoSimRunning = true;
        autoSimTimeRemaining = 120.0;
        updateTimerLabel();
    }

    public void toggleGrid() {
        renderer.toggleGrid();
        // Redraw immediately if paused
        if (!isRunning) {
            renderer.render(simulationManager);
        }
    }

    private void updateTimerLabel() {
        if (timerLabel == null)
            return;
        int minutes = (int) autoSimTimeRemaining / 60;
        int seconds = (int) autoSimTimeRemaining % 60;
        javafx.application.Platform.runLater(() -> timerLabel.setText(String.format("%02d:%02d", minutes, seconds)));
    }

    private void initializeGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isRunning) {
                    // Calculate delta time (simple approx or use real time)
                    double deltaTime = 1.0 / 60.0; // Approx 60 FPS

                    if (isAutoSimRunning) {
                        autoSimTimeRemaining -= deltaTime;
                        if (autoSimTimeRemaining <= 0) {
                            autoSimTimeRemaining = 0;
                            isAutoSimRunning = false;
                            stop(); // End simulation
                            updateTimerLabel();

                            // FINAL UPDATE WITH SUMMARY
                            updateUIMetrics(true);

                            System.out.println("Auto Simulation Complete");
                            return;
                        }
                        updateTimerLabel();
                        handleAutoSpawning();
                    }

                    // Update simulation
                    simulationManager.update();
                    // Update rendering
                    renderer.render(simulationManager);

                    // Update UI Metrics (Realtime only)
                    metricsUpdateTimer++;
                    if (metricsUpdateTimer >= 10) {
                        updateUIMetrics(false);
                        metricsUpdateTimer = 0;
                    }
                }
            }
        };
    }

    private void updateUIMetrics(boolean isSummaryAvailable) {
        if (uiController == null)
            return;

        com.traffic.sim.simulation.managers.MetricsManager mm = simulationManager.getMetricsManager();
        if (mm == null)
            return;

        uiController.updateMetrics(
                mm.getThroughput(),
                mm.getOccupancy(),
                mm.isCongested(),
                mm.getDeadlockRate(),
                mm.getBlockageCount(),
                // Summary Metrics (Passed regardless, UIController decides to show or mask
                // based on flag)
                mm.getFinalAvgWaitingTime(),
                mm.getFinalAvgTravelTime(),
                mm.getFinalTotalCO2(),
                mm.getFinalFrustrationRate(),
                isSummaryAvailable);
    }

    private void handleAutoSpawning() {
        // Vehicle spawn every ~90 frames (approx 1.5 seconds) - Slower to prevent
        // gridlock
        vehicleSpawnTimer++;
        if (vehicleSpawnTimer >= 70) {
            simulationManager.autoSpawnVehicle();
            vehicleSpawnTimer = 0;
        }

        // Pedestrian spawn every ~180 frames (approx 3 seconds)
        pedestrianSpawnTimer++;
        if (pedestrianSpawnTimer >= 140) {
            simulationManager.autoSpawnPedestrian();
            pedestrianSpawnTimer = 0;
        }
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            gameLoop.start();
        }
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
            gameLoop.stop();
        }
    }

    public void reset() {
        stop();
        isAutoSimRunning = false;
        autoSimTimeRemaining = 120.0;
        simulationManager.reset();
        renderer.clear();
        // Render one frame of empty state
        renderer.render(simulationManager);
    }

    public void spawnVehicle() {
        simulationManager.spawnVehicle();
    }

    public void spawnPedestrian() {
        simulationManager.spawnPedestrian();
    }

    public void updateTrafficLightSettings(int green, int yellow, int red) {
        simulationManager.updateTrafficLightTimings(green, yellow, red);
    }
}
