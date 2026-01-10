package com.traffic.sim.simulation.entities;

/**
 * Represents a single traffic light entity in the simulation.
 * Design Pattern: Pure Data Entity (Passive).
 * Responsibility: Holds state (color), location, and type.
 * Note: All control logic is centralized in TrafficLightSystem to separate Data
 * from Logic.
 */
public class TrafficLight {

    public enum State {
        RED, YELLOW, GREEN
    }

    public enum TrafficLightType {
        VEHICLE, PEDESTRIAN
    }

    public enum Direction {
        NS, // North-South
        EW // East-West
    }

    private String id;
    private int x, y; // Grid coordinates
    private State currentState;
    private TrafficLightType type;
    private Direction direction;
    private Region region;

    public TrafficLight(String id, int x, int y, TrafficLightType type, Direction direction, Region region) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.direction = direction;
        this.region = region;

        // Always default to RED for safety during initialization
        this.currentState = State.RED;
    }

    // --- ACCESSORS & MUTATORS ---

    public void setState(State state) {
        this.currentState = state;
    }

    public State getCurrentState() {
        return currentState;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public TrafficLightType getType() {
        return type;
    }

    public Direction getDirection() {
        return direction;
    }

    public Region getRegion() {
        return region;
    }
}
