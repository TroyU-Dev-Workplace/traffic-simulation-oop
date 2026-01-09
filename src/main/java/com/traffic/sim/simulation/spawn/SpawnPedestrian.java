package com.traffic.sim.simulation.spawn;

import com.traffic.sim.simulation.entities.Direction;
import com.traffic.sim.simulation.entities.Pedestrian;
import com.traffic.sim.simulation.managers.MapSystem;
import com.traffic.sim.simulation.managers.PedestrianManager;
import java.util.UUID;

public class SpawnPedestrian extends SpawnBase {
    private PedestrianManager pedestrianManager;

    public SpawnPedestrian(MapSystem mapSystem, PedestrianManager pedestrianManager) {
        super(mapSystem);
        this.pedestrianManager = pedestrianManager;
    }

    @Override
    public void spawn() {
        // Spawn pedestrians on sidewalks in different regions
        spawnInRegion("NORTH"); // Top sidewalks
        spawnInRegion("SOUTH"); // Bottom sidewalks  
        spawnInRegion("WEST");  // Left sidewalks
        spawnInRegion("EAST");  // Right sidewalks
    }
    
    private void spawnInRegion(String regionName) {
        int[] spawnBounds = getSidewalkBoundsForRegion(regionName);
        if (spawnBounds == null) return;
        
        // Try to find a valid sidewalk position in the region
        for (int attempt = 0; attempt < 20; attempt++) {
            int x = spawnBounds[0] + (int)(Math.random() * (spawnBounds[2] - spawnBounds[0] + 1));
            int y = spawnBounds[1] + (int)(Math.random() * (spawnBounds[3] - spawnBounds[1] + 1));
            
            if (mapSystem.getMap().isSidewalk(x, y)) {
                String id = UUID.randomUUID().toString();
                // Set direction towards nearest crosswalk
                Direction direction = getDirectionTowardsCrosswalk(x, y, regionName);
                
                Pedestrian pedestrian = new Pedestrian(id, x + 0.5, y + 0.5, 0.02, direction);
                pedestrianManager.addPedestrian(pedestrian);
                break; // Only spawn one pedestrian per region per call
            }
        }
    }
    
    private int[] getSidewalkBoundsForRegion(String regionName) {
        switch (regionName) {
            case "NORTH":
                // Top sidewalks: around buildings in upper area
                return new int[]{0, 8, 49, 9}; // x1, y1, x2, y2
            case "SOUTH": 
                // Bottom sidewalks: around buildings in lower area
                return new int[]{0, 26, 49, 27};
            case "WEST":
                // Left sidewalks: around buildings on left side
                return new int[]{15, 0, 16, 35};
            case "EAST":
                // Right sidewalks: around buildings on right side  
                return new int[]{33, 0, 34, 35};
            default:
                return null;
        }
    }
    
    private Direction getDirectionTowardsCrosswalk(int x, int y, String regionName) {
        // Move pedestrians towards crosswalk areas based on their region
        switch (regionName) {
            case "NORTH":
                return Direction.DOWN; // Move towards crosswalk
            case "SOUTH":
                return Direction.UP;   // Move towards crosswalk
            case "WEST":
                return Direction.RIGHT; // Move towards crosswalk
            case "EAST":
                return Direction.LEFT;  // Move towards crosswalk
            default:
                return Direction.DOWN; // Default direction
        }
    }
}
