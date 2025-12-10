package com.traffic.sim.simulation.managers;

import com.traffic.sim.simulation.map.Map;

public class MapSystem {
    private Map map;

    public MapSystem(int width, int height) {
        this.map = new Map(width, height);
    }

    public Map getMap() {
        return map;
    }
}
