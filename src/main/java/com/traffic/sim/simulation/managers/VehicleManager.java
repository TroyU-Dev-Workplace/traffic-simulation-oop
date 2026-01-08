package com.traffic.sim.simulation.managers;

import com.traffic.sim.simulation.entities.Vehicle;
import com.traffic.sim.simulation.entities.TrafficLight; // Nam adding: Import for traffic light integration
import com.traffic.sim.simulation.entities.Region; // Nam add: Region matching
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleManager {
    private Map<String, Vehicle> vehicles;
    // Nam adding: Reference to traffic light system for integration
    private TrafficLightSystem trafficLightSystem;

    public VehicleManager() {
        this.vehicles = new HashMap<>();
    }
    
    // Nam adding: Constructor with traffic light system
    public VehicleManager(TrafficLightSystem trafficLightSystem) {
        this.vehicles = new HashMap<>();
        this.trafficLightSystem = trafficLightSystem;
    }
    
    // Nam adding: Set traffic light system reference
    public void setTrafficLightSystem(TrafficLightSystem trafficLightSystem) {
        this.trafficLightSystem = trafficLightSystem;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.put(vehicle.getId(), vehicle);
    }

    public void update() {
        for (Vehicle v : vehicles.values()) {
            // Nam adding: Check traffic lights before updating vehicle movement
            checkTrafficLightsForVehicle(v);
            v.update();
        }
    }
    
    // Nam adding: Enhanced update method with map interaction
    public void updateWithMap(com.traffic.sim.simulation.map.Map map) {
        // Nam adding: Use iterator to safely remove vehicles during iteration
        java.util.Iterator<Vehicle> iterator = vehicles.values().iterator();
        while (iterator.hasNext()) {
            Vehicle v = iterator.next();
            // Check traffic lights with stop line distance logic
            checkTrafficLightsForVehicleWithMap(v, map);
            
            // Debug: Ensure vehicle has original speed if not explicitly stopped
            if (!v.isStoppedAtLight() && !v.isStoppedForVehicle() && v.getSpeed() == 0.0) {
                v.setSpeed(v.getOriginalSpeed()); // Restore speed if somehow lost
            }
            
            // Simplified collision detection - only check very close vehicles
            if (!v.isStoppedAtLight()) {
                // Check for very close collisions only (reduced thresholds)
                boolean hasVehicleAhead = v.hasVehicleAhead(vehicles.values(), 1.5, 0.5); 
                if (hasVehicleAhead) {
                    v.setSpeed(0.0);
                    v.setStoppedForVehicle(true);
                } else if (v.isStoppedForVehicle()) {
                    // Restore speed and clear vehicle collision flag
                    v.setSpeed(v.getOriginalSpeed());
                    v.setStoppedForVehicle(false);
                    // Re-check traffic lights after clearing vehicle collision
                    checkTrafficLightsForVehicleWithMap(v, map);
                }
            }
            
            // Final check: Ensure vehicle is moving if no reason to stop
            if (v.getSpeed() == 0.0 && !v.isStoppedAtLight() && !v.isStoppedForVehicle()) {
                v.setSpeed(v.getOriginalSpeed()); // Force movement if no stopping reason
                
                // Additional check: If vehicle is on a non-driveable tile, try to move to nearby driveable tile
                int currentX = (int) Math.floor(v.getX());
                int currentY = (int) Math.floor(v.getY());
                if (!map.isDriveableForVehicle(currentX, currentY)) {
                    // Try to find nearby driveable position
                    boolean moved = tryMoveToNearbyDriveableTile(v, map);
                    if (!moved) {
                        // If can't find driveable position, remove vehicle
                        v.markForRemoval();
                    }
                }
            }
            
            // Nam adding: Use enhanced update with map checking
            v.updateWithMap(map);
            
            // Nam adding: Remove vehicles that went outside map or hit blocked areas
            if (v.shouldBeRemoved()) {
                iterator.remove();
                continue;
            }

            // Nam adding: Only remove vehicles when they exit the map
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
    private double calculateDistanceToStopLineWithMap(Vehicle vehicle, Region region, com.traffic.sim.simulation.map.Map map) {
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
                if (dx == 0 && dy == 0) continue; // Skip current position
                
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
