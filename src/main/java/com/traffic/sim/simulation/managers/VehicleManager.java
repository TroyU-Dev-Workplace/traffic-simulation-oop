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
            // Nam adding: Check traffic lights before updating vehicle movement
            checkTrafficLightsForVehicle(v);
            // Nam fix: Only check collisions if vehicle is not stopped at traffic light
            if (!v.isStoppedAtLight()) {
                // Nam fix: Check vehicle-ahead collision with better thresholds
                boolean hasVehicleAhead = v.hasVehicleAhead(vehicles.values(), 2.5, 1.0); // Nam fix: Increased thresholds
                if (hasVehicleAhead) {
                    v.setSpeed(0.0); // Nam fix: Stop for vehicle ahead
                    v.setStoppedForVehicle(true); // Nam fix: Mark as stopped for vehicle
                } else if (v.isStoppedForVehicle()) {
                    // Nam fix: Restore original speed when no collision, but still check traffic lights
                    v.setSpeed(v.getOriginalSpeed()); // Nam fix: Restore original speed first
                    v.setStoppedForVehicle(false); // Nam fix: Clear vehicle stop flag
                    checkTrafficLightsForVehicle(v); // Nam fix: Then apply traffic light rules
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
    
    // Nam adding: Method to check traffic lights for a specific vehicle
    /**
     * Checks nearby traffic lights and makes vehicle respond accordingly
     * @param vehicle The vehicle to check traffic lights for
     */
    private void checkTrafficLightsForVehicle(Vehicle vehicle) {
        if (trafficLightSystem == null) {
            return; // No traffic light system available
        }
        
        List<TrafficLight> trafficLights = trafficLightSystem.getTrafficLights();
        TrafficLight.State relevantLightState = null;
        
        double vehicleX = vehicle.getX();
        double vehicleY = vehicle.getY();
        double directionX = vehicle.getDirectionX();
        double directionY = vehicle.getDirectionY();
        
        // Nam fix: Only check traffic lights if vehicle is still in its spawn region
        Region vehicleSpawnRegion = vehicle.getSpawnRegion();
        if (vehicleSpawnRegion == null || !vehicle.isInsideSpawnRegion()) {
            // Nam fix: Vehicle has left spawn region, no need to check traffic lights
            vehicle.respondToTrafficLight(TrafficLight.State.GREEN);
            return;
        }
        
        // Nam fix: Only check traffic lights that belong to vehicle's spawn region
        for (TrafficLight light : trafficLights) {
            if (light.getType() == TrafficLight.TrafficLightType.VEHICLE) {
                // Nam fix: Only check lights in the same region as vehicle's spawn region
                if (light.getRegion() != vehicleSpawnRegion) {
                    continue;
                }
                
                double lightX = light.getX();
                double lightY = light.getY();
                
                // Nam fix: Check if vehicle is approaching this traffic light
                double distance = Math.sqrt(Math.pow(vehicleX - lightX, 2) + Math.pow(vehicleY - lightY, 2));
                double forwardDot = (lightX - vehicleX) * directionX + (lightY - vehicleY) * directionY;
                
                // Nam fix: Vehicle should respond to traffic light when approaching intersection
                if (distance <= 6.0 && forwardDot > 0.0) {
                    // Nam fix: Check if movement direction matches light direction
                    boolean shouldRespond = false;
                    
                    if (light.getDirection() == TrafficLight.Direction.NS && Math.abs(directionY) > Math.abs(directionX)) {
                        shouldRespond = true; // North-South movement
                    } else if (light.getDirection() == TrafficLight.Direction.EW && Math.abs(directionX) > Math.abs(directionY)) {
                        shouldRespond = true; // East-West movement
                    }
                    
                    if (shouldRespond) {
                        relevantLightState = light.getCurrentState();
                        break;
                    }
                }
            }
        }
        
        // Nam fix: Make vehicle respond to traffic light
        if (relevantLightState != null) {
            vehicle.respondToTrafficLight(relevantLightState);
        } else {
            vehicle.respondToTrafficLight(TrafficLight.State.GREEN);
        }
    }
}
