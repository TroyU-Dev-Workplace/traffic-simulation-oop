package com.traffic.sim.simulation.entities;

public class Vehicle {

    // Nam adding: Enum for turn directions
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

    // Nam adding: Traffic light response fields
    private double originalSpeed; // Store original speed for restoration
    private boolean isStoppedAtLight; // Flag to track if stopped at traffic light

    // Nam adding: Turning and lane system fields
    private TurnDirection plannedTurn; // What turn the vehicle will make at intersection
    private boolean hasReachedIntersection; // Track if vehicle has reached intersection
    private double turnProgress; // Progress through turn (0.0 to 1.0)
    private boolean isTurning; // Currently executing a turn
    private double turnStartDirX;
    private double turnStartDirY;
    private Region spawnRegion; // Nam add: Track spawn region for traffic light matching
    private boolean isStoppedForVehicle; // Nam add: Stop flag for vehicle-ahead collision
    private int spawnRegionMinX; // Nam add: Spawn region bounds for traffic light checks
    private int spawnRegionMinY; // Nam add: Spawn region bounds for traffic light checks
    private int spawnRegionMaxX; // Nam add: Spawn region bounds for traffic light checks
    private int spawnRegionMaxY; // Nam add: Spawn region bounds for traffic light checks
    private boolean spawnRegionBoundsSet; // Nam add: Flag for spawn region bounds
    private Double spawnRegionCenterX; // Nam add: Spawn region center for region checks
    private Double spawnRegionCenterY; // Nam add: Spawn region center for region checks
    private boolean shouldBeRemoved = false; // Flag to indicate if vehicle should be removed

    private double totalCO2;
    private int waitingFrames;
    private final long entryTime;

    public Vehicle(
            String id,
            double x,
            double y,
            double speed,
            VehicleType type,
            Direction direction
    ) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.type = type;
        this.direction = direction;

        this.totalCO2 = 0.0;
        this.waitingFrames = 0;
        this.entryTime = System.currentTimeMillis();

        // Nam adding: Initialize traffic light response fields
        this.originalSpeed = speed;
        this.isStoppedAtLight = false;

        // Nam adding: Initialize turning fields
        this.plannedTurn = TurnDirection.STRAIGHT; // Default to straight
        this.hasReachedIntersection = false;
        this.turnProgress = 0.0;
        this.isTurning = false;
        this.turnStartDirX = direction.dx();
        this.turnStartDirY = direction.dy();
        this.spawnRegion = null; // Nam add: Set by spawner
        this.isStoppedForVehicle = false; // Nam add: Initialize vehicle-ahead stop flag
        this.spawnRegionMinX = Integer.MIN_VALUE; // Nam add: Initialize spawn region bounds
        this.spawnRegionMinY = Integer.MIN_VALUE; // Nam add: Initialize spawn region bounds
        this.spawnRegionMaxX = Integer.MAX_VALUE; // Nam add: Initialize spawn region bounds
        this.spawnRegionMaxY = Integer.MAX_VALUE; // Nam add: Initialize spawn region bounds
        this.spawnRegionBoundsSet = false; // Nam add: Initialize spawn region bounds flag
        this.spawnRegionCenterX = null; // Nam add: Initialize spawn region center
        this.spawnRegionCenterY = null; // Nam add: Initialize spawn region center

        initializeSprite();
    }

    // Simplified constructor for spawning (compatible with feature/spawning usage)
    public Vehicle(String id, double x, double y, double speed) {
        this(id, x, y, speed, VehicleType.CAR, Direction.RIGHT);
    }

    private void initializeSprite() {
        switch (type) {
            case CAR -> {
                int index = (int) (Math.random() * 18) + 1;
                this.spritePath = "/assets/vehicles/car" + index + ".png";
            }
            case TRUCK -> {
                int index = (int) (Math.random() * 2) + 19;
                this.spritePath = "/assets/vehicles/car" + index + ".png";
            }
            case MOTORCYCLE -> {
                int index = (int) (Math.random() * 4) + 1;
                this.spritePath = "/assets/vehicles/moto" + index + ".png";
            }
        }
    }

    public void update() {
        // Nam adding: Improved movement logic with CO2 tracking
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
        // Convert dx, dy to Direction enum
        if (dx > 0) {
            this.direction = Direction.RIGHT;
        } else if (dx < 0) {
            this.direction = Direction.LEFT;
        } else if (dy > 0) {
            this.direction = Direction.DOWN;
        } else if (dy < 0) {
            this.direction = Direction.UP;
        }
        // Update turn start direction
        this.turnStartDirX = dx;
        this.turnStartDirY = dy;
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

    // Nam add: Allow VehicleManager to set speed when stopping for vehicles
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        this.turnStartDirX = direction.dx();
        this.turnStartDirY = direction.dy();
    }

    public VehicleType getType() {
        return type;
    }

    public String getSpritePath() {
        return spritePath;
    }

    public double getWidth() {
        return type.getWidth();
    }

    public double getHeight() {
        return type.getHeight();
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
        return type.getWidth() * type.getHeight();
    }

    public int getLengthInCells() {
        return (int) Math.ceil(type.getWidth());
    }

    // Nam adding: Traffic light response methods

    /**
     * Responds to traffic light signal
     * @param lightState RED, YELLOW, or GREEN
     */
    public void respondToTrafficLight(TrafficLight.State lightState) {
        switch (lightState) {
            case RED:
                // Stop completely
                this.speed = 0.0;
                this.isStoppedAtLight = true;
                break;
            case YELLOW:
                // Reduce speed to 30% of original
                this.speed = this.originalSpeed * 0.3;
                this.isStoppedAtLight = false;
                break;
            case GREEN:
                // Resume normal speed
                this.speed = this.originalSpeed;
                this.isStoppedAtLight = false;
                break;
        }
    }

    /**
     * Check if vehicle is currently stopped at a traffic light
     */
    public boolean isStoppedAtLight() {
        return isStoppedAtLight;
    }

    /**
     * Get original speed of the vehicle
     */
    public double getOriginalSpeed() {
        return originalSpeed;
    }

    /**
     * Get current direction X
     */
    public double getDirectionX() {
        return direction.dx();
    }

    /**
     * Get current direction Y
     */
    public double getDirectionY() {
        return direction.dy();
    }

    /**
     * Nam adding: Enhanced update method with map interaction
     * @param map The map to check for valid movement
     */
    public void updateWithMap(com.traffic.sim.simulation.map.Map map) {
        // Nam adding: Enhanced movement logic with lane-aware navigation and turning
        if (speed > 0) { // Only move if not stopped by traffic light

            int currentGridX = (int) Math.floor(x);
            int currentGridY = (int) Math.floor(y);

            // Nam adding: Check if currently in intersection
            if (map.isIntersection(currentGridX, currentGridY)) {
                if (!hasReachedIntersection && plannedTurn != TurnDirection.STRAIGHT) {
                    double centerX = map.getIntersectionCenterX();
                    double centerY = map.getIntersectionCenterY();
                    boolean movingVertical = Math.abs(turnStartDirY) >= Math.abs(turnStartDirX);
                    double axisDistance = movingVertical ? Math.abs(y - centerY) : Math.abs(x - centerX);
                    if (axisDistance <= 0.8) {
                        hasReachedIntersection = true;
                        isTurning = true;
                        turnProgress = 0.0;
                    }
                }

                // Nam adding: Execute turning behavior in intersection
                if (isTurning) {
                    executeTurn();
                } else {
                    // Nam adding: Continue straight through intersection
                    moveForward(map);
                }
            } else {
                // Nam adding: Normal lane movement
                moveForward(map);
            }
        } else {
            // Track waiting time and emissions when stopped
            waitingFrames++;
            totalCO2 += type.getEmissionIdling();
        }
    }

    // Nam adding: Method to move vehicle forward
    private void moveForward(com.traffic.sim.simulation.map.Map map) {
        double nextX = x + direction.dx() * speed;
        double nextY = y + direction.dy() * speed;

        int gridX = (int) Math.floor(nextX);
        int gridY = (int) Math.floor(nextY);

        if (map.isValid(gridX, gridY)) {
            if (map.isDriveableForVehicle(gridX, gridY)) {
                x = nextX;
                y = nextY;
                totalCO2 += type.getEmissionMoving();
            }
        } else {
            // Nam adding: Vehicle moved outside map bounds - mark for removal
            this.shouldBeRemoved = true;
        }
    }

    // Nam adding: Method to execute turning behavior
    private void executeTurn() {
        turnProgress += 0.1; // Adjust turn speed
        double t = Math.min(turnProgress, 1.0);

        double baseX = turnStartDirX;
        double baseY = turnStartDirY;

        double newDirX, newDirY;
        if (plannedTurn == TurnDirection.LEFT) {
            // Nam adding: 90-degree left turn
            newDirX = baseX * (1 - t) - baseY * t;
            newDirY = baseY * (1 - t) + baseX * t;
        } else if (plannedTurn == TurnDirection.RIGHT) {
            // Nam adding: 90-degree right turn
            newDirX = baseX * (1 - t) + baseY * t;
            newDirY = baseY * (1 - t) - baseX * t;
        } else {
            newDirX = baseX;
            newDirY = baseY;
        }

        // Nam adding: Normalize direction for consistent movement
        double magnitude = Math.sqrt(newDirX * newDirX + newDirY * newDirY);
        if (magnitude > 0) {
            newDirX /= magnitude;
            newDirY /= magnitude;
        }

        // Nam adding: Move forward during turn
        x += newDirX * speed * 0.5; // Slower during turn
        y += newDirY * speed * 0.5;
        totalCO2 += type.getEmissionMoving();

        if (turnProgress >= 1.0) {
            completeTurn();
        }
    }

    // Nam adding: Complete the turn and set final direction
    private void completeTurn() {
        if (plannedTurn == TurnDirection.LEFT) {
            // Nam adding: Left turn: rotate direction 90° counter-clockwise
            if (direction == Direction.UP) {
                direction = Direction.LEFT;
            } else if (direction == Direction.LEFT) {
                direction = Direction.DOWN;
            } else if (direction == Direction.DOWN) {
                direction = Direction.RIGHT;
            } else {
                direction = Direction.UP;
            }
        } else if (plannedTurn == TurnDirection.RIGHT) {
            // Nam adding: Right turn: rotate direction 90° clockwise
            if (direction == Direction.UP) {
                direction = Direction.RIGHT;
            } else if (direction == Direction.RIGHT) {
                direction = Direction.DOWN;
            } else if (direction == Direction.DOWN) {
                direction = Direction.LEFT;
            } else {
                direction = Direction.UP;
            }
        }

        // Update turn start direction
        turnStartDirX = direction.dx();
        turnStartDirY = direction.dy();

        // Nam adding: Reset turning flags
        isTurning = false;
        turnProgress = 0.0;
    }

    /**
     * Nam adding: Check if vehicle should be removed from simulation
     */
    public boolean shouldBeRemoved() {
        return shouldBeRemoved;
    }

    // Nam adding: Turning system methods

    /**
     * Set the planned turn direction for this vehicle
     */
    public void setPlannedTurn(TurnDirection turn) {
        this.plannedTurn = turn;
    }

    /**
     * Get the planned turn direction
     */
    public TurnDirection getPlannedTurn() {
        return plannedTurn;
    }

    /**
     * Check if vehicle is currently turning
     */
    public boolean isTurning() {
        return isTurning;
    }

    /**
     * Check if vehicle has reached intersection
     */
    public boolean hasReachedIntersection() {
        return hasReachedIntersection;
    }

    // Nam add: Set spawn region for traffic light filtering
    public void setSpawnRegion(Region region) {
        this.spawnRegion = region;
    }

    // Nam add: Get spawn region for traffic light filtering
    public Region getSpawnRegion() {
        return spawnRegion;
    }

    // Nam add: Set spawn region bounds for traffic light checks
    public void setSpawnRegionBounds(int minX, int minY, int maxX, int maxY) {
        this.spawnRegionMinX = minX;
        this.spawnRegionMinY = minY;
        this.spawnRegionMaxX = maxX;
        this.spawnRegionMaxY = maxY;
        this.spawnRegionBoundsSet = true; // Nam add: Mark bounds as set
    }

    // Nam add: Set spawn region center for traffic light checks
    public void setSpawnRegionCenter(double centerX, double centerY) {
        this.spawnRegionCenterX = centerX;
        this.spawnRegionCenterY = centerY;
    }

    // Nam add: Check if vehicle is still inside its spawn region
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
        return true; // Nam add: Default to checking lights if bounds are not set
    }

    // Nam add: Check if vehicle is currently stopped due to another vehicle
    public boolean isStoppedForVehicle() {
        return isStoppedForVehicle;
    }

    // Nam add: Set stop flag for vehicle-ahead collision handling
    public void setStoppedForVehicle(boolean stopped) {
        this.isStoppedForVehicle = stopped;
    }

    // Nam add: Detect if there is a vehicle ahead in the current direction
    public boolean hasVehicleAhead(Iterable<Vehicle> vehicles, double lookAheadDistance, double lateralThreshold) {
        return hasVehicleAheadInDirection(vehicles, direction.dx(), direction.dy(), lookAheadDistance, lateralThreshold);
    }

    // Nam add: Check collisions in turning direction when vehicle plans to turn
    public boolean hasVehicleAheadForTurn(Iterable<Vehicle> vehicles, double lookAheadDistance, double lateralThreshold) {
        if (plannedTurn == TurnDirection.STRAIGHT) {
            return hasVehicleAhead(vehicles, lookAheadDistance, lateralThreshold);
        }

        double baseX = direction.dx();
        double baseY = direction.dy();
        if (isTurning) {
            baseX = turnStartDirX;
            baseY = turnStartDirY;
        }

        double targetX;
        double targetY;
        if (plannedTurn == TurnDirection.LEFT) {
            targetX = -baseY;
            targetY = baseX;
        } else {
            targetX = baseY;
            targetY = -baseX;
        }

        if (hasVehicleAheadInDirection(vehicles, direction.dx(), direction.dy(), lookAheadDistance, lateralThreshold)) {
            return true;
        }
        return hasVehicleAheadInDirection(vehicles, targetX, targetY, lookAheadDistance, lateralThreshold);
    }

    // Nam fix: Improved directional collision check helper
    private boolean hasVehicleAheadInDirection(Iterable<Vehicle> vehicles, double dirX, double dirY,
                                                double lookAheadDistance, double lateralThreshold) {
        double dirMag = Math.sqrt(dirX * dirX + dirY * dirY); // Nam fix: Direction magnitude
        if (dirMag == 0) {
            return false;
        }
        double normX = dirX / dirMag; // Nam fix: Normalize direction
        double normY = dirY / dirMag;

        for (Vehicle other : vehicles) {
            if (other == this) {
                continue;
            }

            // Nam fix: Skip vehicles that are stopped at traffic lights to avoid deadlock
            if (other.isStoppedAtLight() && this.speed > 0) {
                continue;
            }

            double dx = other.getX() - this.x;
            double dy = other.getY() - this.y;

            // Nam fix: Simple distance check first for performance
            double distanceSquared = dx * dx + dy * dy;
            if (distanceSquared > lookAheadDistance * lookAheadDistance) {
                continue;
            }

            double forwardDist = dx * normX + dy * normY; // Nam fix: Projection along direction
            if (forwardDist <= 0.1 || forwardDist > lookAheadDistance) { // Nam fix: Small buffer to avoid self-collision
                continue;
            }

            double lateralDist = Math.abs(dx * -normY + dy * normX); // Nam fix: Perpendicular distance
            if (lateralDist <= lateralThreshold) {
                return true;
            }
        }
        return false;
    }
}
