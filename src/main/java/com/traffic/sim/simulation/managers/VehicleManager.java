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
            checkTrafficLightsForVehicle(v);
            v.update();
        }
    }

    public void updateWithMap(com.traffic.sim.simulation.map.Map map) {
        java.util.Iterator<Vehicle> iterator = vehicles.values().iterator();
        while (iterator.hasNext()) {
            Vehicle v = iterator.next();

            v.updateWithMap(map, trafficLightSystem);

            if (v.shouldBeRemoved()) {
                iterator.remove();
                continue;
            }
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

    // Enhanced method to check traffic lights based on stop line distance
    private void checkTrafficLightsForVehicle(Vehicle vehicle) {
        if (trafficLightSystem == null) {
            return;
        }

        // Get vehicle spawn region
        Region vehicleSpawnRegion = vehicle.getSpawnRegion();
        if (vehicleSpawnRegion == null) {
            vehicle.respondToTrafficLight(TrafficLight.State.GREEN);
            return;
        }

        // Fix: Don't check if vehicle is inside spawn region anymore

        // Check distance to stop line for vehicle's region
        double distanceToStopLine = calculateDistanceToStopLine(vehicle, vehicleSpawnRegion);
        double threshold = 3.0; // Distance threshold for traffic light activation

        if (distanceToStopLine <= threshold) {
            // Vehicle is close to stop line - check relevant traffic light
            TrafficLight.State lightState = getTrafficLightStateForRegion(vehicleSpawnRegion);
            vehicle.respondToTrafficLight(lightState);
        } else {
            // Vehicle is far from stop line - always green
            vehicle.respondToTrafficLight(TrafficLight.State.GREEN);
        }
    }

    // Calculate distance from vehicle to stop line in its region
    private double calculateDistanceToStopLine(Vehicle vehicle, Region region) {
        // This requires map access - we'll add this parameter in updateWithMap
        return calculateDistanceToStopLineWithMap(vehicle, region, null);
    }

    // Calculate distance from vehicle to stop line with map reference
    private double calculateDistanceToStopLineWithMap(Vehicle vehicle, Region region,
            com.traffic.sim.simulation.map.Map map) {
        if (map == null) {
            return 0.0; // Fallback to immediate check
        }

        double[] stopLinePos = map.getStopLinePosition(region);
        double vehicleX = vehicle.getX();
        double vehicleY = vehicle.getY();
        double directionX = vehicle.getDirectionX();
        double directionY = vehicle.getDirectionY();

        // Calculate vector from vehicle to stop line
        double dx = stopLinePos[0] - vehicleX;
        double dy = stopLinePos[1] - vehicleY;

        // Project onto movement direction to get forward distance
        double forwardDistance = dx * directionX + dy * directionY;

        // Return actual forward distance (can be negative if vehicle passed stop line)
        return forwardDistance;
    }

    // Get traffic light state for specific region
    private TrafficLight.State getTrafficLightStateForRegion(Region region) {
        if (trafficLightSystem == null) {
            return TrafficLight.State.GREEN;
        }

        List<TrafficLight> trafficLights = trafficLightSystem.getTrafficLights();
        for (TrafficLight light : trafficLights) {
            if (light.getType() == TrafficLight.TrafficLightType.VEHICLE &&
                    light.getRegion() == region) {
                return light.getCurrentState();
            }
        }

        return TrafficLight.State.GREEN; // Default to green if no light found
    }

    // Enhanced method that uses map for accurate stop line distance calculation
    private void checkTrafficLightsForVehicleWithMap(Vehicle vehicle, com.traffic.sim.simulation.map.Map map) {
        if (trafficLightSystem == null || map == null) {
            return;
        }

        // Get vehicle spawn region
        Region vehicleSpawnRegion = vehicle.getSpawnRegion();
        if (vehicleSpawnRegion == null) {
            vehicle.respondToTrafficLight(TrafficLight.State.GREEN);
            return;
        }

        // Fix: Don't check if vehicle is inside spawn region anymore
        // Vehicle should check traffic lights regardless of current position

        // Calculate distance to stop line using map data
        double distanceToStopLine = calculateDistanceToStopLineWithMap(vehicle, vehicleSpawnRegion, map);
        double threshold = 3.0; // Distance threshold for traffic light activation

        // Fix: Check if vehicle has passed the stop line (negative distance)
        if (distanceToStopLine < 0) {
            // Vehicle has passed stop line - always allow movement
            vehicle.respondToTrafficLight(TrafficLight.State.GREEN);
        } else if (distanceToStopLine <= threshold) {
            // Vehicle is approaching stop line - check traffic light
            TrafficLight.State lightState = getTrafficLightStateForRegion(vehicleSpawnRegion);
            vehicle.respondToTrafficLight(lightState);
        } else {
            // Vehicle is far from stop line - always green
            vehicle.respondToTrafficLight(TrafficLight.State.GREEN);
        }
    }

    // Helper method to move vehicle to nearby driveable tile
    private boolean tryMoveToNearbyDriveableTile(Vehicle vehicle, com.traffic.sim.simulation.map.Map map) {
        double currentX = vehicle.getX();
        double currentY = vehicle.getY();

        // Try positions in a small radius around current position
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue; // Skip current position

                double newX = currentX + dx * 0.5;
                double newY = currentY + dy * 0.5;
                int gridX = (int) Math.floor(newX);
                int gridY = (int) Math.floor(newY);

                if (map.isValid(gridX, gridY) && map.isDriveableForVehicle(gridX, gridY)) {
                    // Move vehicle to this driveable position
                    vehicle.setPosition(newX, newY);
                    return true;
                }
            }
        }
        return false;
    }
}
