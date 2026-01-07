
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
