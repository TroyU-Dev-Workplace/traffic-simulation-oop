package com.traffic.sim.simulation.spawn;

import com.traffic.sim.simulation.entities.Vehicle;
import com.traffic.sim.simulation.managers.MapSystem;
import com.traffic.sim.simulation.managers.VehicleManager;
import com.traffic.sim.simulation.entities.Region; // Nam add: Assign spawn region
import java.util.UUID;

public class SpawnVehicle extends SpawnBase {
    private VehicleManager vehicleManager;

    public SpawnVehicle(MapSystem mapSystem, VehicleManager vehicleManager) {
        super(mapSystem);
        this.vehicleManager = vehicleManager;
    }

    @Override
    public void spawn() {
        // Nam adding: Spawn only in 4 regions (North/West/South/East) on road tiles
        RegionBounds intersection = findIntersectionBounds();
        SpawnRegion[] spawnRegions = buildRegionSpawns(intersection);

        // Nam adding: Cycling through 4 spawn regions
        if (!hasStaticCounter) {
            regionCounter = 0;
            hasStaticCounter = true;
        }

        SpawnRegion selectedRegion = spawnRegions[regionCounter % 4];
        regionCounter = (regionCounter + 1) % 4;

        // Nam adding: Try to spawn in the selected region
        int[] spawnPoint = pickSpawnPoint(selectedRegion);
        if (spawnPoint != null) {
            String id = UUID.randomUUID().toString();
            Vehicle vehicle = new Vehicle(id, spawnPoint[0] + 0.5, spawnPoint[1] + 0.5, 0.05);
            vehicle.setDirection(selectedRegion.dirX, selectedRegion.dirY);

            // Nam adding: Decide turn direction at spawn time only
            assignRandomTurnDirection(vehicle, selectedRegion.name);
            // Nam add: Save spawn region for traffic light filtering
            vehicle.setSpawnRegion(parseRegion(selectedRegion.name));
            // Nam add: Save spawn region bounds for traffic light checks
            vehicle.setSpawnRegionBounds(selectedRegion.minX, selectedRegion.minY,
                    selectedRegion.maxX, selectedRegion.maxY);
            //Nam add: Save intersection center for region boundary checks
            vehicle.setSpawnRegionCenter(mapSystem.getMap().getIntersectionCenterX(),
                    mapSystem.getMap().getIntersectionCenterY());

            vehicleManager.addVehicle(vehicle);
        }
    }
    
    // Nam adding: Method to assign random turn directions based on spawn region
    private void assignRandomTurnDirection(Vehicle vehicle, String regionName) {
        // Nam adding: Decide straight vs turn once at spawn time
        double rand = Math.random();
        
        if (rand < 0.3) {
            vehicle.setPlannedTurn(Vehicle.TurnDirection.LEFT);
        } else if (rand < 0.6) {
            vehicle.setPlannedTurn(Vehicle.TurnDirection.RIGHT);
        } else {
            vehicle.setPlannedTurn(Vehicle.TurnDirection.STRAIGHT);
        }
    }

    // Nam add: Convert region name to Region enum
    private Region parseRegion(String regionName) {
        if ("NORTH".equals(regionName)) {
            return Region.NORTH;
        }
        if ("SOUTH".equals(regionName)) {
            return Region.SOUTH;
        }
        if ("WEST".equals(regionName)) {
            return Region.WEST;
        }
        return Region.EAST;
    }
    
    // Nam adding: Static variables for region cycling
    private static int regionCounter = 0;
    private static boolean hasStaticCounter = false;
    
    // Nam adding: Helper class to define spawn regions
    private static class SpawnRegion {
        int minX, minY, maxX, maxY;
        double dirX, dirY;
        String name; // Nam adding: Region name for debugging
        
        SpawnRegion(int minX, int minY, int maxX, int maxY, double dirX, double dirY, String name) {
            this.minX = minX;
            this.minY = minY; 
            this.maxX = maxX;
            this.maxY = maxY;
            this.dirX = dirX;
            this.dirY = dirY;
            this.name = name; // Nam adding: Store region name
        }
    }

    // Nam adding: Bounds for intersection detection
    private static class RegionBounds {
        int minX, minY, maxX, maxY;

        RegionBounds(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    // Nam adding: Find intersection bounds from the map
    private RegionBounds findIntersectionBounds() {
        //Nam add: Use map-provided intersection bounds for consistent regions
        int[] bounds = mapSystem.getMap().getIntersectionBounds();
        return new RegionBounds(bounds[0], bounds[1], bounds[2], bounds[3]);
    }

    // Nam fix: Build 4 region spawns around the intersection for edge spawning
    private SpawnRegion[] buildRegionSpawns(RegionBounds intersection) {
        int width = mapSystem.getMap().getWidth();
        int height = mapSystem.getMap().getHeight();

        return new SpawnRegion[] {
            // Nam fix: NORTH region - from map top edge to intersection
            new SpawnRegion(intersection.minX, 0, intersection.maxX + 1, intersection.minY, 0, 1, "NORTH"),
            // Nam fix: WEST region - from map left edge to intersection  
            new SpawnRegion(0, intersection.minY, intersection.minX, intersection.maxY + 1, 1, 0, "WEST"),
            // Nam fix: SOUTH region - from intersection to map bottom edge
            new SpawnRegion(intersection.minX, intersection.maxY + 1, intersection.maxX + 1, height, 0, -1, "SOUTH"),
            // Nam fix: EAST region - from intersection to map right edge
            new SpawnRegion(intersection.maxX + 1, intersection.minY, width, intersection.maxY + 1, -1, 0, "EAST")
        };
    }

    // Nam fix: Pick spawn point at map edge (where vehicles naturally enter from outside)
    private int[] pickSpawnPoint(SpawnRegion region) {
        // Nam fix: Spawn vehicles at the map edge based on their region
        if ("NORTH".equals(region.name)) {
            // Nam fix: NORTH region - spawn at map top edge (y=0), moving south
            return findEdgeSpawnPoint(region.minX, region.maxX, 0, 1, region.name);
        } else if ("SOUTH".equals(region.name)) {
            // Nam fix: SOUTH region - spawn at map bottom edge (y=height-1), moving north
            int mapHeight = mapSystem.getMap().getHeight();
            return findEdgeSpawnPoint(region.minX, region.maxX, mapHeight - 1, mapHeight, region.name);
        } else if ("WEST".equals(region.name)) {
            // Nam fix: WEST region - spawn at map left edge (x=0), moving east
            return findEdgeSpawnPoint(0, 1, region.minY, region.maxY, region.name);
        } else { // EAST
            // Nam fix: EAST region - spawn at map right edge (x=width-1), moving west
            int mapWidth = mapSystem.getMap().getWidth();
            return findEdgeSpawnPoint(mapWidth - 1, mapWidth, region.minY, region.maxY, region.name);
        }
    }
    
    // Nam fix: Find valid spawn point along the edge
    private int[] findEdgeSpawnPoint(int minX, int maxX, int minY, int maxY, String regionName) {
        int attempts = 20;
        
        // Nam fix: Try random positions along the edge first
        for (int i = 0; i < attempts; i++) {
            int x = minX + (int) (Math.random() * Math.max(1, maxX - minX));
            int y = minY + (int) (Math.random() * Math.max(1, maxY - minY));
            
            if (mapSystem.getMap().isValid(x, y) && mapSystem.getMap().isDriveable(x, y)
                    && isCorrectLaneForRegion(x, y, regionName)) {
                return new int[] { x, y };
            }
        }
        
        // Nam fix: Fallback - scan edge systematically
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                if (mapSystem.getMap().isValid(x, y) && mapSystem.getMap().isDriveable(x, y)
                        && isCorrectLaneForRegion(x, y, regionName)) {
                    return new int[] { x, y };
                }
            }
        }
        
        return null;
    }

    // Nam add: Ensure spawn point is on the correct side of the road for region direction
    private boolean isCorrectLaneForRegion(int x, int y, String regionName) {
        double centerX = mapSystem.getMap().getIntersectionCenterX();
        double centerY = mapSystem.getMap().getIntersectionCenterY();
        if ("NORTH".equals(regionName)) {
            // Nam add: Vehicles from NORTH move south, keep to west side of vertical road
            return x < Math.floor(centerX);
        }
        if ("SOUTH".equals(regionName)) {
            // Nam add: Vehicles from SOUTH move north, keep to east side of vertical road
            return x >= Math.ceil(centerX);
        }
        if ("WEST".equals(regionName)) {
            // Nam add: Vehicles from WEST move east, keep to south side of horizontal road
            return y >= Math.ceil(centerY);
        }
        // Nam add: EAST region vehicles move west, keep to north side of horizontal road
        return y < Math.floor(centerY);
    }
}
