package com.traffic.sim.simulation.managers;

import com.traffic.sim.simulation.entities.Pedestrian;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedestrianManager {
    private Map<String, Pedestrian> pedestrians;

    public PedestrianManager() {
        this.pedestrians = new HashMap<>();
    }

    public void addPedestrian(Pedestrian pedestrian) {
        pedestrians.put(pedestrian.getId(), pedestrian);
    }

    public void updateWithMap(com.traffic.sim.simulation.map.Map map, Object tls) {
        com.traffic.sim.simulation.managers.TrafficLightSystem system = (com.traffic.sim.simulation.managers.TrafficLightSystem) tls;

        List<String> toRemove = new ArrayList<>();
        for (Pedestrian p : pedestrians.values()) {
            p.update(map, system);
            if (p.isRemoved()) {
                toRemove.add(p.getId());
            }
        }

        for (String id : toRemove) {
            pedestrians.remove(id);
        }
    }

    public List<Pedestrian> getPedestrians() {
        return new ArrayList<>(pedestrians.values());
    }

    public Pedestrian getPedestrian(String id) {
        return pedestrians.get(id);
    }

    public void removePedestrian(String id) {
        pedestrians.remove(id);
    }

    public void clear() {
        pedestrians.clear();
    }
}
