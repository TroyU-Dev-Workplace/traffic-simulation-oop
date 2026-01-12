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

        // Try up to 4 regions starting from the current counter
        for (int i = 0; i < 4; i++) {
            SpawnRegion selectedRegion = spawnRegions[(regionCounter + i) % 4];

            // Only advance the global counter if we actually spawn (or try the primary one
            // first)
            if (i == 0) {
                regionCounter = (regionCounter + 1) % 4;
            }

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

            // Pass width/height for collision check
            boolean isHorizontal = (selectedRegion.dirX != 0);
            int vehicleWidth = isHorizontal ? type.getWidthCells() : type.getHeightCells();
            int vehicleHeight = isHorizontal ? type.getHeightCells() : type.getWidthCells();

            int[] spawnPoint = pickSpawnPoint(selectedRegion, vehicleWidth, vehicleHeight);

            if (spawnPoint != null) {
                Direction direction = getDirectionFromRegion(selectedRegion);

                double spawnX = spawnPoint[0];
                double spawnY = spawnPoint[1];

                if (vehicleWidth % 2 != 0) {
                    spawnX += 0.5;
                }
                if (vehicleHeight % 2 != 0) {
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
                return; // Successfully spawned, exit
            }
        }
        // If we reach here, we failed to spawn in all 4 regions (traffic jam?)
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

    private int[] pickSpawnPoint(SpawnRegion region, int widthCells, int heightCells) {
        int width = mapSystem.getMap().getWidth();
        int height = mapSystem.getMap().getHeight();

        int spawnMinX = 0, spawnMaxX = 0;
        int spawnMinY = 0, spawnMaxY = 0;

        int lateralSize = heightCells; // The thickness relative to lane
        int startShift = lateralSize / 2;
        int endShift = (lateralSize - 1) / 2;

        if ("NORTH".equals(region.name)) {
            spawnMinX = 17 + startShift;
            spawnMaxX = 24 - endShift;
            spawnMinY = 0;
            spawnMaxY = 2; // Increased range slightly to allow more attempts
        } else if ("SOUTH".equals(region.name)) {
            spawnMinX = 25 + startShift;
            spawnMaxX = 32 - endShift;
            spawnMinY = height - 3;
            spawnMaxY = height - 1;
        } else if ("WEST".equals(region.name)) {
            spawnMinX = 0;
            spawnMaxX = 2;
            spawnMinY = 18 + startShift;
            spawnMaxY = 25 - endShift;
        } else if ("EAST".equals(region.name)) {
            spawnMinX = width - 3;
            spawnMaxX = width - 1;
            spawnMinY = 10 + startShift;
            spawnMaxY = 17 - endShift;
        } else {
            return null;
        }

        // Try random positions first
        for (int i = 0; i < 15; i++) {
            int x = spawnMinX + (int) (Math.random() * (spawnMaxX - spawnMinX + 1));
            int y = spawnMinY + (int) (Math.random() * (spawnMaxY - spawnMinY + 1));

            if (isValidSpawnPoint(x, y, widthCells, heightCells)) {
                return new int[] { x, y };
            }
        }

        // Fallback: sweep search
        for (int x = spawnMinX; x <= spawnMaxX; x++) {
            for (int y = spawnMinY; y <= spawnMaxY; y++) {
                if (isValidSpawnPoint(x, y, widthCells, heightCells)) {
                    return new int[] { x, y };
                }
            }
        }

        return null;
    }

    private boolean isValidSpawnPoint(int x, int y, int w, int h) {
        if (!mapSystem.getMap().isValid(x, y))
            return false;

        // Check driveable
        if (!mapSystem.getMap().isDriveable(x, y) && !mapSystem.getMap().isStopLine(x, y)) {
            return false;
        }

        // Check collision with existing vehicles
        // Calculate the proposed center of the vehicle
        double centerX = x + (w % 2 != 0 ? 0.5 : 0.0);
        double centerY = y + (h % 2 != 0 ? 0.5 : 0.0);

        return isAreaClear(centerX, centerY, w, h);
    }

    private boolean isAreaClear(double x, double y, double width, double height) {
        java.util.List<Vehicle> vehicles = vehicleManager.getVehicles();
        double buffer = 2.5; // Keeping safe distance

        for (Vehicle v : vehicles) {
            double vx = v.getX();
            double vy = v.getY();
            double vw = v.getCurrentWidthCells();
            double vh = v.getCurrentHeightCells();

            // Check overlap
            // We can check if the bounding boxes overlap with buffer
            if (Math.abs(vx - x) < (width + vw) / 2.0 + buffer &&
                    Math.abs(vy - y) < (height + vh) / 2.0 + buffer) {
                return false;
            }
        }
        return true;
    }
}
