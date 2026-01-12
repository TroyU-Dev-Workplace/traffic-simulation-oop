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
    private static final int BLOCKAGE_TIME_THRESHOLD = 300; // 5 seconds of no movement with occupancy

    // --- THRESHOLDS ---
    private static final double CONGESTION_THRESHOLD = 80.0; // %
    private static final int CONGESTION_TIME_THRESHOLD = 180; // 3 seconds @ 60fps
    private static final double FRUSTRATION_WAIT_THRESHOLD = 60.0; // seconds

    public MetricsManager(Map map) {
        this.map = map;
    }

    public void update(List<Vehicle> activeVehicles) {
        totalFrames++;

        // 1. Throughput Calculation (Sliding Window)
        updateThroughput();

        // 2. Occupancy & Deadlock Calculation
        calculateOccupancyAndDeadlock(activeVehicles);

        // 3. Congestion Check
        checkCongestion();

        // 4. Update Real-time Average metrics (for display)
        updateRealTimeMetrics(activeVehicles);
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
        blockageTimer = 0;

        currentActiveAvgWait = 0;
        currentActiveFrustratedCount = 0;
        currentActiveCount = 0;
    }

    // --- INTERNAL CALCULATIONS ---

    private double currentActiveAvgWait = 0;
    private int currentActiveFrustratedCount = 0;
    private int currentActiveCount = 0;

    private void updateRealTimeMetrics(List<Vehicle> activeVehicles) {
        currentActiveCount = activeVehicles.size();
        if (currentActiveCount == 0) {
            currentActiveAvgWait = 0;
            currentActiveFrustratedCount = 0;
            return;
        }

        double totalActiveWait = 0;
        int frustratedCount = 0;

        for (Vehicle v : activeVehicles) {
            totalActiveWait += v.getAccumulatedWaitingTime();
            boolean waitCondition = v.getAccumulatedWaitingTime() > FRUSTRATION_WAIT_THRESHOLD;
            boolean stopCondition = v.getStopCount() > 5;
            if (waitCondition || stopCondition) {
                frustratedCount++;
            }
        }

        currentActiveAvgWait = totalActiveWait / currentActiveCount;
        currentActiveFrustratedCount = frustratedCount;
    }

    private void updateThroughput() {
        long now = System.currentTimeMillis();
        // Remove exits older than 60s
        exitTimestamps.removeIf(time -> (now - time) > THROUGHPUT_WINDOW_MS);
        throughput = exitTimestamps.size(); // vehicles per minute (rolling window)
    }

    private void calculateOccupancyAndDeadlock(List<Vehicle> activeVehicles) {
        double totalVehicleArea = 0;
        int movingVehicles = 0;

        for (Vehicle v : activeVehicles) {
            totalVehicleArea += v.getArea();
            if (v.isMoving()) {
                movingVehicles++;
            }
        }

        // Map dimensions
        double totalArea = map.getWidth() * map.getHeight();
        if (totalArea > 0) {
            occupancy = (totalVehicleArea / totalArea) * 100.0;
        }

        // Deadlock / Logic Failure Detection
        // Relaxed Definition: If > 50% of vehicles are NOT moving, consider it a
        // deadlock risk
        int stoppedVehicles = activeVehicles.size() - movingVehicles;
        boolean isHighStopRate = !activeVehicles.isEmpty() && ((double) stoppedVehicles / activeVehicles.size() > 0.5);

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
        if (totalFrames == 0)
            return 0.0;
        return ((double) deadlockFrames / totalFrames) * 100.0;
    }

    public int getBlockageCount() {
        return blockageCount;
    }

    // Summary (Post-Simulation) - Returns aggregates based on exited vehicles AND
    // active vehicles for live verification
    public double getFinalAvgWaitingTime() {
        int totalCount = exitedVehicleCount + currentActiveCount;
        if (totalCount == 0)
            return 0.0;

        double totalWait = totalWaitingTime + (currentActiveAvgWait * currentActiveCount);
        return totalWait / totalCount;
    }

    public double getFinalAvgTravelTime() {
        if (exitedVehicleCount == 0)
            return 0.0;
        // For travel time, we typically only count completed trips,
        // as active ones are technically infinite until done.
        return totalTravelTime / exitedVehicleCount;
    }

    public double getFinalTotalCO2() {
        return totalCO2; // Note: You might want to add active vehicles CO2 here if not already
                         // accumulating somewhere?
                         // Vehicle.totalCO2 accumulates in update(), but we only add to this.totalCO2 on
                         // exit.
                         // For now, let's leave CO2 as exit-based or update if requested.
                         // To match WaitTime pattern, we generally should include active CO2.
                         // But for now, sticking to the requested fixes.
    }

    public double getFinalFrustrationRate() {
        int totalCount = exitedVehicleCount + currentActiveCount;
        if (totalCount == 0)
            return 0.0;

        int totalFrustrated = frustratedExitedCount + currentActiveFrustratedCount;
        return ((double) totalFrustrated / totalCount) * 100.0;
    }

    public int getExitedCount() {
        return exitedVehicleCount;
    }
}
