package com.traffic.sim.simulation.spawn;

import com.traffic.sim.simulation.entities.Direction;
import com.traffic.sim.simulation.entities.Vehicle;
import com.traffic.sim.simulation.entities.VehicleType;
import com.traffic.sim.simulation.managers.MapSystem;
import com.traffic.sim.simulation.managers.VehicleManager;
import com.traffic.sim.simulation.entities.Region;
import java.util.UUID;

public class SpawnVehicle extends SpawnBase {
    private VehicleManager vehicleManager;

    public SpawnVehicle(MapSystem mapSystem, VehicleManager vehicleManager) {
        super(mapSystem);
        this.vehicleManager = vehicleManager;
    }

    @Override
    public void spawn() {
        RegionBounds intersection = findIntersectionBounds();
        SpawnRegion[] spawnRegions = buildRegionSpawns(intersection);

        if (!hasStaticCounter) {
            regionCounter = 0;
            hasStaticCounter = true;
        }

        SpawnRegion selectedRegion = spawnRegions[regionCounter % 4];
        regionCounter = (regionCounter + 1) % 4;

        String id = UUID.randomUUID().toString();

        double typeRand = Math.random();
        VehicleType type;
        if (typeRand < 0.45) {
            type = VehicleType.CAR;
        } else if (typeRand < 0.90) {
            type = VehicleType.MOTORCYCLE;
        } else {
            type = VehicleType.TRUCK;
        }

        int[] spawnPoint = pickSpawnPoint(selectedRegion, type);
        if (spawnPoint != null) {

            Direction direction = getDirectionFromRegion(selectedRegion);

            double spawnX = spawnPoint[0];
            double spawnY = spawnPoint[1];

            boolean isHorizontal = (direction == Direction.RIGHT || direction == Direction.LEFT);
            int effectiveWidth = isHorizontal ? type.getWidthCells() : type.getHeightCells();
            int effectiveHeight = isHorizontal ? type.getHeightCells() : type.getWidthCells();

            if (effectiveWidth % 2 != 0) {
                spawnX += 0.5;
            }
            if (effectiveHeight % 2 != 0) {
                spawnY += 0.5;
            }

            Vehicle vehicle = new Vehicle(id, spawnX, spawnY, 0.05, type, direction);
            vehicle.setDirection(selectedRegion.dirX, selectedRegion.dirY);

            setVehicleRotation(vehicle, selectedRegion.name);
            assignRandomTurnDirection(vehicle, selectedRegion.name);

            vehicle.setSpawnRegion(parseRegion(selectedRegion.name));
            vehicle.setSpawnRegionBounds(selectedRegion.minX, selectedRegion.minY,
                    selectedRegion.maxX, selectedRegion.maxY);
            vehicle.setSpawnRegionCenter(mapSystem.getMap().getIntersectionCenterX(),
                    mapSystem.getMap().getIntersectionCenterY());

            vehicleManager.addVehicle(vehicle);
        }
    }

    private Direction getDirectionFromRegion(SpawnRegion region) {
        if (region.dirX > 0)
            return Direction.RIGHT;
        if (region.dirX < 0)
            return Direction.LEFT;
        if (region.dirY > 0)
            return Direction.DOWN;
        if (region.dirY < 0)
            return Direction.UP;
        return Direction.RIGHT; // default
    }

    private void assignRandomTurnDirection(Vehicle vehicle, String regionName) {
        double rand = Math.random();

        if (rand < 0.33) {
            vehicle.setPlannedTurn(Vehicle.TurnDirection.STRAIGHT);
        } else if (rand < 0.66) {
            vehicle.setPlannedTurn(Vehicle.TurnDirection.LEFT);
        } else {
            vehicle.setPlannedTurn(Vehicle.TurnDirection.RIGHT);
        }
    }

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

    private void setVehicleRotation(Vehicle vehicle, String regionName) {
        switch (regionName) {
            case "NORTH":
                vehicle.setRotation(90.0);
                break;
            case "WEST":
                vehicle.setRotation(0.0);
                break;
            case "SOUTH":
                vehicle.setRotation(-90.0);
                break;
            case "EAST":
                vehicle.setRotation(180.0);
                break;
            default:
                vehicle.setRotation(0.0);
        }
    }

    private static int regionCounter = 0;
    private static boolean hasStaticCounter = false;

    private static class SpawnRegion {
        int minX, minY, maxX, maxY;
        double dirX, dirY;
        String name;

        SpawnRegion(int minX, int minY, int maxX, int maxY, double dirX, double dirY, String name) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.dirX = dirX;
            this.dirY = dirY;
            this.name = name;
        }
    }

    private static class RegionBounds {
        int minX, minY, maxX, maxY;

        RegionBounds(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    private RegionBounds findIntersectionBounds() {
        int[] bounds = mapSystem.getMap().getIntersectionBounds();
        return new RegionBounds(bounds[0], bounds[1], bounds[2], bounds[3]);
    }

    private SpawnRegion[] buildRegionSpawns(RegionBounds intersection) {
        int width = mapSystem.getMap().getWidth();
        int height = mapSystem.getMap().getHeight();

        return new SpawnRegion[] {
                new SpawnRegion(intersection.minX, 0, intersection.maxX + 1, intersection.minY, 0, 1, "NORTH"),
                new SpawnRegion(0, intersection.minY, intersection.minX, intersection.maxY + 1, 1, 0, "WEST"),
                new SpawnRegion(intersection.minX, intersection.maxY + 1, intersection.maxX + 1, height, 0, -1,
                        "SOUTH"),
                new SpawnRegion(intersection.maxX + 1, intersection.minY, width, intersection.maxY + 1, -1, 0, "EAST")
        };
    }

    private int[] pickSpawnPoint(SpawnRegion region, VehicleType type) {
        int width = mapSystem.getMap().getWidth();
        int height = mapSystem.getMap().getHeight();

        int spawnMinX = 0, spawnMaxX = 0;
        int spawnMinY = 0, spawnMaxY = 0;

        int lateralSize = type.getHeightCells();
        int startShift = lateralSize / 2;
        int endShift = (lateralSize - 1) / 2;

        if ("NORTH".equals(region.name)) {
            spawnMinX = 17 + startShift;
            spawnMaxX = 24 - endShift;
            spawnMinY = 0;
            spawnMaxY = 0;
        } else if ("SOUTH".equals(region.name)) {
            spawnMinX = 25 + startShift;
            spawnMaxX = 32 - endShift;
            spawnMinY = height - 1;
            spawnMaxY = height - 1;
        } else if ("WEST".equals(region.name)) {
            spawnMinX = 0;
            spawnMaxX = 0;
            spawnMinY = 18 + startShift;
            spawnMaxY = 25 - endShift;
        } else if ("EAST".equals(region.name)) {
            spawnMinX = width - 1;
            spawnMaxX = width - 1;
            spawnMinY = 10 + startShift;
            spawnMaxY = 17 - endShift;
        } else {
            return null;
        }

        for (int i = 0; i < 10; i++) {
            int x = spawnMinX + (int) (Math.random() * (spawnMaxX - spawnMinX + 1));
            int y = spawnMinY + (int) (Math.random() * (spawnMaxY - spawnMinY + 1));

            if (mapSystem.getMap().isValid(x, y) &&
                    (mapSystem.getMap().isDriveable(x, y) || mapSystem.getMap().isStopLine(x, y))) {
                return new int[] { x, y };
            }
        }

        for (int x = spawnMinX; x <= spawnMaxX; x++) {
            for (int y = spawnMinY; y <= spawnMaxY; y++) {
                if (mapSystem.getMap().isValid(x, y) &&
                        (mapSystem.getMap().isDriveable(x, y) || mapSystem.getMap().isStopLine(x, y))) {
                    return new int[] { x, y };
                }
            }
        }

        return null;
    }
}
