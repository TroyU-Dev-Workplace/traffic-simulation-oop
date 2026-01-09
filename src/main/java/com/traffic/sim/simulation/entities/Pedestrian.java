
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

    private int waitingFrames;
    private final long entryTime;

    public Pedestrian(
            String id,
            double x,
            double y,
            double speed,
            Direction direction
    ) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;

        this.waitingFrames = 0;
        this.entryTime = System.currentTimeMillis();

        initializeSprite();
    }

    private void initializeSprite() {
        int index = (int) (Math.random() * 11) + 1;
        this.spritePath = "/assets/pedestrians/Person" + index + ".png";

        this.width = 36;
        this.height = 36;
    }

    public void update() {
        x += direction.dx() * speed;
        y += direction.dy() * speed;

        if (speed == 0) {
            waitingFrames++;
        }
    }
    
    // Enhanced update with map-aware movement
    public void updateWithMap(com.traffic.sim.simulation.map.Map map) {
        if (speed > 0) {
            double nextX = x + direction.dx() * speed;
            double nextY = y + direction.dy() * speed;
            
            int gridX = (int) Math.floor(nextX);
            int gridY = (int) Math.floor(nextY);
            
            // Check if next position is walkable
            if (map.isValid(gridX, gridY) && map.isWalkable(gridX, gridY)) {
                x = nextX;
                y = nextY;
            } else {
                // Try to find alternate path or change direction
                findNewDirection(map);
            }
        } else {
            waitingFrames++;
        }
    }
    
    private void findNewDirection(com.traffic.sim.simulation.map.Map map) {
        // Try different directions to find walkable path
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        
        for (Direction newDir : directions) {
            if (newDir == direction) continue; // Don't go back same direction
            
            double testX = x + newDir.dx() * speed;
            double testY = y + newDir.dy() * speed;
            int gridX = (int) Math.floor(testX);
            int gridY = (int) Math.floor(testY);
            
            if (map.isValid(gridX, gridY) && map.isWalkable(gridX, gridY)) {
                this.direction = newDir;
                return;
            }
        }
        
        // If no direction works, stop temporarily
        speed = 0;
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

    public int getWaitingFrames() {
        return waitingFrames;
    }

    public long getTravelTime() {
        return System.currentTimeMillis() - entryTime;
    }
}
