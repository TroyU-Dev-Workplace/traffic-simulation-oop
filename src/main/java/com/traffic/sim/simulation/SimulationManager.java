package com.traffic.sim.simulation;

import com.traffic.sim.simulation.entities.Pedestrian;
import com.traffic.sim.simulation.entities.TrafficLight;
import com.traffic.sim.simulation.entities.Vehicle;
import com.traffic.sim.simulation.managers.MapSystem;
import com.traffic.sim.simulation.managers.PedestrianManager;
import com.traffic.sim.simulation.managers.TrafficLightSystem;
import com.traffic.sim.simulation.managers.VehicleManager;
import com.traffic.sim.simulation.spawn.SpawnPedestrian;
import com.traffic.sim.simulation.spawn.SpawnVehicle;
import java.util.List;

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
        this.vehicleManager = new VehicleManager();
        this.pedestrianManager = new PedestrianManager();
        this.trafficLightSystem = new TrafficLightSystem();
        // Initialize map 50x36 (1000px / 20px)
        this.mapSystem = new MapSystem(50, 36);
        this.spawnVehicleLogic = new SpawnVehicle(mapSystem, vehicleManager);
        this.spawnPedestrianLogic = new SpawnPedestrian(mapSystem, pedestrianManager);
    }

    public void update() {
        vehicleManager.update();
        pedestrianManager.update();
        trafficLightSystem.update();
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
