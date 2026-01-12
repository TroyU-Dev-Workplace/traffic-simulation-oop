package com.traffic.sim.simulation.managers;

import com.traffic.sim.simulation.entities.Vehicle;
import com.traffic.sim.simulation.entities.TrafficLight;
import com.traffic.sim.simulation.entities.Region;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleManager {
    private Map<String, Vehicle> vehicles;
    private TrafficLightSystem trafficLightSystem;

    public VehicleManager() {
        this.vehicles = new HashMap<>();
    }

    public VehicleManager(TrafficLightSystem trafficLightSystem) {
        this.vehicles = new HashMap<>();
        this.trafficLightSystem = trafficLightSystem;
    }

    public void setTrafficLightSystem(TrafficLightSystem trafficLightSystem) {
        this.trafficLightSystem = trafficLightSystem;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.put(vehicle.getId(), vehicle);
    }

    public void update() {
        for (Vehicle v : vehicles.values()) {

            v.update();
        }
    }

    public List<Vehicle> updateWithMap(com.traffic.sim.simulation.map.Map map) {
        List<Vehicle> removedVehicles = new ArrayList<>();
        java.util.Iterator<Vehicle> iterator = vehicles.values().iterator();
        while (iterator.hasNext()) {
            Vehicle v = iterator.next();

            v.updateWithMap(map, trafficLightSystem);

            if (v.shouldBeRemoved()) {
                removedVehicles.add(v);
                iterator.remove();
                continue;
            }
        }
        return removedVehicles;
    }

    public List<Vehicle> getVehicles() {
        return new ArrayList<>(vehicles.values());
    }

    public Vehicle getVehicle(String id) {
        return vehicles.get(id);
    }

    public void removeVehicle(String id) {
        vehicles.remove(id);
    }

    public void clear() {
        vehicles.clear();
    }

}
