package com.traffic.sim.simulation.map;

/**
 * Represents the simulation grid with 2-way lane system.
 * Nam adding: Simplified lane system
 * - 0 = blocked/building
 * - 1 = sidewalk (pedestrians only)
 * - 2 = road (all directions)
 */
public class Map {
    private int[][] grid;
    private int width;
    private int height;
    private Double intersectionCenterX;
    private Double intersectionCenterY;
    private Integer intersectionMinX; //Nam add: Intersection bounds cache
    private Integer intersectionMinY; //Nam add: Intersection bounds cache
    private Integer intersectionMaxX; //Nam add: Intersection bounds cache
    private Integer intersectionMaxY; //Nam add: Intersection bounds cache

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
        initializeDefaultMap();
    }

    private void initializeDefaultMap() {
        // Nam adding: Try to load map from Map_grid_2way_lanes.txt first
        if (loadMapFromFile("Map_grid_2way_lanes.txt")) {
            // Nam add: Overlay layout sizes from reference Map.java (blocked/sidewalk)
            applyReferenceLayout();
            return;
        }

        // Nam adding: Fallback to blocked map if file missing
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = 0; // Blocked by default
            }
        }
    }

    // Nam adding: Parse the ASCII map file into grid (0/1/2)
    private boolean loadMapFromFile(String filePath) {
        java.nio.file.Path path = java.nio.file.Paths.get(filePath);
        if (!java.nio.file.Files.exists(path)) {
            return false;
        }

        java.util.List<String> rows = new java.util.ArrayList<>();
        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(path);
            for (String line : lines) {
                if (!line.contains("|")) {
                    continue;
                }
                int start = line.indexOf('|');
                int end = line.lastIndexOf('|');
                if (start == end) {
                    continue;
                }
                String core = line.substring(start + 1, end);
                int commentIndex = core.indexOf('#');
                if (commentIndex >= 0) {
                    core = core.substring(0, commentIndex);
                }

                StringBuilder row = new StringBuilder();
                boolean inParen = false;
                for (int i = 0; i < core.length(); i++) {
                    char c = core.charAt(i);
                    if (c == '(') {
                        inParen = true;
                        row.append('2'); // Keep labels from breaking road continuity
                        continue;
                    }
                    if (c == ')') {
                        inParen = false;
                        row.append('2');
                        continue;
                    }
                    if (inParen) {
                        row.append('2');
                        continue;
                    }
                    if (c == '0' || c == '1' || c == '2') {
                        row.append(c);
                    } else {
                        row.append('2'); // Nam adding: treat spaces/labels as road
                    }
                }

                // Nam adding: Normalize row width
                if (row.length() > width) {
                    row.setLength(width);
                } else if (row.length() < width) {
                    while (row.length() < width) {
                        row.append('0');
                    }
                }
                rows.add(row.toString());
            }
        } catch (java.io.IOException e) {
            return false;
        }

        // Nam adding: Fill grid from parsed rows
        for (int y = 0; y < height; y++) {
            if (y < rows.size()) {
                String row = rows.get(y);
                for (int x = 0; x < width; x++) {
                    char c = row.charAt(x);
                    grid[y][x] = (c == '1') ? 1 : (c == '2') ? 2 : 0;
                }
            } else {
                for (int x = 0; x < width; x++) {
                    grid[y][x] = 0; // Nam adding: pad missing rows as blocked
                }
            }
        }

        return true;
    }

    // Nam add: Apply layout sizes from reference Map.java to adjust blocked/sidewalk areas
    private void applyReferenceLayout() {
        // Nam add: Blocked corners
        fillArea(0, 0, 16, 9, 0); // Top-left blocked
        fillArea(33, 0, 49, 9, 0); // Top-right blocked
        fillArea(0, 26, 16, 35, 0); // Bottom-left blocked
        fillArea(33, 26, 49, 35, 0); // Bottom-right blocked

        // Nam add: Sidewalk borders for corners
        fillArea(15, 0, 16, 9, 1); // Top-left right edge
        fillArea(0, 8, 14, 9, 1); // Top-left bottom edge
        fillArea(33, 0, 34, 9, 1); // Top-right left edge
        fillArea(35, 8, 49, 9, 1); // Top-right bottom edge
        fillArea(0, 26, 14, 27, 1); // Bottom-left top edge
        fillArea(15, 26, 16, 35, 1); // Bottom-left right edge
        fillArea(35, 26, 49, 27, 1); // Bottom-right top edge
        fillArea(33, 26, 34, 35, 1); // Bottom-right left edge

        // Nam add: Crosswalks mapped as road to keep vehicles moving
        fillArea(17, 7, 32, 8, 2); // Top crosswalk
        fillArea(17, 27, 32, 28, 2); // Bottom crosswalk
        fillArea(14, 10, 15, 25, 2); // Left crosswalk
        fillArea(34, 10, 35, 25, 2); // Right crosswalk

        // Nam add: Lane dividers and stop lines treated as road in this simplified map
        fillArea(24, 0, 25, 6, 2);
        fillArea(24, 29, 25, 35, 2);
        fillArea(0, 17, 13, 18, 2);
        fillArea(36, 17, 49, 18, 2);
        fillArea(17, 5, 25, 6, 2);
        fillArea(24, 29, 32, 30, 2);
        fillArea(12, 17, 13, 25, 2);
        fillArea(36, 10, 37, 18, 2);
    }

    // Nam add: Fill helper for overlaying tile types
    private void fillArea(int x1, int y1, int x2, int y2, int tileType) {
        for (int y = y1; y <= y2 && y < height; y++) {
            for (int x = x1; x <= x2 && x < width; x++) {
                if (x >= 0 && y >= 0) {
                    grid[y][x] = tileType;
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

    public double getIntersectionCenterX() {
        ensureIntersectionCenter();
        return intersectionCenterX;
    }

    public double getIntersectionCenterY() {
        ensureIntersectionCenter();
        return intersectionCenterY;
    }

    //Nam add: Get intersection bounds for spawn regions
    public int[] getIntersectionBounds() {
        ensureIntersectionCenter();
        return new int[] { intersectionMinX, intersectionMinY, intersectionMaxX, intersectionMaxY };
    }
    
    // Nam adding: Helper methods for lane system
    
    /**
     * Check if tile is driveable (any type of road/lane)
     */
    public boolean isDriveable(int x, int y) {
        int tileType = getTileType(x, y);
        return tileType == 2; // Nam adding: road tiles only
    }

    public boolean isDriveableForVehicle(int x, int y) {
        int tileType = getTileType(x, y);
        if (tileType == 2) {
            return true;
        }
        if (tileType != 1) {
            return false;
        }
        // Allow short sidewalk bands that connect two road segments (crosswalks).
        return hasRoadBridge(x, y, 0, -1, 0, 1) || hasRoadBridge(x, y, -1, 0, 1, 0);
    }
    
    /**
     * Check if tile is intersection  
     */
    public boolean isIntersection(int x, int y) {
        // Nam adding: Intersection detected when road connects in both axes
        if (getTileType(x, y) != 2) {
            return false;
        }
        boolean vertical = isDriveable(x, y - 1) && isDriveable(x, y + 1);
        boolean horizontal = isDriveable(x - 1, y) && isDriveable(x + 1, y);
        return vertical && horizontal;
    }

    private void ensureIntersectionCenter() {
        if (intersectionCenterX != null && intersectionCenterY != null) {
            return;
        }
        int minX = width - 1; //Nam add: Default scan bounds
        int minY = height - 1; //Nam add: Default scan bounds
        int maxX = 0; //Nam add: Default scan bounds
        int maxY = 0; //Nam add: Default scan bounds
        boolean found = false; //Nam add: Scan flag

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (isIntersection(x, y)) {
                    found = true;
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        boolean boundsTooWide = !found || (maxX - minX + 1) > width * 0.8
                || (maxY - minY + 1) > height * 0.8; //Nam add: Fallback if scan is too wide
        if (boundsTooWide) {
            int[] refBounds = getReferenceIntersectionBounds(); //Nam add: Use reference layout bounds
            minX = refBounds[0];
            minY = refBounds[1];
            maxX = refBounds[2];
            maxY = refBounds[3];
        }

        intersectionMinX = minX; //Nam add: Cache bounds
        intersectionMinY = minY; //Nam add: Cache bounds
        intersectionMaxX = maxX; //Nam add: Cache bounds
        intersectionMaxY = maxY; //Nam add: Cache bounds

        intersectionCenterX = (minX + maxX + 1) / 2.0;
        intersectionCenterY = (minY + maxY + 1) / 2.0;
    }

    //Nam add: Reference intersection bounds from Map.java layout data
    private int[] getReferenceIntersectionBounds() {
        int minX = 17;
        int maxX = 32;
        int minY = 10;
        int maxY = 25;
        return new int[] { minX, minY, maxX, maxY };
    }
    
    /**
     * Get allowed direction for lane type
     */
    public String getLaneDirection(int x, int y) {
        int tileType = getTileType(x, y);
        switch (tileType) {
            case 2: return "ALL";       // Nam adding: road allows all directions
            case 3: return "SOUTH";     // South-bound ↓  
            case 4: return "EAST";      // East-bound →
            case 5: return "WEST";      // West-bound ←
            case 6: return "ALL";       // Intersection
            case 7: return "LEFT_TURN"; // Left turn lane
            case 8: return "RIGHT_TURN";// Right turn lane
            default: return "NONE";     // Not a lane
        }
    }
    
    /**
     * Check if vehicle direction matches lane direction
     */
    public boolean isValidLaneDirection(int x, int y, double dirX, double dirY) {
        String laneDir = getLaneDirection(x, y);
        
        // Nam adding: Determine vehicle's primary movement direction
        if (Math.abs(dirY) > Math.abs(dirX)) {
            // Primarily vertical movement
            if (dirY > 0) { // Moving down
                return laneDir.equals("SOUTH") || laneDir.equals("ALL")
                    || laneDir.equals("LEFT_TURN") || laneDir.equals("RIGHT_TURN"); // Nam adding: allow turn lanes
            } else { // Moving up  
                return laneDir.equals("NORTH") || laneDir.equals("ALL")
                    || laneDir.equals("LEFT_TURN") || laneDir.equals("RIGHT_TURN"); // Nam adding: allow turn lanes
            }
        } else {
            // Primarily horizontal movement
            if (dirX > 0) { // Moving right
                return laneDir.equals("EAST") || laneDir.equals("ALL")
                    || laneDir.equals("LEFT_TURN") || laneDir.equals("RIGHT_TURN"); // Nam adding: allow turn lanes
            } else { // Moving left
                return laneDir.equals("WEST") || laneDir.equals("ALL")
                    || laneDir.equals("LEFT_TURN") || laneDir.equals("RIGHT_TURN"); // Nam adding: allow turn lanes
            }
        }
    }

    private boolean hasRoadBridge(int x, int y, int dirAx, int dirAy, int dirBx, int dirBy) {
        return hasDriveableInRange(x, y, dirAx, dirAy, 2) && hasDriveableInRange(x, y, dirBx, dirBy, 2);
    }

    private boolean hasDriveableInRange(int x, int y, int dx, int dy, int maxSteps) {
        for (int step = 1; step <= maxSteps; step++) {
            int nx = x + dx * step;
            int ny = y + dy * step;
            if (!isValid(nx, ny)) {
                return false;
            }
            if (isDriveable(nx, ny)) {
                return true;
            }
        }
        return false;
    }
}
