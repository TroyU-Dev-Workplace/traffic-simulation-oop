package com.traffic.sim.simulation;

import java.util.List;

import com.traffic.sim.simulation.entities.Pedestrian;
import com.traffic.sim.simulation.entities.TrafficLight;
import com.traffic.sim.simulation.entities.Vehicle;
import com.traffic.sim.simulation.managers.MapSystem;
import com.traffic.sim.simulation.managers.PedestrianManager;
import com.traffic.sim.simulation.managers.TrafficLightSystem;
import com.traffic.sim.simulation.managers.VehicleManager;
import com.traffic.sim.simulation.spawn.SpawnPedestrian;
import com.traffic.sim.simulation.spawn.SpawnVehicle;

/**
 * Main coordinator for the simulation logic.
 * Pure Java, no JavaFX.
 */
public class SimulationManager {
    private VehicleManager vehicleManager;
    private PedestrianManager pedestrianManager;
    private TrafficLightSystem trafficLightSystem;
    private MapSystem mapSystem;
    private SpawnVehicle spawnVehicleLogic;
    private SpawnPedestrian spawnPedestrianLogic;

    public SimulationManager() {
        this.trafficLightSystem = new TrafficLightSystem();
        // Nam adding: Initialize VehicleManager with traffic light system reference
        this.vehicleManager = new VehicleManager(trafficLightSystem);
        this.pedestrianManager = new PedestrianManager();
        // Initialize map 50x36 (1000px / 20px)
        this.mapSystem = new MapSystem(50, 36);
        this.spawnVehicleLogic = new SpawnVehicle(mapSystem, vehicleManager);
        this.spawnPedestrianLogic = new SpawnPedestrian(mapSystem, pedestrianManager);
    }

    public void update() {
        // Nam adding: Use enhanced update method with map integration
        vehicleManager.updateWithMap(mapSystem.getMap());
        pedestrianManager.updateWithMap(mapSystem.getMap(), trafficLightSystem); // Use map-aware pedestrian update with
                                                                                 // strict TLS check

        trafficLightSystem.update();
    }

    public void updateTrafficLightTimings(int green, int yellow, int red) {
        trafficLightSystem.setDurations(green, yellow, red);
    }

    public void spawnVehicle() {
        spawnVehicleLogic.spawn();
    }

    public void spawnPedestrian() {
        spawnPedestrianLogic.spawn();
    }

    public void reset() {
        vehicleManager.clear();
        pedestrianManager.clear();
    }

    // Getters for Renderer to read state (ReadOnly ideally, but for simplicity
    // returning lists)

    public Vehicle findVehicleAt(double x, double y, double radius) {
        for (Vehicle v : vehicleManager.getVehicles()) {
            double dx = v.getX() - x;
            double dy = v.getY() - y;
            if (dx * dx + dy * dy < radius * radius) {
                return v;
            }
        }
        return null;
    }

    public List<Vehicle> getVehicles() {
        return vehicleManager.getVehicles();
    }

    public List<Pedestrian> getPedestrians() {
        return pedestrianManager.getPedestrians();
    }

    public List<TrafficLight> getTrafficLights() {
        return trafficLightSystem.getTrafficLights();
    }

    public MapSystem getMapSystem() {
        return mapSystem;
    }
}
