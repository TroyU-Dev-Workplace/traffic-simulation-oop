package com.traffic.sim.simulation.managers;

import java.util.ArrayList;
import java.util.List;

import com.traffic.sim.simulation.entities.Region;
import com.traffic.sim.simulation.entities.TrafficLight;
import com.traffic.sim.simulation.entities.TrafficLight.Direction;
import com.traffic.sim.simulation.entities.TrafficLight.State;
import com.traffic.sim.simulation.entities.TrafficLight.TrafficLightType;

/**
 * TrafficLightSystem
 * Manages the state machine, timing, and synchronization of all traffic lights.
 * Handles the logic for both vehicles and pedestrians.
 */
public class TrafficLightSystem {
    private List<TrafficLight> trafficLights;

    // --- TIMING CONFIGURATION (Frames) ---
    // calculated based on a fixed 60 FPS update loop.
    private int greenDuration;
    private int yellowDuration;
    private int redBuffer; // All-Red phase duration for safety clearance

    private int timer;
    private Phase currentPhase;

    // State Machine: Defines the cycle sequence
    private enum Phase {
        NS_GREEN, NS_YELLOW, ALL_RED_1,
        EW_GREEN, EW_YELLOW, ALL_RED_2
    }

    private static final int GREEN_DURATION = 150;
    private static final int YELLOW_DURATION = 30;
    private static final int RED_BUFFER = 20;

    public TrafficLightSystem() {
        this.trafficLights = new ArrayList<>();
        initializeLights();

        // Initialize state
        this.currentPhase = Phase.NS_GREEN;
        this.timer = 0;

        // Load the default realistic scenario on startup
        loadDefaultScenario();
    }

    // --- SCENARIO MANAGEMENT ---

    // Scenario 1: Realistic
    // Balanced settings for standard traffic flow.
    // Green: 15s | Yellow: 3s | Buffer: 2s
    public void loadDefaultScenario() {
        setDurations(15, 3, 2);
        System.out.println("LOG: Loaded REALISTIC Scenario (15s Green / 3s Yellow / 2s Buffer)");
    }

    // Scenario 2: Short Cycle
    // Optimized for debugging and quick testing.
    // Green: 8s | Yellow: 2s
    public void loadShortCycleScenario() {
        setDurations(8, 2, 1);
        System.out.println("LOG: Loaded FAST Scenario (8s Green / 2s Yellow / 1s Buffer)");
    }

    // Scenario 3: Long Cycle
    // Designed to flush heavy traffic volume.
    // Green: 25s | Yellow: 4s
    public void loadLongCycleScenario() {
        setDurations(25, 4, 2);
        System.out.println("LOG: Loaded HEAVY TRAFFIC Scenario (25s Green / 4s Yellow / 2s Buffer)");
    }

    // --- UI INTERACTION API ---

    /**
     * Public API exposed for the UI layer (Person 1).
     * Allows dynamic reconfiguration of signal timings.
     * Resets the timer immediately to apply changes.
     */
    public void setDurations(int greenSec, int yellowSec, int redBufferSec) {
        this.greenDuration = greenSec * 60;
        this.yellowDuration = yellowSec * 60;
        this.redBuffer = redBufferSec * 60;

        // Reset logic to apply new config instantly
        this.timer = 0;
        this.currentPhase = Phase.NS_GREEN;
        updateLights();
        System.out.println("LOG: Custom Config Applied by UI");
    }

    // --- MAIN SIMULATION LOOP ---

    public void update() {
        timer++;

        // Ensure visual state is always synced with logical state
        updateLights();

        // Time-based State Machine transitions
        switch (currentPhase) {
            case NS_GREEN:
                if (timer > greenDuration)
                    switchPhase(Phase.NS_YELLOW);
                break;
            case NS_YELLOW:
                if (timer > yellowDuration)
                    switchPhase(Phase.ALL_RED_1);
                break;
            case ALL_RED_1: // Safety Buffer 1
                if (timer > redBuffer)
                    switchPhase(Phase.EW_GREEN);
                break;
            case EW_GREEN:
                if (timer > greenDuration)
                    switchPhase(Phase.EW_YELLOW);
                break;
            case EW_YELLOW:
                if (timer > yellowDuration)
                    switchPhase(Phase.ALL_RED_2);
                break;
            case ALL_RED_2: // Safety Buffer 2
                if (timer > redBuffer)
                    switchPhase(Phase.NS_GREEN);
                break;
        }
    }

    private void switchPhase(Phase nextPhase) {
        this.currentPhase = nextPhase;
        this.timer = 0;
    }

    // --- LIGHT SYNCHRONIZATION LOGIC ---

    private void updateLights() {
        switch (currentPhase) {
            case NS_GREEN:
                setPhaseColors(Direction.NS, State.GREEN);
                break;
            case NS_YELLOW:
                setPhaseColors(Direction.NS, State.YELLOW);
                break;
            case ALL_RED_1:
            case ALL_RED_2:
                setAllRed(); // Safety: Stop all traffic
                break;
            case EW_GREEN:
                setPhaseColors(Direction.EW, State.GREEN);
                break;
            case EW_YELLOW:
                setPhaseColors(Direction.EW, State.YELLOW);
                break;
        }
    }

    private void setPhaseColors(Direction activeDir, State activeState) {
        for (TrafficLight tl : trafficLights) {
            // Logic for Vehicles: Follow the active direction
            if (tl.getType() == TrafficLightType.VEHICLE) {
                if (tl.getDirection() == activeDir) {
                    tl.setState(activeState);
                } else {
                    tl.setState(State.RED);
                }
            }
            // Logic for Pedestrians: Inverse synchronization
            // Pedestrians only walk when parallel vehicle traffic stops.
            else if (tl.getType() == TrafficLightType.PEDESTRIAN) {
                boolean isVehicleMovingHere = (tl.getDirection() == activeDir);

                if (isVehicleMovingHere) {
                    tl.setState(State.RED); // Stop if cars are moving
                } else {
                    tl.setState(State.GREEN); // Go if cars are stopped
                }
            }
        }
    }

    private void setAllRed() {
        for (TrafficLight tl : trafficLights) {
            tl.setState(State.RED);
            // Critical: Ensure pedestrians also wait during the All-Red buffer
            // to allow clearing of the intersection.
            if (tl.getType() == TrafficLightType.PEDESTRIAN) {
                tl.setState(State.RED);
            }
        }
    }

    // --- CORE SIMULATION API (Person 5) ---

    /**
     * Used by the Simulation Core to determine if a vehicle must stop.
     * Returns RED if the specific signal is not found (Safety default).
     */
    public State getSignal(Direction dir) {
        for (TrafficLight tl : trafficLights) {
            if (tl.getType() == TrafficLightType.VEHICLE && tl.getDirection() == dir) {
                return tl.getCurrentState();
            }
        }
        return State.RED;
    }

    public State getPedestrianSignal(Direction dir) {
        for (TrafficLight tl : trafficLights) {
            if (tl.getType() == TrafficLightType.PEDESTRIAN && tl.getDirection() == dir) {
                return tl.getCurrentState();
            }
        }
        return State.RED;
    }

    // Exposed for the Renderer to draw the entities
    public List<TrafficLight> getTrafficLights() {
        return trafficLights;
    }

    private void initializeLights() {
        // Init Vehicles
        trafficLights.add(new TrafficLight("TL_V_NS_1", 24, 16, TrafficLightType.VEHICLE, Direction.NS, Region.NORTH));
        trafficLights.add(new TrafficLight("TL_V_NS_2", 26, 20, TrafficLightType.VEHICLE, Direction.NS, Region.WEST));
        trafficLights.add(new TrafficLight("TL_V_EW_1", 22, 19, TrafficLightType.VEHICLE, Direction.EW, Region.SOUTH));
        trafficLights.add(new TrafficLight("TL_V_EW_2", 28, 17, TrafficLightType.VEHICLE, Direction.EW, Region.EAST));
        // Init Pedestrians
        trafficLights
                .add(new TrafficLight("TL_P_NS_1", 23, 16, TrafficLightType.PEDESTRIAN, Direction.NS, Region.NORTH));
        trafficLights
                .add(new TrafficLight("TL_P_EW_1", 27, 16, TrafficLightType.PEDESTRIAN, Direction.EW, Region.WEST));
        trafficLights
                .add(new TrafficLight("TL_P_NS_2", 23, 20, TrafficLightType.PEDESTRIAN, Direction.NS, Region.SOUTH));
        trafficLights
                .add(new TrafficLight("TL_P_EW_2", 27, 20, TrafficLightType.PEDESTRIAN, Direction.EW, Region.EAST));
    }
}