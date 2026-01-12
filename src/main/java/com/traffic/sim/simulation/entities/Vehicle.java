package com.traffic.sim.simulation.entities;

import java.util.ArrayList;
import java.util.List;

import com.traffic.sim.simulation.managers.TrafficLightSystem;
import com.traffic.sim.simulation.map.Map;

/**
 * Represents a vehicle in the simulation.
 * Handles state, movement, and interaction with the map/traffic lights.
 */
public class Vehicle {

    public enum TurnDirection {
        STRAIGHT, LEFT, RIGHT
    }

    // Lane centers for turning logic
    private static final double LANE_CENTER_SOUTH = 21.0;
    private static final double LANE_CENTER_NORTH = 28.0;
    private static final double LANE_CENTER_WEST = 14.0;
    private static final double LANE_CENTER_EAST = 21.0;

    // Time per frame (assuming 60 FPS)
    private static final double FRAME_DURATION = 1.0 / 60.0;

    private final String id;
    private double x;
    private double y;
    private double speed;
    private Direction direction;
    private final VehicleType type;
    private String spritePath;

    private double originalSpeed;
    private boolean isStoppedAtLight;

    private TurnDirection plannedTurn;

    // Spawn region metadata (used for metrics/verification)
    private Region spawnRegion;
    private int spawnRegionMinX;
    private int spawnRegionMinY;
    private int spawnRegionMaxX;
    private int spawnRegionMaxY;
    private boolean spawnRegionBoundsSet;
    private Double spawnRegionCenterX;
    private Double spawnRegionCenterY;

    private boolean shouldBeRemoved = false;
    private double rotation = 0.0;

    // Metric Data
    private double totalCO2;
    private int waitingFrames;
    private final long entryTime;

    private long spawnTime; // Time in ms
    private double accumulatedWaitingTime; // Seconds
    private int stopCount;
    private boolean isMoving;
    private boolean wasStopped; // Helper to detect stop transitions

    private List<GridPoint> occupiedCells = new ArrayList<>();

    private static class GridPoint {
        int x, y;
        int originalType;

        GridPoint(int x, int y, int originalType) {
            this.x = x;
            this.y = y;
            this.originalType = originalType;
        }
    }

    public Vehicle(String id, double x, double y, double speed, VehicleType type, Direction direction) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.type = type;
        this.direction = direction;

        this.entryTime = System.currentTimeMillis();

        // Init metrics
        this.spawnTime = System.currentTimeMillis();
        this.accumulatedWaitingTime = 0.0;
        this.stopCount = 0;
        this.isMoving = true;
        this.wasStopped = false;

        this.originalSpeed = speed;
        this.isStoppedAtLight = false;

        this.plannedTurn = TurnDirection.STRAIGHT;
        this.spawnRegion = null;

        this.spawnRegionMinX = Integer.MIN_VALUE;
        this.spawnRegionMinY = Integer.MIN_VALUE;
        this.spawnRegionMaxX = Integer.MAX_VALUE;
        this.spawnRegionMaxY = Integer.MAX_VALUE;
        this.spawnRegionBoundsSet = false;
        this.spawnRegionCenterX = null;
        this.spawnRegionCenterY = null;

        initializeSprite();
    }

    public Vehicle(String id, double x, double y, double speed) {
        this(id, x, y, speed, VehicleType.CAR, Direction.RIGHT);
    }

    private void initializeSprite() {
        int index;
        switch (type) {
            case CAR:
                index = 1 + (int) (Math.random() * 18); // 1-18
                this.spritePath = "/assets/vehicles/Car" + index + ".png";
                break;
            case TRUCK:
                index = 19 + (int) (Math.random() * 2); // 19-20
                this.spritePath = "/assets/vehicles/Car" + index + ".png";
                break;
            case MOTORCYCLE:
                index = 1 + (int) (Math.random() * 4); // 1-4
                this.spritePath = "/assets/vehicles/moto" + index + ".png";
                break;
            default:
                this.spritePath = "/assets/vehicles/Car1.png";
        }
    }

    public void setDirection(double dx, double dy) {
        if (dx > 0) {
            this.direction = Direction.RIGHT;
        } else if (dx < 0) {
            this.direction = Direction.LEFT;
        } else if (dy > 0) {
            this.direction = Direction.DOWN;
        } else if (dy < 0) {
            this.direction = Direction.UP;
        }
    }

    // --- Getters & Setters ---

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public VehicleType getType() {
        return type;
    }

    public String getSpritePath() {
        return spritePath;
    }

    public double getWidth() {
        return type.getWidthCells();
    }

    public double getHeight() {
        return type.getHeightCells();
    }

    public double getTotalCO2() {
        return totalCO2;
    }

    public int getWaitingFrames() {
        return waitingFrames;
    }

    public long getTravelTime() {
        return System.currentTimeMillis() - entryTime;
    }

    public double getArea() {
        return type.getWidthCells() * type.getHeightCells();
    }

    public int getLengthInCells() {
        return type.getHeightCells();
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public double getAccumulatedWaitingTime() {
        return accumulatedWaitingTime;
    }

    public int getStopCount() {
        return stopCount;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getRotation() {
        return rotation;
    }

    public boolean isStoppedAtLight() {
        return isStoppedAtLight;
    }

    public double getOriginalSpeed() {
        return originalSpeed;
    }

    public double getDirectionX() {
        return direction.dx();
    }

    public double getDirectionY() {
        return direction.dy();
    }

    public boolean shouldBeRemoved() {
        return shouldBeRemoved;
    }

    public void setPlannedTurn(TurnDirection turn) {
        this.plannedTurn = turn;
    }

    // --- Spawn Region Logic ---

    public void setSpawnRegion(Region region) {
        this.spawnRegion = region;
    }

    public void setSpawnRegionBounds(int minX, int minY, int maxX, int maxY) {
        this.spawnRegionMinX = minX;
        this.spawnRegionMinY = minY;
        this.spawnRegionMaxX = maxX;
        this.spawnRegionMaxY = maxY;
        this.spawnRegionBoundsSet = true;
    }

    public void setSpawnRegionCenter(double centerX, double centerY) {
        this.spawnRegionCenterX = centerX;
        this.spawnRegionCenterY = centerY;
    }

    public boolean isInsideSpawnRegion() {
        if (spawnRegionBoundsSet) {
            return x >= spawnRegionMinX && x <= spawnRegionMaxX
                    && y >= spawnRegionMinY && y <= spawnRegionMaxY;
        }
        if (spawnRegion != null && spawnRegionCenterX != null && spawnRegionCenterY != null) {
            switch (spawnRegion) {
                case NORTH:
                    return y <= spawnRegionCenterY;
                case SOUTH:
                    return y >= spawnRegionCenterY;
                case WEST:
                    return x <= spawnRegionCenterX;
                case EAST:
                    return x >= spawnRegionCenterX;
            }
        }
        return true;
    }

    // --- Core Logic ---

    /**
     * Updates the vehicle's position and logic interacting with the map and traffic
     * lights.
     * Handles movement validation, turning, and stop-line behaviors.
     */
    public void updateWithMap(Map map, TrafficLightSystem tls) {
        boolean didMove = false;

        if (speed > 0 || isStoppedAtLight) {
            // Turning Logic
            if (plannedTurn != TurnDirection.STRAIGHT && !isStoppedAtLight && speed > 0) {
                checkAndExecuteTurn(map);
            }

            double nextX = x + direction.dx() * speed;
            double nextY = y + direction.dy() * speed;

            // Anti-Deadlock: Don't enter intersection if exit blocked
            if (willEnterIntersection(x, y, nextX, nextY, map)) {
                if (isExitBlocked(map)) {
                    this.speed = 0;
                    trackStoppedState();
                    return;
                }
            }

            // Check Map Bounds
            List<GridPoint> nextOccupied = getTargetCells(nextX, nextY);
            boolean outOfBounds = false;
            for (GridPoint p : nextOccupied) {
                if (!map.isValid(p.x, p.y)) {
                    outOfBounds = true;
                    break;
                }
            }

            // Allow moving if still inside designated spawn region (entry phase)
            if (outOfBounds) {
                if (isInsideSpawnRegion()) {
                    outOfBounds = false;
                }
            }

            // Exit Simulation
            if (outOfBounds) {
                clearOccupiedCells(map);
                this.shouldBeRemoved = true;
                return;
            }

            // Stop Line Logic
            double frontDist = type.getWidthCells() / 2.0;
            int nextGridX = (int) Math.floor(nextX + direction.dx() * (frontDist + 0.5));
            int nextGridY = (int) Math.floor(nextY + direction.dy() * (frontDist + 0.5));
            int nextTileType = map.getTileType(nextGridX, nextGridY);

            if (nextTileType == Map.STOP_LINE) {
                int currentGridX = (int) Math.floor(x);
                int currentGridY = (int) Math.floor(y);

                if (map.getTileType(currentGridX, currentGridY) != Map.STOP_LINE) {
                    // Only stop if NOT already inside intersection
                    if (!isInsideIntersection(x, y, map)) {
                        TrafficLight.State lightState = getTrafficLightState(tls);
                        if (lightState != TrafficLight.State.GREEN) {
                            this.speed = 0;
                            this.isStoppedAtLight = true;
                            trackStoppedState();
                            return;
                        }
                    }
                }
            }

            // Move if clear
            if (canMoveTo(map, nextX, nextY)) {
                this.speed = this.originalSpeed;
                this.isStoppedAtLight = false;

                updateMapOccupancy(map, nextX, nextY);
                this.x = nextX;
                this.y = nextY;
                this.totalCO2 += type.getEmissionMoving();
                didMove = true;
            } else {
                this.speed = 0;
            }
        } else {
            // Stopped Logic
            if (isStoppedAtLight) {
                TrafficLight.State lightState = getTrafficLightState(tls);
                if (lightState == TrafficLight.State.GREEN) {
                    this.speed = this.originalSpeed;
                    this.isStoppedAtLight = false;
                }
            } else {
                // Resume from obstacle stop
                double nextX = x + direction.dx() * originalSpeed;
                double nextY = y + direction.dy() * originalSpeed;
                if (canMoveTo(map, nextX, nextY)) {
                    this.speed = this.originalSpeed;
                }
            }

            waitingFrames++;
            totalCO2 += type.getEmissionIdling();
        }

        if (didMove) {
            trackMovingState();
        } else if (speed == 0) {
            trackStoppedState();
        }
    }

    private void trackMovingState() {
        isMoving = true;
        if (wasStopped) {
            wasStopped = false;
        }
    }

    private void trackStoppedState() {
        isMoving = false;
        accumulatedWaitingTime += FRAME_DURATION;

        if (!wasStopped) {
            stopCount++;
            wasStopped = true;
        }
    }

    private void checkAndExecuteTurn(Map map) {
        boolean turned = false;
        Direction newDir = direction;
        double newRot = rotation;
        double snapX = x;
        double snapY = y;

        if (direction == Direction.DOWN) {
            if (y >= LANE_CENTER_EAST && plannedTurn == TurnDirection.LEFT) {
                snapY = LANE_CENTER_EAST;
                newDir = Direction.RIGHT;
                newRot = 0.0;
                turned = true;
            } else if (y >= LANE_CENTER_WEST && plannedTurn == TurnDirection.RIGHT) {
                snapY = LANE_CENTER_WEST;
                newDir = Direction.LEFT;
                newRot = 180.0;
                turned = true;
            }
        } else if (direction == Direction.UP) {
            if (y <= LANE_CENTER_WEST && plannedTurn == TurnDirection.LEFT) {
                snapY = LANE_CENTER_WEST;
                newDir = Direction.LEFT;
                newRot = 180.0;
                turned = true;
            } else if (y <= LANE_CENTER_EAST && plannedTurn == TurnDirection.RIGHT) {
                snapY = LANE_CENTER_EAST;
                newDir = Direction.RIGHT;
                newRot = 0.0;
                turned = true;
            }
        } else if (direction == Direction.RIGHT) {
            if (x >= LANE_CENTER_NORTH && plannedTurn == TurnDirection.LEFT) {
                snapX = LANE_CENTER_NORTH;
                newDir = Direction.UP;
                newRot = -90.0;
                turned = true;
            } else if (x >= LANE_CENTER_SOUTH && plannedTurn == TurnDirection.RIGHT) {
                snapX = LANE_CENTER_SOUTH;
                newDir = Direction.DOWN;
                newRot = 90.0;
                turned = true;
            }
        } else if (direction == Direction.LEFT) {
            if (x <= LANE_CENTER_SOUTH && plannedTurn == TurnDirection.LEFT) {
                snapX = LANE_CENTER_SOUTH;
                newDir = Direction.DOWN;
                newRot = 90.0;
                turned = true;
            } else if (x <= LANE_CENTER_NORTH && plannedTurn == TurnDirection.RIGHT) {
                snapX = LANE_CENTER_NORTH;
                newDir = Direction.UP;
                newRot = -90.0;
                turned = true;
            }
        }

        if (turned) {
            boolean isHoriz = (newDir == Direction.RIGHT || newDir == Direction.LEFT);
            int newW = isHoriz ? type.getWidthCells() : type.getHeightCells();
            int newH = isHoriz ? type.getHeightCells() : type.getWidthCells();

            snapX = (newW % 2 != 0) ? Math.floor(snapX) + 0.5 : Math.floor(snapX + 0.5);
            snapY = (newH % 2 != 0) ? Math.floor(snapY) + 0.5 : Math.floor(snapY + 0.5);

            clearOccupiedCells(map);

            this.x = snapX;
            this.y = snapY;
            this.direction = newDir;
            this.rotation = newRot;
            this.plannedTurn = TurnDirection.STRAIGHT;

            updateMapOccupancy(map, this.x, this.y);
        }
    }

    private TrafficLight.State getTrafficLightState(TrafficLightSystem tls) {
        if (tls == null)
            return TrafficLight.State.GREEN;

        Region targetRegion;
        switch (this.direction) {
            case UP:
                targetRegion = Region.NORTH;
                break;
            case DOWN:
                targetRegion = Region.SOUTH;
                break;
            case LEFT:
                targetRegion = Region.WEST;
                break;
            case RIGHT:
                targetRegion = Region.EAST;
                break;
            default:
                targetRegion = Region.NORTH;
        }

        for (com.traffic.sim.simulation.entities.TrafficLight light : tls.getTrafficLights()) {
            if (light.getRegion() == targetRegion && light.getType() == TrafficLight.TrafficLightType.VEHICLE) {
                return light.getCurrentState();
            }
        }
        return TrafficLight.State.GREEN;
    }

    private boolean canMoveTo(Map map, double targetX, double targetY) {
        List<GridPoint> nextCells = getTargetCells(targetX, targetY);
        for (GridPoint p : nextCells) {
            if (!map.isValid(p.x, p.y))
                continue;

            int tileType = map.getTileType(p.x, p.y);

            boolean isSelf = false;
            for (GridPoint occ : occupiedCells) {
                if (occ.x == p.x && occ.y == p.y) {
                    isSelf = true;
                    break;
                }
            }
            if (isSelf)
                continue;

            if (tileType == Map.BLOCKED)
                return false;
            if (tileType == Map.LANE_DIVIDER)
                return true;
            if (tileType == Map.SIDEWALK)
                return false;
        }
        return true;
    }

    private void updateMapOccupancy(Map map, double targetX, double targetY) {
        clearOccupiedCells(map);

        List<GridPoint> newCells = getTargetCells(targetX, targetY);
        for (GridPoint p : newCells) {
            if (map.isValid(p.x, p.y)) {
                int currentType = map.getTileType(p.x, p.y);
                p.originalType = currentType;
                occupiedCells.add(p);
                map.setTileType(p.x, p.y, Map.BLOCKED);
            }
        }
    }

    public void clearOccupiedCells(Map map) {
        if (map == null)
            return;
        for (GridPoint p : occupiedCells) {
            if (map.getTileType(p.x, p.y) == Map.BLOCKED) {
                map.setTileType(p.x, p.y, p.originalType);
            }
        }
        occupiedCells.clear();
    }

    public int getCurrentWidthCells() {
        boolean isHorizontal = Math.abs(direction.dx()) > Math.abs(direction.dy());
        return isHorizontal ? type.getWidthCells() : type.getHeightCells();
    }

    public int getCurrentHeightCells() {
        boolean isHorizontal = Math.abs(direction.dx()) > Math.abs(direction.dy());
        return isHorizontal ? type.getHeightCells() : type.getWidthCells();
    }

    private List<GridPoint> getTargetCells(double cx, double cy) {
        List<GridPoint> cells = new ArrayList<>();

        int w = getCurrentWidthCells();
        int h = getCurrentHeightCells();

        double snapX = (w % 2 == 0) ? Math.floor(cx + 0.5) : Math.floor(cx) + 0.5;
        double snapY = (h % 2 == 0) ? Math.floor(cy + 0.5) : Math.floor(cy) + 0.5;

        double halfW = w / 2.0;
        double halfH = h / 2.0;

        double startX = snapX - halfW;
        double startY = snapY - halfH;

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int ix = (int) Math.floor(startX + i + 0.01);
                int iy = (int) Math.floor(startY + j + 0.01);
                cells.add(new GridPoint(ix, iy, 0));
            }
        }

        // Add 1-cell buffer in front of the vehicle
        if (direction != null) {
            int dirX = (int) Math.signum(direction.dx());
            int dirY = (int) Math.signum(direction.dy());

            if (dirX != 0) { // Moving horizontally
                int frontX = (dirX > 0) ? (int) Math.floor(startX + w - 1 + 0.01) + 1
                        : (int) Math.floor(startX + 0.01) - 1;

                for (int j = 0; j < h; j++) {
                    int iy = (int) Math.floor(startY + j + 0.01);
                    cells.add(new GridPoint(frontX, iy, 0));
                }
            } else if (dirY != 0) { // Moving vertically
                int frontY = (dirY > 0) ? (int) Math.floor(startY + h - 1 + 0.01) + 1
                        : (int) Math.floor(startY + 0.01) - 1;

                for (int i = 0; i < w; i++) {
                    int ix = (int) Math.floor(startX + i + 0.01);
                    cells.add(new GridPoint(ix, frontY, 0));
                }
            }
        }

        return cells;
    }

    // --- Anti-Deadlock Helpers ---

    private boolean willEnterIntersection(double currentX, double currentY, double nextX, double nextY, Map map) {
        boolean currentlyInside = isInsideIntersection(currentX, currentY, map);
        boolean nextInside = isInsideIntersection(nextX, nextY, map);
        return !currentlyInside && nextInside;
    }

    public boolean isInsideIntersection(double x, double y, Map map) {
        int[] bounds = map.getIntersectionBounds();
        return x >= bounds[0] && x <= bounds[2] && y >= bounds[1] && y <= bounds[3];
    }

    private boolean isExitBlocked(Map map) {
        double probeDist = 8.0;
        double probeX = x + direction.dx() * probeDist;
        double probeY = y + direction.dy() * probeDist;

        List<GridPoint> cells = getTargetCells(probeX, probeY);
        for (GridPoint p : cells) {
            if (map.isValid(p.x, p.y)) {
                if (map.getTileType(p.x, p.y) == Map.BLOCKED) {
                    return true;
                }
            }
        }
        return false;
    }
}
