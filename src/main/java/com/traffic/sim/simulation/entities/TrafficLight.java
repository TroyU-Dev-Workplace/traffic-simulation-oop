package com.traffic.sim.simulation.entities;

/**
 * Represents a traffic light in the simulation.
 */
public class TrafficLight {
    public enum State {
        RED, YELLOW, GREEN
    }

    public enum TrafficLightType {
        VEHICLE, PEDESTRIAN
    }

    public enum Direction {
        NS, EW // North-South, East-West
    }

    private String id;
    private int x, y;
    private State currentState;
    private TrafficLightType type;
    private Direction direction;
    private Region region; // Nam add: Region assigned to this traffic light

    // Nam add: Include region for per-spawn traffic light matching
    public TrafficLight(String id, int x, int y, TrafficLightType type, Direction direction, Region region) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.direction = direction;
        this.region = region; // Nam add: Store region
        this.currentState = State.RED;
    }

    public void setState(State state) {
        this.currentState = state;
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

    public State getCurrentState() {
        return currentState;
    }

    public TrafficLightType getType() {
        return type;
    }

    public Direction getDirection() {
        return direction;
    }

    // Nam add: Get region assigned to this traffic light
    public Region getRegion() {
        return region;
    }

    // Removed internal update() timer logic as TrafficLightSystem will control
    // state
    public void update() {
        // No-op for now, state controlled externally
    }
}
