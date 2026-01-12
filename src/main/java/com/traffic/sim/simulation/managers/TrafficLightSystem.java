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
    // 60 ticks = 1 second.
    // Default Scenario: FAST (for testing)
    // Green: 4s (240 ticks) | Yellow: 2s (120 ticks) | Buffer: 1s (60 ticks)
    private static final int DEFAULT_GREEN = 240;
    private static final int DEFAULT_YELLOW = 120;
    private static final int DEFAULT_BUFFER = 60; // All-Red buffer

    // Independent Controllers for each direction
    private final DirectionController nsController;
    private final DirectionController ewController;

    // Inner Class: Independent Traffic Controller
    // Manages the phase cycle for a single direction strictly by timer.
    private class DirectionController {
        private Phase phase;
        private int timer;

        private int greenDuration;
        private int yellowDuration;
        private int redDuration; // Current Red duration (may vary if we want asymmetry, but standardized here)

        private int cycleCount = 0;

        public DirectionController(Phase startPhase, int g, int y, int r) {
            this.phase = startPhase;
            this.greenDuration = g;
            this.yellowDuration = y;
            this.redDuration = r;
            this.timer = 0;
            this.cycleCount = 0;
        }

        public void update() {
            timer++;
            switch (phase) {
                case GREEN:
                    if (timer > greenDuration)
                        switchPhase(Phase.YELLOW);
                    break;
                case YELLOW:
                    if (timer > yellowDuration)
                        switchPhase(Phase.RED);
                    break;
                case RED:
                    if (timer > redDuration) {
                        switchPhase(Phase.GREEN);
                        cycleCount++; // Completed a full Red cycle
                    }
                    break;
            }
        }

        public int getCycleCount() {
            return cycleCount;
        }

        private void switchPhase(Phase next) {
            this.phase = next;
            this.timer = 0;
        }

        public void setDurations(int g, int y, int r) {
            this.greenDuration = g;
            this.yellowDuration = y;
            this.redDuration = r;
        }

        public void reset(Phase startPhase) {
            this.phase = startPhase;
            this.timer = 0;
        }

        public State getState() {
            switch (phase) {
                case GREEN:
                    return State.GREEN;
                case YELLOW:
                    return State.YELLOW;
                default:
                    return State.RED;
            }
        }
    }

    // Simplified Phase Enum for single direction
    private enum Phase {
        GREEN, YELLOW, RED
    }

    public TrafficLightSystem() {
        this.trafficLights = new ArrayList<>();
        initializeLights();

        // Initialize Independent Controllers
        // NS Starts GREEN
        // EW Starts RED
        // Note: For EW to start in Red, we calculate its RED duration.

        this.nsController = new DirectionController(Phase.GREEN, DEFAULT_GREEN, DEFAULT_YELLOW,
                DEFAULT_BUFFER + DEFAULT_GREEN + DEFAULT_YELLOW);
        this.ewController = new DirectionController(Phase.RED, DEFAULT_GREEN, DEFAULT_YELLOW,
                DEFAULT_BUFFER + DEFAULT_GREEN + DEFAULT_YELLOW);

        // Initial Defaults Load (just to sync standard values if needed, mostly covered
        // by constructor)
        loadDefaultScenario();
    }

    // --- SCENARIO MANAGEMENT ---

    public void loadDefaultScenario() {
        setDurations(4, 2, 5); // Example: 4s Green, 2s Yellow, 5s Red
    }

    // --- UI INTERACTION API ---

    /**
     * Public API exposed for the UI layer.
     * Allows dynamic reconfiguration of signal timings.
     * Resets both controllers immediately to apply strict timing.
     */
    public void setDurations(int greenSec, int yellowSec, int redSec) {
        // Convert to frames
        int g = greenSec * 60;
        int y = yellowSec * 60;
        int r = redSec * 60;

        // Update settings
        nsController.setDurations(g, y, r);
        ewController.setDurations(g, y, r);

        // Reset to initial deterministic state
        // NS: Starts at Green
        nsController.reset(Phase.GREEN);
        // EW: Starts at Red
        ewController.reset(Phase.RED);

        updateLights();
    }

    // --- MAIN SIMULATION LOOP ---

    public void update() {
        // Update both controllers independently
        nsController.update();
        ewController.update();

        // Sync visual state
        updateLights();
    }

    public void reset() {
        // Reset Independent Controllers to initial state
        nsController.reset(Phase.GREEN);
        ewController.reset(Phase.RED);
        updateLights();
    }

    // --- LIGHT SYNCHRONIZATION LOGIC ---

    private void updateLights() {
        // Get strict states from independent controllers
        State nsState = nsController.getState();
        State ewState = ewController.getState();

        // Apply to NS Lights
        setPhaseColors(Direction.NS, nsState);

        // Apply to EW Lights
        setPhaseColors(Direction.EW, ewState);
    }

    private void setPhaseColors(Direction activeDir, State activeState) {
        for (TrafficLight tl : trafficLights) {
            // Filter: Only touch lights belonging to the active direction
            if (tl.getDirection() != activeDir) {
                continue;
            }

            // Logic for Vehicles: Follow the active state directly
            if (tl.getType() == TrafficLightType.VEHICLE) {
                tl.setState(activeState);
            }
            // Logic for Pedestrians:
            // STRICT INVERSE LOGIC:
            // 1. If Same Direction Vehicle is GREEN -> Pedestrian RED.
            // 2. If Same Direction Vehicle is YELLOW or RED -> Pedestrian GREEN.
            // (Pedestrians cross when parallel traffic is STOPPING or STOPPED)
            else if (tl.getType() == TrafficLightType.PEDESTRIAN) {
                // Since strictly same direction, simplify logic:
                if (activeState == State.GREEN) {
                    tl.setState(State.RED); // Vehicle GREEN -> Ped RED
                } else {
                    tl.setState(State.GREEN); // Vehicle YELLOW/RED -> Ped GREEN
                }
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

    public State getPedestrianSignal(Region region) {
        for (TrafficLight tl : trafficLights) {
            if (tl.getType() == TrafficLightType.PEDESTRIAN && tl.getRegion() == region) {
                return tl.getCurrentState();
            }
        }
        return State.RED;
    }

    public int getCycleCount() {
        // Return the max cycle count (e.g., from NS which is main controller or max of
        // both)
        return Math.max(nsController.getCycleCount(), ewController.getCycleCount());
    }

    // Exposed for the Renderer to draw the entities
    public List<TrafficLight> getTrafficLights() {
        return trafficLights;
    }

    private void initializeLights() {
        // Init Vehicles
        trafficLights.add(new TrafficLight("TL_V_NS_1", 24, 16, TrafficLightType.VEHICLE, Direction.NS, Region.NORTH));
        trafficLights.add(new TrafficLight("TL_V_NS_2", 26, 20, TrafficLightType.VEHICLE, Direction.NS, Region.SOUTH));
        trafficLights.add(new TrafficLight("TL_V_EW_1", 22, 19, TrafficLightType.VEHICLE, Direction.EW, Region.WEST));
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