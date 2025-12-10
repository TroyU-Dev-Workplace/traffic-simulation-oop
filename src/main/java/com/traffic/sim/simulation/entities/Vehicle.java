package com.traffic.sim.simulation.entities;

/**
 * Represents a vehicle in the simulation.
 */
public class Vehicle {
    private String id;
    private double x, y;
    private double speed;
    private double directionX, directionY; // Unit vector for direction

    // New fields for sprite logic
    private String vehicleType;
    private String spritePath;
    private double width;
    private double height;

    public Vehicle(String id, double startX, double startY, double startSpeed) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.speed = startSpeed;
        this.directionX = 1; // Default moving right
        this.directionY = 0;

        initializeSprite();
    }

    private void initializeSprite() {
        double rand = Math.random();
        // 33% Car, 33% Truck, 33% Motor
        if (rand < 0.33) {
            this.vehicleType = "Car";
            // Car 1-18
            int index = (int) (Math.random() * 18) + 1;
            this.spritePath = "/assets/vehicles/car" + index + ".png";
            this.width = 80; // Estimated proportional width
            this.height = 40;
        } else if (rand < 0.66) {
            this.vehicleType = "Truck";
            // Truck (car19-20)
            int index = (int) (Math.random() * 2) + 19;
            this.spritePath = "/assets/vehicles/car" + index + ".png";
            this.width = 150; // Estimated proportional width
            this.height = 60;
        } else {
            this.vehicleType = "Motor";
            // Motor 1-4
            int index = (int) (Math.random() * 4) + 1;
            this.spritePath = "/assets/vehicles/moto" + index + ".png";
            this.width = 54; // Estimated proportional width
            this.height = 24;
        }
    }

    public void update() {
        // Simple movement logic
        x += directionX * speed;
        y += directionY * speed;

        // Basic boundary check (can be improved with Map interaction)
    }

    public void setDirection(double dx, double dy) {
        this.directionX = dx;
        this.directionY = dy;
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

    public String getVehicleType() {
        return vehicleType;
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
}
