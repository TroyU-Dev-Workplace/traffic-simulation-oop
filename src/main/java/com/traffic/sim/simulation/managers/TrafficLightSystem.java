package com.traffic.sim.simulation.managers;

import com.traffic.sim.simulation.entities.TrafficLight;
import com.traffic.sim.simulation.entities.Region; // Nam add: Region mapping for traffic lights
import java.util.ArrayList;
import java.util.List;

public class TrafficLightSystem {
    private List<TrafficLight> trafficLights;
    private int timer;
    private Phase currentPhase;

    private enum Phase {
        NS_GREEN, NS_YELLOW, ALL_RED_1, EW_GREEN, EW_YELLOW, ALL_RED_2
    }

    private static final int GREEN_DURATION = 100;
    private static final int YELLOW_DURATION = 30;
    private static final int RED_BUFFER = 20;

    public TrafficLightSystem() {
        this.trafficLights = new ArrayList<>();
        initializeLights();
        this.currentPhase = Phase.NS_GREEN;
        this.timer = 0;
    }

    private void initializeLights() {
        // Initialize Vehicle Lights (based on intersection bounds 17-32, 10-25)
        // Nam fix: Position traffic lights at intersection approaches
        trafficLights.add(new TrafficLight("TL_V_NS_1", 24, 9, TrafficLight.TrafficLightType.VEHICLE,
                TrafficLight.Direction.NS, Region.NORTH));  // North approach
        trafficLights.add(new TrafficLight("TL_V_NS_2", 25, 26, TrafficLight.TrafficLightType.VEHICLE,
                TrafficLight.Direction.NS, Region.SOUTH)); // South approach
        trafficLights.add(new TrafficLight("TL_V_EW_1", 16, 17, TrafficLight.TrafficLightType.VEHICLE,
                TrafficLight.Direction.EW, Region.WEST));  // West approach
        trafficLights.add(new TrafficLight("TL_V_EW_2", 33, 18, TrafficLight.TrafficLightType.VEHICLE,
                TrafficLight.Direction.EW, Region.EAST));  // East approach

        // Initialize Pedestrian Lights (corners)
        trafficLights.add(new TrafficLight("TL_P_NS_1", 23, 16, TrafficLight.TrafficLightType.PEDESTRIAN,
                TrafficLight.Direction.NS, Region.NORTH)); // Top-Left-ish
        trafficLights.add(new TrafficLight("TL_P_EW_1", 27, 16, TrafficLight.TrafficLightType.PEDESTRIAN,
                TrafficLight.Direction.EW, Region.EAST)); // Top-Right-ish
        trafficLights.add(new TrafficLight("TL_P_NS_2", 23, 20, TrafficLight.TrafficLightType.PEDESTRIAN,
                TrafficLight.Direction.NS, Region.WEST)); // Bottom-Left-ish
        trafficLights.add(new TrafficLight("TL_P_EW_2", 27, 20, TrafficLight.TrafficLightType.PEDESTRIAN,
                TrafficLight.Direction.EW, Region.SOUTH)); // Bottom-Right-ish
    }

    public void addTrafficLight(TrafficLight tl) {
        trafficLights.add(tl);
    }

    public void update() {
        timer++;
        switch (currentPhase) {
            case NS_GREEN:
                setLights(TrafficLight.Direction.NS, TrafficLight.State.GREEN, TrafficLight.State.RED);
                if (timer > GREEN_DURATION)
                    switchPhase(Phase.NS_YELLOW);
                break;
            case NS_YELLOW:
                setLights(TrafficLight.Direction.NS, TrafficLight.State.YELLOW, TrafficLight.State.RED);
                if (timer > YELLOW_DURATION)
                    switchPhase(Phase.ALL_RED_1);
                break;
            case ALL_RED_1:
                setAllRed();
                if (timer > RED_BUFFER)
                    switchPhase(Phase.EW_GREEN);
                break;
            case EW_GREEN:
                setLights(TrafficLight.Direction.EW, TrafficLight.State.GREEN, TrafficLight.State.RED);
                if (timer > GREEN_DURATION)
                    switchPhase(Phase.EW_YELLOW);
                break;
            case EW_YELLOW:
                setLights(TrafficLight.Direction.EW, TrafficLight.State.YELLOW, TrafficLight.State.RED);
                if (timer > YELLOW_DURATION)
                    switchPhase(Phase.ALL_RED_2);
                break;
            case ALL_RED_2:
                setAllRed();
                if (timer > RED_BUFFER)
                    switchPhase(Phase.NS_GREEN);
                break;
        }
    }

    private void switchPhase(Phase nextPhase) {
        this.currentPhase = nextPhase;
        this.timer = 0;
    }

    private void setLights(TrafficLight.Direction activeVehicleDir, TrafficLight.State activeState,
            TrafficLight.State inactiveState) {
        for (TrafficLight tl : trafficLights) {
            if (tl.getType() == TrafficLight.TrafficLightType.VEHICLE) {
                if (tl.getDirection() == activeVehicleDir) {
                    tl.setState(activeState);
                } else {
                    tl.setState(inactiveState);
                }
            } else if (tl.getType() == TrafficLight.TrafficLightType.PEDESTRIAN) {
                // Pedestrian Logic: Walk only if PARALLEL Vehicle Axis is RED.
                // activeVehicleDir is the one that is NOT Red (Green or Yellow).
                // So if activeVehicleDir == NS, then NS Vehicles are moving. EW Vehicles are
                // Red.
                // Rule: If Vehicle E-W = RED -> Pedestrian E-W = WALK.
                // So if activeVehicleDir (NS) != EW, then EW Vehicles are Red -> Pedestrian EW
                // can Walk.

                // Simplified:
                // If tl.Direction != activeVehicleDir, it means the corresponding Vehicle axis
                // is Red.
                // However, we must ensure the active vehicle axis is strictly GREEN (not
                // Yellow) for safety?
                // User said: "Pedestrian WALK is only active when the vehicle axis is fully
                // RED."
                // In NS_GREEN phase: NS Vehicles Green. EW Vehicles Red. -> EW Pedestrians
                // Walk.
                // In NS_YELLOW phase: NS Vehicles Yellow. EW Vehicles Red. -> EW Pedestrians
                // Walk??
                // Usually Pedestrians should stop on Yellow.
                // Let's be safe: Pedestrians Walk ONLY if activeState is GREEN and they are on
                // the OTHER axis.

                if (activeState == TrafficLight.State.GREEN && tl.getDirection() != activeVehicleDir) {
                    tl.setState(TrafficLight.State.GREEN); // Walk
                } else {
                    tl.setState(TrafficLight.State.RED); // Don't Walk
                }
            }
        }
    }

    private void setAllRed() {
        for (TrafficLight tl : trafficLights) {
            tl.setState(TrafficLight.State.RED);
        }
    }

    public List<TrafficLight> getTrafficLights() {
        return trafficLights;
    }

    public void clear() {
        trafficLights.clear();
    }
}
