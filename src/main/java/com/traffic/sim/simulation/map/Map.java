package com.traffic.sim.simulation.map;

import com.traffic.sim.simulation.entities.Region;

/**
 * Represents the simulation grid.
 * TileType values:
 * - 0 = ROAD (đường xe chạy)
 * - 1 = BLOCKED (vùng cấm - người or xe đang chiếm chỗ)
 * - 2 = SIDEWALK (vỉa hè - người đi bộ có thể đi)
 * - 3 = CROSSWALK (vạch kẻ đường - người đi bộ qua đường)
 * - 4 = STOP_LINE (vạch dừng đèn đỏ)
 * - 5 = LANE_DIVIDER (vạch chia làn)
 */
public class Map {
    public static final int ROAD = 0;
    public static final int BLOCKED = 1;
    public static final int SIDEWALK = 2;
    public static final int CROSSWALK = 3;
    public static final int STOP_LINE = 4;
    public static final int LANE_DIVIDER = 5;

    private int[][] grid;
    private int width;
    private int height;
    private Double intersectionCenterX;
    private Double intersectionCenterY;
    private Integer intersectionMinX; // Nam add: Intersection bounds cache
    private Integer intersectionMinY; // Nam add: Intersection bounds cache
    private Integer intersectionMaxX; // Nam add: Intersection bounds cache
    private Integer intersectionMaxY; // Nam add: Intersection bounds cache

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width]; // grid[y][x]
        initializeDefaultMap();
    }

    private void initializeDefaultMap() {
        // Bước 1: Đặt tất cả là ROAD trước
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = ROAD;
            }
        }

        // Bước 2: Đặt 4 góc là SIDEWALK (bao gồm cả tòa nhà cũ)
        // Góc trên bên trái: x=0-16, y=0-9
        fillArea(0, 0, 16, 9, SIDEWALK);
        // Góc trên bên phải: x=33-49, y=0-9
        fillArea(33, 0, 49, 9, SIDEWALK);
        // Góc dưới bên trái: x=0-16, y=26-35
        fillArea(0, 26, 16, 35, SIDEWALK);
        // Góc dưới bên phải: x=33-49, y=26-35
        fillArea(33, 26, 49, 35, SIDEWALK);

        // Bước 3: Đã gộp vào Bước 2

        // Bước 4: Vạch kẻ đường cho người đi bộ (CROSSWALK)
        // Vạch phía trên (ngang): x=17-32, y=7-8
        fillArea(17, 7, 32, 8, CROSSWALK);
        // Vạch phía dưới (ngang): x=17-32, y=27-28
        fillArea(17, 27, 32, 28, CROSSWALK);
        // Vạch phía trái (dọc): x=14-15, y=10-25
        fillArea(14, 10, 15, 25, CROSSWALK);
        // Vạch phía phải (dọc): x=34-35, y=10-25
        fillArea(34, 10, 35, 25, CROSSWALK);

        // Bước 5: Vạch chia làn đường (LANE_DIVIDER)
        // Phía trên: x=25, y=0-5
        fillArea(25, 0, 25, 6, LANE_DIVIDER);
        // Phía dưới: x=24, y=30-35
        fillArea(24, 30, 24, 35, LANE_DIVIDER);
        // Phía trái: x=0-12, y=17
        fillArea(0, 17, 12, 17, LANE_DIVIDER);
        // Phía phải: x=37-49, y=18
        fillArea(37, 18, 49, 18, LANE_DIVIDER);

        // Bước 6: Vạch dừng đèn đỏ (STOP_LINE)
        // Phía trên: x=17-25, y=5
        fillArea(17, 5, 25, 5, STOP_LINE);
        // Phía dưới: x=24-32, y=30
        fillArea(24, 30, 32, 30, STOP_LINE);
        // Phía trái: x=12, y=17-25
        fillArea(12, 17, 12, 25, STOP_LINE);
        // Phía phải: x=37, y=10-18
        fillArea(37, 10, 37, 18, STOP_LINE);
    }

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
        if (isValidCoordinate(x, y)) {
            return grid[y][x];
        }
        return BLOCKED;
    }

    // Nam add: Allow updating grid for dynamic obstacles
    public void setTileType(int x, int y, int type) {
        if (isValidCoordinate(x, y)) {
            grid[y][x] = type;
        }
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isValid(int x, int y) {
        return isValidCoordinate(x, y);
    }

    public boolean isRoad(int x, int y) {
        int type = getTileType(x, y);
        return type == ROAD || type == LANE_DIVIDER || type == STOP_LINE;
    }

    public boolean isSidewalk(int x, int y) {
        return getTileType(x, y) == SIDEWALK;
    }

    public boolean isCrosswalk(int x, int y) {
        return getTileType(x, y) == CROSSWALK;
    }

    public boolean isWalkable(int x, int y) {
        int type = getTileType(x, y);
        return type == SIDEWALK || type == CROSSWALK;
    }

    public boolean isDrivable(int x, int y) {
        int type = getTileType(x, y);
        return type == ROAD || type == CROSSWALK || type == LANE_DIVIDER || type == STOP_LINE;
    }

    public boolean isBlocked(int x, int y) {
        return getTileType(x, y) == BLOCKED;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[][] getGrid() {
        return grid;
    }

    public double getIntersectionCenterX() {
        ensureIntersectionCenter();
        return intersectionCenterX;
    }

    public double getIntersectionCenterY() {
        ensureIntersectionCenter();
        return intersectionCenterY;
    }

    // Nam add: Get intersection bounds for spawn regions
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
        return tileType == ROAD || tileType == CROSSWALK || tileType == STOP_LINE || tileType == LANE_DIVIDER;
    }

    public boolean isDriveableForVehicle(int x, int y) {
        int tileType = getTileType(x, y);
        // Fix: Check for actual road tiles (ROAD=0), not SIDEWALK=2
        if (tileType == ROAD || tileType == CROSSWALK || tileType == STOP_LINE || tileType == LANE_DIVIDER) {
            return true;
        }
        return false;
    }

    /**
     * Check if tile is intersection
     */
    public boolean isIntersection(int x, int y) {
        // Nam adding: Intersection detected when road connects in both axes
        if (!isDriveable(x, y)) {
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
        int minX = width - 1; // Nam add: Default scan bounds
        int minY = height - 1; // Nam add: Default scan bounds
        int maxX = 0; // Nam add: Default scan bounds
        int maxY = 0; // Nam add: Default scan bounds
        boolean found = false; // Nam add: Scan flag

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (isIntersection(x, y)) {
                    found = true;
                    if (x < minX)
                        minX = x;
                    if (y < minY)
                        minY = y;
                    if (x > maxX)
                        maxX = x;
                    if (y > maxY)
                        maxY = y;
                }
            }
        }

        boolean boundsTooWide = !found || (maxX - minX + 1) > width * 0.8
                || (maxY - minY + 1) > height * 0.8; // Nam add: Fallback if scan is too wide
        if (boundsTooWide) {
            int[] refBounds = getReferenceIntersectionBounds(); // Nam add: Use reference layout bounds
            minX = refBounds[0];
            minY = refBounds[1];
            maxX = refBounds[2];
            maxY = refBounds[3];
        }

        intersectionMinX = minX; // Nam add: Cache bounds
        intersectionMinY = minY; // Nam add: Cache bounds
        intersectionMaxX = maxX; // Nam add: Cache bounds
        intersectionMaxY = maxY; // Nam add: Cache bounds

        intersectionCenterX = (minX + maxX + 1) / 2.0;
        intersectionCenterY = (minY + maxY + 1) / 2.0;
    }

    // Nam add: Reference intersection bounds from Map.java layout data
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
            case 2:
                return "ALL"; // Nam adding: road allows all directions
            case 3:
                return "SOUTH"; // South-bound ↓
            case 4:
                return "EAST"; // East-bound →
            case 5:
                return "WEST"; // West-bound ←
            case 6:
                return "ALL"; // Intersection
            case 7:
                return "LEFT_TURN"; // Left turn lane
            case 8:
                return "RIGHT_TURN";// Right turn lane
            default:
                return "NONE"; // Not a lane
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

    // Helper method to get stop line position for a specific region
    public double[] getStopLinePosition(Region region) {
        switch (region) {
            case NORTH:
                // Stop line phía trên: x=17-25, y=5
                return new double[] { 21.0, 5.5 }; // center of stop line
            case SOUTH:
                // Stop line phía dưới: x=24-32, y=30
                return new double[] { 28.0, 30.5 }; // center of stop line
            case WEST:
                // Stop line phía trái: x=12, y=17-25
                return new double[] { 12.5, 21.0 }; // center of stop line
            case EAST:
                // Stop line phía phải: x=37, y=10-18
                return new double[] { 37.5, 14.0 }; // center of stop line
            default:
                return new double[] { 0.0, 0.0 };
        }
    }

    // Check if a position is on stop line
    public boolean isStopLine(int x, int y) {
        return getTileType(x, y) == STOP_LINE;
    }

    /**
     * Debug method to print the grid to console.
     * 0: ROAD, 1: BLOCKED, 2: SIDEWALK, 3: CROSSWALK, 4: STOP_LINE, 5: LANE_DIVIDER
     */
    public void printGrid() {
        System.out.println("=== MAP GRID DEBUG ===");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(grid[y][x] + " ");
            }
            System.out.println();
        }
        System.out.println("======================");
    }
}
