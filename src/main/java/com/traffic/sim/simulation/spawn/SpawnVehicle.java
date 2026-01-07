package com.traffic.sim.simulation.spawn;

import com.traffic.sim.simulation.entities.Direction;
import com.traffic.sim.simulation.entities.Vehicle;
import com.traffic.sim.simulation.entities.VehicleType;
import com.traffic.sim.simulation.managers.MapSystem;
import com.traffic.sim.simulation.managers.VehicleManager;
import java.util.UUID;

public class SpawnVehicle extends SpawnBase {
    private VehicleManager vehicleManager;

    public SpawnVehicle(MapSystem mapSystem, VehicleManager vehicleManager) {
        super(mapSystem);
        this.vehicleManager = vehicleManager;
    }

    @Override
    public void spawn() {
        // Simple logic: Spawn at a random location on the road (tile type 0)
        // For simplicity in this starter code, we try 10 times to find a valid spot

        int width = mapSystem.getMap().getWidth();
        int height = mapSystem.getMap().getHeight();

        for (int i = 0; i < 10; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);

            if (mapSystem.getMap().getTileType(x, y) == 0) {
                // Found a road
                String id = UUID.randomUUID().toString();
                // Random vehicle type
                VehicleType[] types = VehicleType.values();
                VehicleType type = types[(int) (Math.random() * types.length)];
                // Random direction
                Direction[] directions = Direction.values();
                Direction direction = directions[(int) (Math.random() * directions.length)];
                // Spawn in the center of the tile
                vehicleManager.addVehicle(new Vehicle(id, x + 0.5, y + 0.5, 0.05, type, direction));
                break;
            }
        }
    }
}
