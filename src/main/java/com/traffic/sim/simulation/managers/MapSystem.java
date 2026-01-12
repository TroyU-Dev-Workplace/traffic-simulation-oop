package com.traffic.sim.simulation.managers;

import com.traffic.sim.simulation.map.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manager class that wraps Map and provides utility methods
 * for spawn logic, collision detection, and coordinate conversion.
 */
public class MapSystem {
    private Map map;
    private Random random;
    private final int TILE_SIZE = 20;

    private List<int[]> roadTiles;
    private List<int[]> sidewalkTiles;
    private List<int[]> crosswalkTiles;

    public MapSystem(int width, int height) {
        this.map = new Map(width, height);
        this.random = new Random();
        cacheWalkableTiles();
    }

    public void reset() {
        map.reset();
    }

    private void cacheWalkableTiles() {
        roadTiles = new ArrayList<>();
        sidewalkTiles = new ArrayList<>();
        crosswalkTiles = new ArrayList<>();

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                int tileType = map.getTileType(x, y);
                if (tileType == Map.ROAD) {
                    roadTiles.add(new int[] { x, y });
                } else if (tileType == Map.SIDEWALK) {
                    sidewalkTiles.add(new int[] { x, y });
                } else if (tileType == Map.CROSSWALK) {
                    crosswalkTiles.add(new int[] { x, y });
                }
            }
        }
    }

    public Map getMap() {
        return map;
    }

    public List<int[]> getRoadTiles() {
        return roadTiles;
    }

    public List<int[]> getSidewalkTiles() {
        return sidewalkTiles;
    }

    public List<int[]> getCrosswalkTiles() {
        return crosswalkTiles;
    }

    public int[] getRandomRoadTile() {
        if (roadTiles.isEmpty()) {
            return null;
        }
        return roadTiles.get(random.nextInt(roadTiles.size()));
    }

    public int[] getRandomSidewalkTile() {
        if (sidewalkTiles.isEmpty()) {
            return null;
        }
        return sidewalkTiles.get(random.nextInt(sidewalkTiles.size()));
    }

    public int[] getRandomCrosswalkTile() {
        if (crosswalkTiles.isEmpty()) {
            return null;
        }
        return crosswalkTiles.get(random.nextInt(crosswalkTiles.size()));
    }

    public boolean canVehicleMoveTo(int x, int y) {
        return map.isDrivable(x, y);
    }

    public boolean canPedestrianMoveTo(int x, int y) {
        return map.isWalkable(x, y);
    }

    public int getTileTypeAtPixel(double pixelX, double pixelY) {
        int tileX = (int) (pixelX / TILE_SIZE);
        int tileY = (int) (pixelY / TILE_SIZE);
        return map.getTileType(tileX, tileY);
    }

    public int[] pixelToTile(double pixelX, double pixelY) {
        int tileX = (int) (pixelX / TILE_SIZE);
        int tileY = (int) (pixelY / TILE_SIZE);
        return new int[] { tileX, tileY };
    }

    public double[] tileToPixel(int tileX, int tileY) {
        double pixelX = tileX * TILE_SIZE + TILE_SIZE / 2.0;
        double pixelY = tileY * TILE_SIZE + TILE_SIZE / 2.0;
        return new double[] { pixelX, pixelY };
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    public int getWidthInTiles() {
        return map.getWidth();
    }

    public int getHeightInTiles() {
        return map.getHeight();
    }

    public int getWidthInPixels() {
        return map.getWidth() * TILE_SIZE;
    }

    public int getHeightInPixels() {
        return map.getHeight() * TILE_SIZE;
    }
}
