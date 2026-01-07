package com.traffic.sim.simulation.entities;

public class Vehicle {

    private final String id;

    private double x;
    private double y;

    private double speed;
    private Direction direction;

    private final VehicleType type;

    private String spritePath;

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

        initializeSprite();
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
        x += direction.dx() * speed;
        y += direction.dy() * speed;

        if (speed == 0) {
            waitingFrames++;
            totalCO2 += type.getEmissionIdling();
        } else {
            totalCO2 += type.getEmissionMoving();
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
}

