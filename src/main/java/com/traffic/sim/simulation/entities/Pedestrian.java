
package com.traffic.sim.simulation.entities;

public class Pedestrian {

    private final String id;

    private double x;
    private double y;
    private double speed;

    private Direction direction;

    private String spritePath;
    private double width;
    private double height;

    // Strict strict states
    public enum State {
        SPAWNING, MOVING, WAITING
    }

    private State state;
    private final Region regionToCheck;
    private double rotation;
    private boolean isRemoved = false;

    // ...

    private void clearOccupiedCells(com.traffic.sim.simulation.map.Map map) {
        for (GridPoint p : occupiedCells) {
            map.setTileType(p.x, p.y, p.originalType);
        }
        occupiedCells.clear();
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    // Each pedestrian occupies 2x2 cells (40x40px on 20px grid)
    // We store the 4 coordinates and their original types
    private static class GridPoint {
        int x, y;
        int originalType;

        GridPoint(int x, int y, int type) {
            this.x = x;
            this.y = y;
            this.originalType = type;
        }
    }

    private java.util.List<GridPoint> occupiedCells = new java.util.ArrayList<>();

    public Pedestrian(
            String id,
            double x,
            double y,
            double speed,
            Direction direction,
            Region regionToCheck) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
        this.regionToCheck = regionToCheck;

        this.state = State.SPAWNING; // Initial state

        // 1. Fix Rotation on Spawn
        initializeRotation();
        initializeSprite();
    }

    private void initializeRotation() {
        switch (direction) {
            case DOWN: // Move South
                this.rotation = 90;
                break;
            case UP: // Move North
                this.rotation = -90;
                break;
            case LEFT: // Move West
                this.rotation = 180;
                break;
            case RIGHT: // Move East
            default:
                this.rotation = 0;
                break;
        }
    }

    private void initializeSprite() {
        int index = (int) (Math.random() * 11) + 1;
        this.spritePath = "/assets/pedestrians/Person" + index + ".png";
        this.width = 36; // Logical size less than 40 to avoid edge clipping visual
        this.height = 36;
    }

    /**
     * Strict Grid-Based Update Logic
     */
    public void update(com.traffic.sim.simulation.map.Map map,
            com.traffic.sim.simulation.managers.TrafficLightSystem tls) {
        if (state == State.SPAWNING) {
            handleSpawning(map, tls);
        } else if (state == State.MOVING) {
            handleMoving(map);
        } else if (state == State.WAITING) {
            handleWaiting(map);
        }
    }

    private void handleSpawning(com.traffic.sim.simulation.map.Map map,
            com.traffic.sim.simulation.managers.TrafficLightSystem tls) {
        // 1. Check Traffic Light (ONLY once at spawn)
        TrafficLight.State signal = tls.getPedestrianSignal(regionToCheck);
        if (signal != TrafficLight.State.GREEN) {
            // Wait at spawn
            return;
        }

        // Green -> Try to enter map
        if (tryOccupyMap(map, x, y)) {
            this.state = State.MOVING;
        } else {
            // Blocked by something else at spawn? Wait.
        }
    }

    private void handleMoving(com.traffic.sim.simulation.map.Map map) {
        moveIfPossible(map);
    }

    private void handleWaiting(com.traffic.sim.simulation.map.Map map) {
        // In WAITING state, we simply try to resume movement if obstacle clears.
        // We DO NOT check lights (requirement: never stop for light mid-path).
        moveIfPossible(map);
    }

    private void moveIfPossible(com.traffic.sim.simulation.map.Map map) {
        double nextX = x + direction.dx() * speed;
        double nextY = y + direction.dy() * speed;

        // Check if the NEXT position moves out of map bounds
        // If ANY part of the 2x2 area is out of bounds, we treat it as leaving the map
        // -> Despawn
        java.util.List<GridPoint> nextCells = getTargetCells(nextX, nextY);
        boolean outOfBounds = false;
        for (GridPoint p : nextCells) {
            if (!map.isValid(p.x, p.y)) {
                outOfBounds = true;
                break;
            }
        }

        if (outOfBounds) {
            // Leaving the map -> Despawn
            clearOccupiedCells(map);
            this.isRemoved = true;
            return;
        }

        // Check if the NEW position is valid for occupancy (blocked by obstacles/other
        // pedestrians)
        // We know it's within bounds now because we checked above.
        if (canMoveTo(map, nextX, nextY)) {
            // Move
            updateMapOccupancy(map, nextX, nextY);
            this.x = nextX;
            this.y = nextY;
            this.state = State.MOVING;
        } else {
            // Blocked -> Wait
            this.state = State.WAITING;
        }
    }

    // --- MAP CHECK & SYNC LOGIC ---

    // Pedestrian is 40x40. Grid is 20x20.
    // Occupies [x, y], [x+1, y], [x, y+1], [x+1, y+1] (approximately)
    // We use strict integer grid mapping of the top-left corner
    // Assuming x, y are logical grid coordinates.
    // If x=14.5, it overlaps 14 and 15.
    // We calculate the 4 integer tiles covered by the 2x2 block centered (?) or
    // top-left (?)
    // Prompt: "NW: y=8-9, x=14-15". This defines a 2x2 integer block.
    // So the pedestrian occupies `floor(x)`, `floor(x)+1`, etc. if we assume x is
    // top-left.
    // Let's assume x,y are center for physics but let's assume strict integer
    // alignment for the "4 cells".

    // Prompt says: "Each pedestrian occupies exactly 4 adjacent cells at all
    // times."
    // Let's define the 4 cells based on the rounded grid position.

    private java.util.List<GridPoint> getTargetCells(double px, double py) {
        java.util.List<GridPoint> cells = new java.util.ArrayList<>();
        int gx = (int) Math.floor(px); // Top-left of the 2x2 block
        int gy = (int) Math.floor(py);

        // Fix: Based on spawn "NW: 14-15, 8-9".
        // If x=14, y=8 is top left.
        // Then cells are (14,8), (15,8), (14,9), (15,9).
        // Correct.

        cells.add(new GridPoint(gx, gy, 0));
        cells.add(new GridPoint(gx + 1, gy, 0));
        cells.add(new GridPoint(gx, gy + 1, 0));
        cells.add(new GridPoint(gx + 1, gy + 1, 0));
        return cells;
    }

    private boolean canMoveTo(com.traffic.sim.simulation.map.Map map, double targetX, double targetY) {
        java.util.List<GridPoint> targets = getTargetCells(targetX, targetY);

        for (GridPoint p : targets) {
            // If coordinate invalid, stop
            if (!map.isValid(p.x, p.y))
                return false;

            // If currently occupied ONLY by self, it's fine.
            if (isHasCell(p.x, p.y))
                continue;

            // Check map
            int type = map.getTileType(p.x, p.y);
            if (type == 1)
                return false; // Blocked obstacle
            // 0=ROAD? Pedestrians shouldn't walk on road unless CROSSWALK (3).
            // SIDEWALK (2), CROSSWALK (3) OK.
            // If ROAD (0) -> Technically unsafe but prompt says "only stop if next_cell ==
            // 1".
            // Let's stick to simple "blocked" check.
            // If type == 1 -> Stop.
        }
        return true;
    }

    private boolean isHasCell(int x, int y) {
        for (GridPoint p : occupiedCells) {
            if (p.x == x && p.y == y)
                return true;
        }
        return false;
    }

    private boolean tryOccupyMap(com.traffic.sim.simulation.map.Map map, double targetX, double targetY) {
        if (!canMoveTo(map, targetX, targetY))
            return false;
        updateMapOccupancy(map, targetX, targetY);
        return true;
    }

    private void updateMapOccupancy(com.traffic.sim.simulation.map.Map map, double targetX, double targetY) {
        // 1. Restore old cells
        for (GridPoint p : occupiedCells) {
            map.setTileType(p.x, p.y, p.originalType); // Restore original type
        }
        occupiedCells.clear();

        // 2. Set new cells
        java.util.List<GridPoint> newCells = getTargetCells(targetX, targetY);
        for (GridPoint p : newCells) {
            // Save current type (which should be 2, 3, or maybe already 1 if glitch, but we
            // checked canMoveTo)
            // Wait, if canMoveTo checked type, we should fetch it again or store it.
            int currentType = map.getTileType(p.x, p.y);

            // If currentType is 1 (BLOCKED), it might be from another entity?
            // But canMoveTo ensures we don't step on 1.
            // Exception: If we step on ourselves, we just restored ourselves (step 1), so
            // it should be the original type.

            p.originalType = currentType;
            occupiedCells.add(p);

            // Mark as 1 (BLOCKED)
            map.setTileType(p.x, p.y, 1);
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

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
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

    public String getSpritePath() {
        return spritePath;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getRotation() {
        return rotation;
    }
}
