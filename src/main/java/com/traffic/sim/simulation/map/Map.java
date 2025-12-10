package com.traffic.sim.simulation.map;

/**
 * Represents the simulation grid.
 * - 0 = road
 * - 1 = blocked/obstacle
 * - 2 = sidewalk (pedestrians only)
 */
public class Map {
    private int[][] grid;
    private int width;
    private int height;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
        initializeDefaultMap();
    }

    private void initializeDefaultMap() {
        // Simple initialization:
        // Borders are blocks (1)
        // Middle is road (0)
        // Sidewalks on edges of road (2)
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    grid[y][x] = 1; // Blocked
                } else if (x == 1 || x == width - 2 || y == 1 || y == height - 2) {
                    grid[y][x] = 2; // Sidewalk
                } else {
                    grid[y][x] = 0; // Road
                }
            }
        }
    }
    
    public int getTileType(int x, int y) {
        if (isValid(x, y)) {
            return grid[y][x];
        }
        return 1; // Treat out of bounds as blocked
    }
    
    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
