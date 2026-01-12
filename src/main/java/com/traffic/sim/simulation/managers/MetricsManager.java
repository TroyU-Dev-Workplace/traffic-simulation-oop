package com.traffic.sim.simulation.managers;

import com.traffic.sim.simulation.entities.Vehicle;
import com.traffic.sim.simulation.map.Map;
import java.util.ArrayList;
import java.util.List;

public class MetricsManager {

    private final Map map;

    // --- METRICS DATA ---
    private int exitedVehicleCount = 0;

    private double totalTravelTime = 0;
    private double totalWaitingTime = 0;
    private double totalCO2 = 0; // accumulated for exited vehicles
    private int frustratedExitedCount = 0;

    // Time window for throughput (last 60s)
    private static final int THROUGHPUT_WINDOW_MS = 60000;
    private List<Long> exitTimestamps = new ArrayList<>();

    // Real-time calculated metrics
    private double throughput = 0; // vehicles/min
    private double occupancy = 0; // %
    private boolean isCongested = false;
    private int congestionTimer = 0; // frames

    // Deadlock / Blockage
    private int totalFrames = 0;
    private int deadlockFrames = 0;
    private int blockageCount = 0;
    private int blockageTimer = 0;
    private double currentDeadlockRate = 0; // % Instantaneous
    private static final int BLOCKAGE_TIME_THRESHOLD = 300; // 5 seconds of no movement with occupancy

    // --- THRESHOLDS ---
    private static final double CONGESTION_THRESHOLD = 40.0; // % (Scaled)
    private static final int CONGESTION_TIME_THRESHOLD = 60; // 1 seconds @ 60fps
    private static final double FRUSTRATION_WAIT_THRESHOLD = 60.0; // seconds

    // Driveable area count
    private int totalDriveableTiles = 0;

    // Storing current active sums for "Final" retrieval (Summary Metrics)
    // These are reset and recalculated every frame in update() ->
    // calculateOccupancyAndDeadlock()
    private double currentActiveWaitSum = 0;
    private double currentActiveCO2Sum = 0;
    private int currentActiveFrustrated = 0;
    private int currentActiveCount = 0;

    public MetricsManager(Map map) {
        this.map = map;
        calculateDriveableArea();
    }

    private void calculateDriveableArea() {
        totalDriveableTiles = 0;
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.isDriveable(x, y)) {
                    totalDriveableTiles++;
                }
            }
        }
    }

    public void update(List<Vehicle> activeVehicles) {
        totalFrames++;

        // 1. Throughput Calculation (Sliding Window)
        updateThroughput();

        // 2. Occupancy & Deadlock Calculation (and Active Stats Accumulation)
        calculateOccupancyAndDeadlock(activeVehicles);

        // 3. Congestion Check
        checkCongestion();
    }

    public void registerVehicleExit(Vehicle v) {
        exitedVehicleCount++;
        totalTravelTime += (v.getTravelTime() / 1000.0); // convert to seconds
        totalWaitingTime += v.getAccumulatedWaitingTime();
        totalCO2 += v.getTotalCO2();

        exitTimestamps.add(System.currentTimeMillis());

        // Frustration Logic (On Exit - Summary)
        boolean waitCondition = v.getAccumulatedWaitingTime() > FRUSTRATION_WAIT_THRESHOLD;
        boolean stopCondition = v.getStopCount() > 5; // Frequent stops
        if (waitCondition || stopCondition) {
            frustratedExitedCount++;
        }
    }

    public void reset() {
        exitedVehicleCount = 0;
        totalTravelTime = 0;
        totalWaitingTime = 0;
        totalCO2 = 0;
        frustratedExitedCount = 0;
        exitTimestamps.clear();

        throughput = 0;
        occupancy = 0;
        isCongested = false;
        congestionTimer = 0;

        totalFrames = 0;
        deadlockFrames = 0;
        blockageCount = 0;
        currentDeadlockRate = 0;
        blockageTimer = 0;

        // Reset active accumulators
        currentActiveWaitSum = 0;
        currentActiveCO2Sum = 0;
        currentActiveFrustrated = 0;
        currentActiveCount = 0;

        // Recalculate driveable area in case map reset (rare but safe)
        if (map != null)
            calculateDriveableArea();
    }

    // --- INTERNAL CALCULATIONS ---

    private void updateThroughput() {
        long now = System.currentTimeMillis();
        // Remove exits older than 60s
        exitTimestamps.removeIf(time -> (now - time) > THROUGHPUT_WINDOW_MS);
        throughput = exitTimestamps.size(); // vehicles per minute (rolling window)
    }

    private void calculateOccupancyAndDeadlock(List<Vehicle> activeVehicles) {
        double totalVehicleArea = 0;
        int movingVehicles = 0;

        // Reset real-time accumulators for this frame
        currentActiveCount = 0;
        currentActiveWaitSum = 0;
        currentActiveCO2Sum = 0;
        currentActiveFrustrated = 0;

        for (Vehicle v : activeVehicles) {
            totalVehicleArea += v.getArea();
            if (v.isMoving()) {
                movingVehicles++;
            }

            // Accumulate active vehicle stats for potential Summary
            currentActiveCount++;
            currentActiveWaitSum += v.getAccumulatedWaitingTime();
            currentActiveCO2Sum += v.getTotalCO2();

            boolean waitCondition = v.getAccumulatedWaitingTime() > FRUSTRATION_WAIT_THRESHOLD;
            boolean stopCondition = v.getStopCount() > 5; // Frequent stops
            if (waitCondition || stopCondition) {
                currentActiveFrustrated++;
            }
        }

        // Occupancy based on DRIVEABLE area
        // SCALING: User perceives 20% as "Low" even in gridlock due to wide
        // roads/shoulders.
        // We scale by 3.0 to approximate "Lane Occupancy" (effective density).
        if (totalDriveableTiles > 0) {
            double rawOccupancy = (totalVehicleArea / totalDriveableTiles) * 100.0;
            occupancy = Math.min(100.0, rawOccupancy * 3.0);
        }
        //

        // Deadlock / Logic Failure Detection
        // Relaxed Definition: If > 50% of vehicles are NOT moving, consider it a
        // deadlock risk
        int stoppedVehicles = activeVehicles.size() - movingVehicles;
        boolean isHighStopRate = false;

        if (!activeVehicles.isEmpty()) {
            currentDeadlockRate = ((double) stoppedVehicles / activeVehicles.size()) * 100.0;
            if (currentDeadlockRate > 50.0) {
                isHighStopRate = true;
            }
        } else {
            currentDeadlockRate = 0.0;
        }

        if (isHighStopRate) {
            deadlockFrames++;

            // Blockage logic: Prolonged standstill
            blockageTimer++;
            if (blockageTimer == BLOCKAGE_TIME_THRESHOLD) {
                blockageCount++;
                blockageTimer = 0;
            }
        } else {
            blockageTimer = 0;
        }
    }

    private void checkCongestion() {
        if (occupancy > CONGESTION_THRESHOLD) {
            congestionTimer++;
            if (congestionTimer > CONGESTION_TIME_THRESHOLD) {
                isCongested = true;
            }
        } else {
            congestionTimer = 0;
            isCongested = false;
        }
    }

    // --- GETTERS ---

    // Real-time
    public double getThroughput() {
        return throughput;
    }

    public double getOccupancy() {
        return occupancy;
    }

    public boolean isCongested() {
        return isCongested;
    }

    public double getDeadlockRate() {
        return currentDeadlockRate;
    }

    public int getBlockageCount() {
        return blockageCount;
    }

    // SUMMARY METRICS: NOW INCLUDE ACTIVE VEHICLES FOR DEADLOCK SCENARIOS

    public double getFinalAvgWaitingTime() {
        int total = exitedVehicleCount + currentActiveCount;
        if (total == 0)
            return 0.0;
        return (totalWaitingTime + currentActiveWaitSum) / total;
    }

    public double getFinalAvgTravelTime() {
        if (exitedVehicleCount == 0)
            return 0.0;
        return totalTravelTime / exitedVehicleCount; // Travel time only meaningful for completed trips
    }

    public double getFinalTotalCO2() {
        return totalCO2 + currentActiveCO2Sum;
    }

    public double getFinalFrustrationRate() {
        int total = exitedVehicleCount + currentActiveCount;
        if (total == 0)
            return 0.0;
        return ((double) (frustratedExitedCount + currentActiveFrustrated) / total) * 100.0;
    }

    public int getExitedCount() {
        return exitedVehicleCount;
    }
}
