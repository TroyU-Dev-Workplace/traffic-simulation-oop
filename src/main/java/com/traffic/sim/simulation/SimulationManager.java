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
    private com.traffic.sim.simulation.managers.MetricsManager metricsManager;

    public SimulationManager() {
        this.trafficLightSystem = new TrafficLightSystem();
        this.vehicleManager = new VehicleManager(trafficLightSystem);
        this.pedestrianManager = new PedestrianManager();
        // Initialize map 50x36 (1000px / 20px)
        this.mapSystem = new MapSystem(50, 36);
        this.metricsManager = new com.traffic.sim.simulation.managers.MetricsManager(mapSystem.getMap());

        this.spawnVehicleLogic = new SpawnVehicle(mapSystem, vehicleManager);
        this.spawnPedestrianLogic = new SpawnPedestrian(mapSystem, pedestrianManager);
    }

    public void update() {
        List<Vehicle> removedVehicles = vehicleManager.updateWithMap(mapSystem.getMap());
        // Register exited vehicles for metrics
        for (Vehicle v : removedVehicles) {
            metricsManager.registerVehicleExit(v);
        }

        pedestrianManager.updateWithMap(mapSystem.getMap(), trafficLightSystem);

        trafficLightSystem.update();

        // Update real-time metrics
        metricsManager.update(vehicleManager.getVehicles());
    }

    public void updateTrafficLightTimings(int green, int yellow, int red) {
        trafficLightSystem.setDurations(green, yellow, red);
    }

    public void spawnVehicle() {
        spawnVehicleLogic.spawn();
    }

    public void autoSpawnVehicle() {
        // Just call standard logic which now includes safe retry
        spawnVehicleLogic.spawn();
    }

    public void spawnPedestrian() {
        spawnPedestrianLogic.spawn();
    }

    public void autoSpawnPedestrian() {
        spawnPedestrianLogic.spawn();
    }

    public void reset() {
        vehicleManager.clear();
        pedestrianManager.clear();
        // Reset traffic lights if possible
        trafficLightSystem.reset();
        mapSystem.reset();
        metricsManager.reset();
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

    public com.traffic.sim.simulation.managers.MetricsManager getMetricsManager() {
        return metricsManager;
    }
}
