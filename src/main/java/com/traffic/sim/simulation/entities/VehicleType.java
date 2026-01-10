package com.traffic.sim.simulation.entities;

public enum VehicleType {

    CAR(4, 2, 0.12, 0.03),
    TRUCK(7, 3, 0.35, 0.10),
    MOTORCYCLE(3, 1, 0.05, 0.01);

    private final int widthCells;
    private final int heightCells;
    private final double emissionMoving;
    private final double emissionIdling;

    VehicleType(int widthCells, int heightCells,
            double emissionMoving, double emissionIdling) {
        this.widthCells = widthCells;
        this.heightCells = heightCells;
        this.emissionMoving = emissionMoving;
        this.emissionIdling = emissionIdling;
    }

    public int getWidthCells() {
        return widthCells;
    }

    public int getHeightCells() {
        return heightCells;
    }

    public double getEmissionMoving() {
        return emissionMoving;
    }

    public double getEmissionIdling() {
        return emissionIdling;
    }
}
