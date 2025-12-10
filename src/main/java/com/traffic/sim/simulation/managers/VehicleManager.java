package com.traffic.sim.simulation.managers;

import com.traffic.sim.simulation.entities.Vehicle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleManager {
    private Map<String, Vehicle> vehicles;

    public VehicleManager() {
        this.vehicles = new HashMap<>();
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.put(vehicle.getId(), vehicle);
    }

    public void update() {
        for (Vehicle v : vehicles.values()) {
            v.update();
        }
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
