package com.traffic.sim.controller;

import com.traffic.sim.rendering.Renderer;
import com.traffic.sim.simulation.SimulationManager;
import javafx.animation.AnimationTimer;

/**
 * Coordinator between Backend (Simulation) and Frontend (Renderer).
 * Does not contain UI logic itself, but controls the flow.
 */
public class MainController {
    private SimulationManager simulationManager;
    private Renderer renderer;
    private AnimationTimer gameLoop;
    private boolean isRunning;

    public MainController(Renderer renderer) {
        this.renderer = renderer;
        this.simulationManager = new SimulationManager();
        this.isRunning = false;

        initializeGameLoop();
    }

    private void initializeGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isRunning) {
                    // Update simulation
                    simulationManager.update();
                    // Update rendering
                    renderer.render(simulationManager);
                }
            }
        };
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
}
