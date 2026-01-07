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
        // Spawn on sidewalk (tile type 2)
        int width = mapSystem.getMap().getWidth();
        int height = mapSystem.getMap().getHeight();

        for (int i = 0; i < 10; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);

            if (mapSystem.getMap().getTileType(x, y) == 2) {
                String id = UUID.randomUUID().toString();
                // Random direction
                Direction[] directions = Direction.values();
                Direction direction = directions[(int) (Math.random() * directions.length)];
                pedestrianManager.addPedestrian(new Pedestrian(id, x + 0.5, y + 0.5, 0.02, direction));
                break;
            }
        }
    }
}
