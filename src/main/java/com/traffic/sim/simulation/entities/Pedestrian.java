package com.traffic.sim.simulation.entities;

/**
 * Represents a pedestrian in the simulation.
 */
public class Pedestrian {
    private String id;
    private double x, y;
    private double speed;

    // New fields for sprite logic
    private String spritePath;
    private double width;
    private double height;

    public Pedestrian(String id, double startX, double startY, double speed) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.speed = speed;

        initializeSprite();
    }

    private void initializeSprite() {
        // Person 1-11
        int index = (int) (Math.random() * 11) + 1;
        this.spritePath = "/assets/pedestrians/Person" + index + ".png";

        // Fixed size 36px (scaled from 20px * 1.8)
        this.width = 36;
        this.height = 36;
    }

    public void update() {
        // Simple random movement or path following
        // For starter code, moving simply
        x += (Math.random() - 0.5) * speed;
        y += (Math.random() - 0.5) * speed;
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
