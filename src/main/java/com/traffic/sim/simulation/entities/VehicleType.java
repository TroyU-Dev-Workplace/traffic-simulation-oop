package com.traffic.sim.simulation.entities;

public enum VehicleType {

    CAR(80, 40, 0.12, 0.03),
    TRUCK(150, 60, 0.35, 0.10),
    MOTORCYCLE(54, 24, 0.05, 0.01);

    private final double width;
    private final double height;
    private final double emissionMoving;
    private final double emissionIdling;

    VehicleType(double width, double height,
                double emissionMoving, double emissionIdling) {
        this.width = width;
        this.height = height;
        this.emissionMoving = emissionMoving;
        this.emissionIdling = emissionIdling;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getEmissionMoving() {
        return emissionMoving;
    }

    public double getEmissionIdling() {
        return emissionIdling;
    }
}
