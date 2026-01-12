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
        // Spawn randomly picked strict location
        int choice = (int) (Math.random() * 4);

        // Data derived from DescriptionPedestrian.md
        // NW: y=8-9, x=14-15 -> Dir DOWN -> Region WEST
        // NE: y=8-9, x=35-36 -> Dir LEFT -> Region NORTH
        // SE: y=26-27, x=35-36 -> Dir UP -> Region EAST
        // SW: y=26-27, x=14-15 -> Dir RIGHT -> Region SOUTH

        int x = 0;
        int y = 0;
        Direction dir = Direction.DOWN;
        com.traffic.sim.simulation.entities.Region regionStr = null;

        switch (choice) {
            case 0: // NW
                x = 14;
                y = 8;
                dir = Direction.DOWN;
                regionStr = com.traffic.sim.simulation.entities.Region.WEST;
                break;
            case 1: // NE
                x = 35;
                y = 7;
                dir = Direction.LEFT;
                regionStr = com.traffic.sim.simulation.entities.Region.NORTH;
                break;
            case 2: // SE
                x = 34;
                y = 26;
                dir = Direction.UP;
                regionStr = com.traffic.sim.simulation.entities.Region.EAST;
                break;
            case 3: // SW
                x = 14;
                y = 27;
                dir = Direction.RIGHT;
                regionStr = com.traffic.sim.simulation.entities.Region.SOUTH;
                break;
        }

        // Create Pedestrian
        // Check strict grid validity just in case
        if (mapSystem.getMap().isSidewalk(x, y) || mapSystem.getMap().isCrosswalk(x, y)) {
            String id = UUID.randomUUID().toString();
            // Center in tile
            double spawnX = x + 0.5;
            double spawnY = y + 0.5;
            double speed = 0.03; // Adjusted speed

            Pedestrian p = new Pedestrian(id, spawnX, spawnY, speed, dir, regionStr);
            pedestrianManager.addPedestrian(p);
        }
    }
}
