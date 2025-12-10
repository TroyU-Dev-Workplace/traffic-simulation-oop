package com.traffic.sim.simulation.spawn;

import com.traffic.sim.simulation.managers.MapSystem;

public abstract class SpawnBase {
    protected MapSystem mapSystem;

    public SpawnBase(MapSystem mapSystem) {
        this.mapSystem = mapSystem;
    }

    public abstract void spawn();
}
