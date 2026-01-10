package com.traffic.sim.simulation.entities;

import java.util.ArrayList;
import java.util.List;

import com.traffic.sim.simulation.managers.TrafficLightSystem;
import com.traffic.sim.simulation.map.Map;

public class Vehicle {

    public enum TurnDirection {
        STRAIGHT, LEFT, RIGHT
    }

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

    private Region spawnRegion;
    private boolean isStoppedForVehicle;
    private int spawnRegionMinX;
    private int spawnRegionMinY;
    private int spawnRegionMaxX;
    private int spawnRegionMaxY;
    private boolean spawnRegionBoundsSet;
    private Double spawnRegionCenterX;
    private Double spawnRegionCenterY;
    private boolean shouldBeRemoved = false;
    private double rotation = 0.0;

    private double totalCO2;
    private int waitingFrames;
    private final long entryTime;

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

    public Vehicle(
            String id,
            double x,
            double y,
            double speed,
            VehicleType type,
            Direction direction) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.type = type;
        this.direction = direction;

        this.totalCO2 = 0.0;
        this.waitingFrames = 0;
        this.entryTime = System.currentTimeMillis();

        this.originalSpeed = speed;
        this.isStoppedAtLight = false;

        this.plannedTurn = TurnDirection.STRAIGHT;
        this.spawnRegion = null;
        this.isStoppedForVehicle = false;
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
        switch (type) {
            case CAR:
                int index = (int) (Math.random() * 18) + 1;
                this.spritePath = "/assets/vehicles/car" + index + ".png";
                break;
            case TRUCK:
                int index2 = (int) (Math.random() * 2) + 19;
                this.spritePath = "/assets/vehicles/car" + index2 + ".png";
                break;
            case MOTORCYCLE:
                int index3 = (int) (Math.random() * 4) + 1;
                this.spritePath = "/assets/vehicles/moto" + index3 + ".png";
                break;
        }
    }

    /**
     * Updates the vehicle's state (position, CO2, waiting time) independent of the
     * map.
     * This is typically used for simple animations or state tracking.
     */
    public void update() {
        if (speed > 0) {
            x += direction.dx() * speed;
            y += direction.dy() * speed;
            totalCO2 += type.getEmissionMoving();
        } else {
            waitingFrames++;
            totalCO2 += type.getEmissionIdling();
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

    /**
     * Updates the vehicle's speed and state based on the traffic light signal.
     * 
     * @param lightState The current state of the relevant traffic light.
     */
    public void respondToTrafficLight(TrafficLight.State lightState) {
        switch (lightState) {
            case RED:
                this.speed = 0.0;
                this.isStoppedAtLight = true;
                break;
            case YELLOW:
                this.speed = this.originalSpeed * 0.3;
                this.isStoppedAtLight = false;
                break;
            case GREEN:
                this.speed = this.originalSpeed;
                this.isStoppedAtLight = false;
                break;
        }
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

    /**
     * Updates the vehicle's position and logic interacting with the map and traffic
     * lights.
     * Handles movement validation, turning, and stop-line behaviors.
     * 
     * @param map The simulation map.
     * @param tls The traffic light system.
     */
    public void updateWithMap(Map map, TrafficLightSystem tls) {
        if (speed > 0 || isStoppedAtLight) {
            if (plannedTurn != TurnDirection.STRAIGHT && !isStoppedAtLight && speed > 0) {
                checkAndExecuteTurn(map);
            }

            double nextX = x + direction.dx() * speed;
            double nextY = y + direction.dy() * speed;

            // Anti-Deadlock: Don't Block the Box
            if (willEnterIntersection(x, y, nextX, nextY, map)) {
                if (isExitBlocked(map)) {
                    this.speed = 0;
                    return; // Wait at the line/boundary
                }
            }

            List<GridPoint> nextOccupied = getTargetCells(nextX, nextY);
            boolean outOfBounds = false;
            for (GridPoint p : nextOccupied) {
                if (!map.isValid(p.x, p.y)) {
                    outOfBounds = true;
                    break;
                }
            }

            if (outOfBounds) {
                if (isInsideSpawnRegion()) {
                    outOfBounds = false;
                }
            }

            if (outOfBounds) {
                clearOccupiedCells(map);
                this.shouldBeRemoved = true;
                return;
            }

            double frontDist = type.getWidthCells() / 2.0;
            int nextGridX = (int) Math.floor(nextX + direction.dx() * (frontDist + 0.5));
            int nextGridY = (int) Math.floor(nextY + direction.dy() * (frontDist + 0.5));

            int nextTileType = map.getTileType(nextGridX, nextGridY);

            if (nextTileType == Map.STOP_LINE) {
                int currentGridX = (int) Math.floor(x);
                int currentGridY = (int) Math.floor(y);

                if (map.getTileType(currentGridX, currentGridY) != Map.STOP_LINE) {
                    TrafficLight.State lightState = getTrafficLightState(tls);

                    if (lightState != TrafficLight.State.GREEN) {
                        this.speed = 0;
                        this.isStoppedAtLight = true;
                        return;
                    }
                }
            }

            if (canMoveTo(map, nextX, nextY)) {
                this.speed = this.originalSpeed;
                this.isStoppedAtLight = false;

                updateMapOccupancy(map, nextX, nextY);
                this.x = nextX;
                this.y = nextY;
                this.totalCO2 += type.getEmissionMoving();
            } else {
                this.speed = 0;
            }
        } else {
            if (isStoppedAtLight) {
                TrafficLight.State lightState = getTrafficLightState(tls);
                if (lightState == TrafficLight.State.GREEN) {
                    this.speed = this.originalSpeed;
                    this.isStoppedAtLight = false;
                }
            } else {
                double nextX = x + direction.dx() * originalSpeed;
                double nextY = y + direction.dy() * originalSpeed;
                if (canMoveTo(map, nextX, nextY)) {
                    this.speed = this.originalSpeed;
                }
            }

            waitingFrames++;
            totalCO2 += type.getEmissionIdling();
        }
    }

    /**
     * Checks if the vehicle is at a turning point and executes the turn if planned.
     * Snaps the vehicle to the lane center upon turning.
     * 
     * @param map The simulation map.
     */
    private void checkAndExecuteTurn(Map map) {
        double laneCenterSouth = 21.0;
        double laneCenterNorth = 28.0;
        double laneCenterWest = 14.0;
        double laneCenterEast = 21.0;

        boolean turned = false;
        Direction newDir = direction;
        double newRot = rotation;
        double snapX = x;
        double snapY = y;

        if (direction == Direction.DOWN) {
            if (y >= laneCenterEast && plannedTurn == TurnDirection.LEFT) {
                snapY = laneCenterEast;
                newDir = Direction.RIGHT;
                newRot = 0.0;
                turned = true;
            } else if (y >= laneCenterWest && plannedTurn == TurnDirection.RIGHT) {
                snapY = laneCenterWest;
                newDir = Direction.LEFT;
                newRot = 180.0;
                turned = true;
            }
        } else if (direction == Direction.UP) {
            if (y <= laneCenterWest && plannedTurn == TurnDirection.LEFT) {
                snapY = laneCenterWest;
                newDir = Direction.LEFT;
                newRot = 180.0;
                turned = true;
            } else if (y <= laneCenterEast && plannedTurn == TurnDirection.RIGHT) {
                snapY = laneCenterEast;
                newDir = Direction.RIGHT;
                newRot = 0.0;
                turned = true;
            }
        } else if (direction == Direction.RIGHT) {
            if (x >= laneCenterNorth && plannedTurn == TurnDirection.LEFT) {
                snapX = laneCenterNorth;
                newDir = Direction.UP;
                newRot = -90.0;
                turned = true;
            } else if (x >= laneCenterSouth && plannedTurn == TurnDirection.RIGHT) {
                snapX = laneCenterSouth;
                newDir = Direction.DOWN;
                newRot = 90.0;
                turned = true;
            }
        } else if (direction == Direction.LEFT) {
            if (x <= laneCenterSouth && plannedTurn == TurnDirection.LEFT) {
                snapX = laneCenterSouth;
                newDir = Direction.DOWN;
                newRot = 90.0;
                turned = true;
            } else if (x <= laneCenterNorth && plannedTurn == TurnDirection.RIGHT) {
                snapX = laneCenterNorth;
                newDir = Direction.UP;
                newRot = -90.0;
                turned = true;
            }
        }

        if (turned) {
            boolean isHoriz = (newDir == Direction.RIGHT || newDir == Direction.LEFT);
            int newW = isHoriz ? type.getWidthCells() : type.getHeightCells();
            int newH = isHoriz ? type.getHeightCells() : type.getWidthCells();

            if (newW % 2 != 0) {
                snapX = Math.floor(snapX) + 0.5;
            } else {
                snapX = Math.floor(snapX + 0.5);
            }

            if (newH % 2 != 0) {
                snapY = Math.floor(snapY) + 0.5;
            } else {
                snapY = Math.floor(snapY + 0.5);
            }

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

        for (TrafficLight light : tls.getTrafficLights()) {
            if (light.getRegion() == targetRegion
                    && light.getType() == TrafficLight.TrafficLightType.VEHICLE) {
                return light.getCurrentState();
            }
        }
        return TrafficLight.State.GREEN;
    }

    private boolean canMoveTo(Map map, double targetX, double targetY) {
        List<GridPoint> nextCells = getTargetCells(targetX, targetY);
        for (GridPoint p : nextCells) {
            if (!map.isValid(p.x, p.y)) {
                continue;
            }

            int type = map.getTileType(p.x, p.y);

            boolean isSelf = false;
            for (GridPoint occ : occupiedCells) {
                if (occ.x == p.x && occ.y == p.y) {
                    isSelf = true;
                    break;
                }
            }
            if (isSelf)
                continue;

            if (type == Map.BLOCKED) {
                return false;
            }
            if (type == Map.LANE_DIVIDER) {
                return true; // Use lane divider as valid road
            }
            if (type == Map.SIDEWALK) {
                return false;
            }
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
        return cells;
    }

    public boolean shouldBeRemoved() {
        return shouldBeRemoved;
    }

    public void markForRemoval() {
        this.shouldBeRemoved = true;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setPlannedTurn(TurnDirection turn) {
        this.plannedTurn = turn;
    }

    public TurnDirection getPlannedTurn() {
        return plannedTurn;
    }

    public void setSpawnRegion(Region region) {
        this.spawnRegion = region;
    }

    public Region getSpawnRegion() {
        return spawnRegion;
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
            if (spawnRegion == Region.NORTH) {
                return y <= spawnRegionCenterY;
            }
            if (spawnRegion == Region.SOUTH) {
                return y >= spawnRegionCenterY;
            }
            if (spawnRegion == Region.WEST) {
                return x <= spawnRegionCenterX;
            }
            if (spawnRegion == Region.EAST) {
                return x >= spawnRegionCenterX;
            }
        }
        return true;
    }

    public boolean isStoppedForVehicle() {
        return isStoppedForVehicle;
    }

    public void setStoppedForVehicle(boolean stopped) {
        this.isStoppedForVehicle = stopped;
    }

    public boolean hasVehicleAhead(Iterable<Vehicle> vehicles, double lookAheadDistance, double lateralThreshold) {
        return hasVehicleAheadInDirection(vehicles, direction.dx(), direction.dy(), lookAheadDistance,
                lateralThreshold);
    }

    public boolean hasVehicleAheadForTurn(Iterable<Vehicle> vehicles, double lookAheadDistance,
            double lateralThreshold) {
        if (plannedTurn == TurnDirection.STRAIGHT) {
            return hasVehicleAhead(vehicles, lookAheadDistance, lateralThreshold);
        }

        double baseX = direction.dx();
        double baseY = direction.dy();

        double targetX;
        double targetY;
        if (plannedTurn == TurnDirection.LEFT) {
            targetX = -baseY;
            targetY = baseX;
        } else if (plannedTurn == TurnDirection.RIGHT) {
            targetX = baseY;
            targetY = -baseX;
        } else {
            targetX = baseX;
            targetY = baseY;
        }

        if (hasVehicleAheadInDirection(vehicles, direction.dx(), direction.dy(), lookAheadDistance, lateralThreshold)) {
            return true;
        }
        return hasVehicleAheadInDirection(vehicles, targetX, targetY, lookAheadDistance, lateralThreshold);
    }

    private boolean hasVehicleAheadInDirection(Iterable<Vehicle> vehicles, double dirX, double dirY,
            double lookAheadDistance, double lateralThreshold) {
        double dirMag = Math.sqrt(dirX * dirX + dirY * dirY);
        if (dirMag == 0) {
            return false;
        }
        double normX = dirX / dirMag;
        double normY = dirY / dirMag;

        for (Vehicle other : vehicles) {
            if (other == this) {
                continue;
            }

            if (other.isStoppedAtLight() && this.speed > 0) {
                continue;
            }

            double dx = other.getX() - this.x;
            double dy = other.getY() - this.y;

            double distanceSquared = dx * dx + dy * dy;
            if (distanceSquared > lookAheadDistance * lookAheadDistance) {
                continue;
            }

            double forwardDist = dx * normX + dy * normY;
            if (forwardDist <= 0.1 || forwardDist > lookAheadDistance) {
                continue;
            }

            double lateralDist = Math.abs(dx * -normY + dy * normX);
            if (lateralDist <= lateralThreshold) {
                return true;
            }
        }
        return false;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;

    }

    // Helper methods for Anti-Deadlock

    private boolean willEnterIntersection(double currentX, double currentY, double nextX, double nextY, Map map) {
        boolean currentlyIn = isInsideIntersection(currentX, currentY, map);
        boolean nextIn = isInsideIntersection(nextX, nextY, map);
        return !currentlyIn && nextIn;
    }

    private boolean isInsideIntersection(double x, double y, Map map) {
        int[] bounds = map.getIntersectionBounds();
        return x >= bounds[0] && x <= bounds[2] && y >= bounds[1] && y <= bounds[3];
    }

    private boolean isExitBlocked(Map map) {
        int[] bounds = map.getIntersectionBounds();

        double exitDirX = direction.dx();
        double exitDirY = direction.dy();

        if (plannedTurn == TurnDirection.LEFT) {
            if (direction == Direction.RIGHT) {
                exitDirX = 0;
                exitDirY = -1;
            } else if (direction == Direction.UP) {
                exitDirX = -1;
                exitDirY = 0;
            } else if (direction == Direction.LEFT) {
                exitDirX = 0;
                exitDirY = 1;
            } else if (direction == Direction.DOWN) {
                exitDirX = 1;
                exitDirY = 0;
            }
        } else if (plannedTurn == TurnDirection.RIGHT) {
            if (direction == Direction.RIGHT) {
                exitDirX = 0;
                exitDirY = 1;
            } else if (direction == Direction.UP) {
                exitDirX = 1;
                exitDirY = 0;
            } else if (direction == Direction.LEFT) {
                exitDirX = 0;
                exitDirY = -1;
            } else if (direction == Direction.DOWN) {
                exitDirX = -1;
                exitDirY = 0;
            }
        }

        double checkX = x;
        double checkY = y;

        if (exitDirX > 0)
            checkX = bounds[2] + 2.0;
        else if (exitDirX < 0)
            checkX = bounds[0] - 2.0;

        if (exitDirY > 0)
            checkY = bounds[3] + 2.0;
        else if (exitDirY < 0)
            checkY = bounds[1] - 2.0;

        int targetX = (int) Math.floor(checkX);
        int targetY = (int) Math.floor(checkY);

        if (plannedTurn != TurnDirection.STRAIGHT) {
            if (exitDirX != 0) {
                if (exitDirX > 0)
                    targetY = 21;
                else
                    targetY = 14;
            } else {
                if (exitDirY > 0)
                    targetX = 21;
                else
                    targetX = 28;
            }
        } else {
            if (exitDirX != 0)
                targetY = (int) Math.floor(y);
            else
                targetX = (int) Math.floor(x);
        }

        for (int i = 0; i < 3; i++) {
            int tx = targetX + (int) (exitDirX * i);
            int ty = targetY + (int) (exitDirY * i);

            if (map.isValid(tx, ty) && map.getTileType(tx, ty) == Map.BLOCKED) {
                return true;
            }
        }
        return false;
    }
}
